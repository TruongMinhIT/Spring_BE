# Implementation Plan: User CSV Export

**Branch**: `001-user-csv-export` | **Date**: 2026-07-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-user-csv-export/spec.md`

## Summary

Add an admin-only endpoint that streams the current tenant's users as a downloadable
`text/csv` file. The endpoint reuses the existing OAuth2/JWT security and the existing
Hibernate `DATABASE` multi-tenancy routing (driven by `TenantContext` / the `X-Tenant`
header) so that only users of the caller's active tenant are read. Rows are written
directly to the HTTP response output stream with OpenCSV (RFC-4180 quoting), prefixed
with a UTF-8 BOM, using Vietnamese header labels and `yyyy-MM-dd` dates. Sensitive
account fields (password, tokens, 2FA) are never selected into the file. An optional
`status` query parameter filters to active or locked users.

## Technical Context

**Language/Version**: Java 11 (per `pom.xml`, unchanged)

**Primary Dependencies**: Spring Boot 2.3.0.RELEASE (Web, Data JPA, Security/OAuth2,
Validation), Hibernate multi-tenancy (`DATABASE` strategy, existing), OpenCSV (new —
see Complexity Tracking), Lombok, Springfox Swagger 2.

**Storage**: Per-tenant relational DB (MySQL/PostgreSQL) reached through the existing
`tenantEntityManagerFactory` + `CustomMultiTenantConnectionProvider`. Users/accounts are
read from the tenant-routed persistence unit so the result is automatically scoped to the
current tenant. No schema change; no Liquibase changelog required.

**Testing**: PROHIBITED — automated testing (unit, integration, e2e, mock, or any other)
is forbidden by the constitution (Principle VIII, NON-NEGOTIABLE). Verification is via
code review and manual/exploratory checks only (see `quickstart.md`).

**Target Platform**: Linux server (Spring Boot fat jar / servlet container)

**Project Type**: Web service (single Spring Boot backend, `mgr-api`)

**Performance Goals**: Synchronous export for up to ~10,000 users per tenant per request.
Rows streamed to the response output stream (no full in-memory byte buffer) so heap stays
flat regardless of row count.

**Constraints**: Must not expose JPA entities at the boundary; must not leak sensitive
fields; must produce UTF-8-with-BOM output parseable by Excel; must enforce ADMIN-only
access and tenant isolation using existing mechanisms only.

**Scale/Scope**: One new read-only endpoint, one tenant-scoped read projection, one CSV
writer service, one status-filter enum/validation. No new tables, no migrations.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Clean Code & SOLID**: PASS — field `@Autowired` injection is the established pattern
  in this codebase; the new `UserExportService` is single-purpose and coded against a
  repository interface. CSV row shaping is isolated in one service, not duplicated.
- **II. Layered Architecture**: PASS — `UserController` (HTTP + response wiring only) →
  `UserExportService` (business logic: resolve status filter, shape rows, write CSV) →
  tenant `UserRepository` projection (persistence only). Controller never runs the query
  against the repository directly.
- **III. RESTful API & DTOs**: PASS — resource-oriented `GET /v1/users/export`; the CSV is
  the representation, and the internal row model is a projection/DTO, never the `User`/
  `Account` entity. (No JSON envelope on the success path because the representation is a
  file download; see V.)
- **IV. Validation & Centralized Exception Handling**: PASS — `status` query param validated
  against the allowed set (`active`/`locked`); invalid values raise the existing
  `BadRequestException` handled by the existing `@ControllerAdvice`. No ad-hoc try/catch that
  builds HTTP responses.
- **V. Consistent Responses & OpenAPI**: PARTIAL/JUSTIFIED — the success response is a binary
  `text/csv` download, which by nature cannot use the `ApiMessageDto` envelope. All **error**
  responses still flow through the standard envelope via the global handler. Endpoint carries
  Swagger annotations. Recorded in Complexity Tracking.
- **VI. Security**: PASS — reuses existing Spring Security/OAuth2/JWT; ADMIN-only via
  `@PreAuthorize` + admin-kind check; tenant isolation via existing `TenantContext` routing;
  no secrets added; JPA/criteria queries are parameterized; sensitive fields excluded from the
  projection and from logs.
- **VII. Minimal Dependencies & Spring Boot-First**: JUSTIFIED — adds OpenCSV. `commons-csv`
  is already on the classpath and could satisfy the need, but OpenCSV was explicitly selected
  for this feature; recorded and justified in Complexity Tracking. No version changes to Java
  or Spring Boot.
- **VIII. No Automated Testing (NON-NEGOTIABLE)**: PASS — no test tasks, files, or
  infrastructure introduced. Verification is manual (quickstart.md).

Gate result: **PASS** (two items justified in Complexity Tracking; no unjustified violations).

## Project Structure

### Documentation (this feature)

```text
specs/001-user-csv-export/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── users-export.md  # GET /v1/users/export contract
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

```text
mgr-api/source/mgr-api/
├── pom.xml                                   # + OpenCSV dependency (Complexity Tracking)
└── src/main/java/com/mgr/api/
    ├── controller/
    │   └── UserController.java               # + GET /export endpoint (streams text/csv)
    ├── service/
    │   ├── UserExportService.java            # NEW interface (business contract)
    │   └── impl/
    │       └── UserExportServiceImpl.java    # NEW: resolve filter, fetch rows, write CSV
    ├── repository/tenant/
    │   └── UserRepository.java               # NEW tenant-scoped repo (projection query)
    ├── dto/user/
    │   └── UserExportRow.java                # NEW projection/row DTO (no sensitive fields)
    ├── constant/
    │   └── UserExportStatus.java             # NEW enum: active/locked <-> status codes
    └── ...                                   # existing model/, exception/, config reused
```

**Structure Decision**: Single existing Spring Boot service (`mgr-api`). The feature is added
inside the current layered packages (`controller` / `service` / `service.impl` /
`repository.tenant` / `dto.user` / `constant`). A **tenant-scoped** `UserRepository` (in
`repository.tenant`, bound to `tenantEntityManagerFactory`) is introduced so the export query
is routed to the caller's tenant DB by the existing multi-tenancy machinery — distinct from
the existing master-scoped `repository.master.UserRepository` used by the CRUD endpoints.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| New dependency: OpenCSV (Principle VII) | Explicitly chosen technology for this feature; provides a streaming `CSVWriter` with RFC-4180 quoting for values containing commas/quotes/newlines (FR-012). | `commons-csv` (already on classpath) could do the same; rejected only because OpenCSV was the specified library. If the reviewer prefers zero new deps, `commons-csv` is a drop-in substitute with no other design change. |
| Success path bypasses the `ApiMessageDto` envelope (Principle V) | The successful response IS a downloadable `text/csv` file; wrapping binary file content in a JSON envelope would defeat the download and contradict FR-002. | A JSON envelope carrying inline/base64 CSV was rejected: it breaks the "downloadable file" requirement and Excel compatibility. Error responses still use the standard envelope. |
