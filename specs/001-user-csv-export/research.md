# Phase 0 Research: User CSV Export

All items from the user-supplied technical direction (Spring Boot, OpenCSV, REST streaming
`text/csv`, Spring Data JPA, ADMIN-only, filter by current tenant) are resolved below. No
open `NEEDS CLARIFICATION` remain.

## 1. Tenant scoping — how to export "only the current tenant's users"

- **Decision**: Read users through a **tenant-scoped** repository bound to the existing
  `tenantEntityManagerFactory` (`repository.tenant.UserRepository`). The request's tenant is
  already resolved by `LogInterceptor` from the `X-Tenant` header (validated against the
  token's allowed-tenant list) and stored in `TenantContext`; Hibernate's
  `CustomMultiTenantConnectionProvider` + `TenantIdentifierResolver` then route the query to
  that tenant's database automatically.
- **Rationale**: Tenant isolation is already implemented at the connection/datasource layer
  (commits `4df7066`, `e287d51`). Reusing it means the export needs **no** tenant column and
  **no** manual `WHERE tenant = ?`, and it cannot accidentally cross tenants. It also matches
  how the tenant business repositories (`News`, `Post`, …) are already queried.
- **Alternatives considered**:
  - *Reuse the master `repository.master.UserRepository` and add a tenant predicate*: rejected
    — the master persistence unit is not tenant-routed, and there is no tenant column on
    `Account`/`User` to filter on; it would bypass the isolation model.
  - *Add a `tenant_id` column + Specification filter*: rejected — a schema change (Liquibase)
    and redundant with the existing datasource-per-tenant design.
- **Edge case**: If no `X-Tenant` header is sent, `TenantContext` is cleared and the tenant
  resolver falls back to `DEFAULT_TENANT` (master). The contract therefore **requires** the
  `X-Tenant` header for this endpoint; absence yields the master/default scope, which is the
  documented behavior of the existing interceptor.

## 2. Authorization — "chỉ role ADMIN"

- **Decision**: Guard the endpoint with `@PreAuthorize` (consistent with existing
  `UserController` methods) **and** verify the caller is an administrator from the JWT:
  `getSessionFromToken().getUserKind() == MgrConstant.USER_KIND_ADMIN` (value `1`), also
  honoring `isSuperAdmin`. A non-admin or a caller with no resolvable tenant/authentication is
  rejected before any file bytes are written (FR-009, FR-010) — the global `@ControllerAdvice`
  turns this into a standard error envelope with 401/403.
- **Rationale**: The codebase already models "admin" as `Account.kind == USER_KIND_ADMIN`
  (`AccountController` sets it) and carries `userKind` + `isSuperAdmin` in `MgrJwt`. Reusing
  these avoids inventing a parallel role concept (Principle VI: reuse existing security).
- **Alternatives considered**:
  - *A granular permission code (e.g. a new `USE_EX`) via `@PreAuthorize("hasRole('USE_EX')")`
    like the CRUD endpoints*: viable and can be layered on, but the spec's requirement is the
    coarse "ADMIN role", so the admin-kind check is the authoritative gate. Teams that manage
    export as a discrete permission can additionally register `USE_EX`.
- **Note for `/speckit-tasks`**: confirm the exact `@PreAuthorize` expression with the team
  (existing admin role code vs. admin-kind check in the service). Both enforce the same
  outcome; the service-layer admin-kind check is the guaranteed backstop.

## 3. CSV generation — OpenCSV, streaming, RFC-4180 quoting

- **Decision**: Add OpenCSV and use `com.opencsv.CSVWriter` writing to an
  `OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)`. Emit the UTF-8 BOM
  bytes (`0xEF 0xBB 0xBF`) to the raw output stream **before** the writer, then write the
  header row and stream user rows. OpenCSV's default quoting escapes values containing commas,
  double quotes, and line breaks (FR-012).
- **Rationale**: OpenCSV was explicitly requested. `CSVWriter` supports incremental row
  writing, which keeps the response streaming and memory flat for up to ~10k rows.
- **Alternatives considered**:
  - *`commons-csv` (already on classpath)*: functionally equivalent (`CSVPrinter`); would avoid
    a new dependency. Documented in the plan's Complexity Tracking as the drop-in fallback if
    the reviewer rejects the new dependency.
  - *Build a full `byte[]`/`String` then return*: rejected — unnecessary heap pressure at the
    upper bound; streaming to the output stream is strictly better and still synchronous.

## 4. Streaming transport — REST `text/csv` download

- **Decision**: `@GetMapping(value = "/export", produces = "text/csv")`. Write directly to the
  injected `HttpServletResponse` output stream and set:
  `Content-Type: text/csv; charset=UTF-8` and
  `Content-Disposition: attachment; filename="users-export-<yyyyMMdd>.csv"`. Do **not** set
  `Content-Length` (streamed). Flush after writing.
- **Rationale**: Direct `HttpServletResponse` streaming is the simplest way to combine a
  content-disposition download with incremental writing under Spring MVC 5 (Boot 2.3), and it
  lets the global exception handler still own error responses because nothing is written to the
  stream until authorization/validation have passed.
- **Alternatives considered**: `StreamingResponseBody` / `ResponseEntity<StreamingResponseBody>`
  — also valid; rejected for minor extra ceremony, but acceptable if the team prefers it. The
  key invariant either way: **validate + authorize before writing the first byte**.

## 5. Column shaping & value mapping

- **Decision**: Select only `fullName`, `email`, role/group name, status, and `createdDate`
  into a projection DTO (`UserExportRow`) — never the `Account`/`User` entity, and never
  `password`, `resetPwdCode`, tokens, or 2FA secrets (FR-005).
  - **Status** maps from the numeric `status` column (`Auditable.status`): `1`
    (`STATUS_ACTIVE`) → `active`, `-1` (`STATUS_LOCK`) → `locked`. Any other internal value is
    mapped to the nearest of these two for display (per spec Assumptions). Filtering only
    accepts `active`/`locked`.
  - **Role/"Vai trò"**: derived from the account's `Group` name (the account's assigned group).
    Confirm the exact source label with the team in `/speckit-tasks`; `Group.name` is the
    working choice.
  - **Creation date** rendered `yyyy-MM-dd` (ISO-8601 date) via a fixed formatter (FR-004).
- **Rationale**: A dedicated projection keeps the entity out of the boundary (Principle III)
  and guarantees no sensitive field can leak.
- **Alternatives considered**: mapping the full entity then formatting in the writer — rejected
  (risks lazy-loading and over-exposure).

## 6. Header labels & encoding

- **Decision**: Header row (order fixed by FR-004): `Họ tên, Email, Vai trò, Trạng thái,
  Ngày tạo`. File encoded UTF-8 **with BOM** so Excel renders Vietnamese diacritics correctly
  (FR-013, clarifications).
- **Rationale**: Directly from the resolved clarifications in the spec.

## 7. Empty results

- **Decision**: When the tenant has no users, or the status filter matches none, still write
  the BOM + header row and zero data rows, returning HTTP 200 (FR-008).
- **Rationale**: A valid header-only CSV is the specified behavior.
