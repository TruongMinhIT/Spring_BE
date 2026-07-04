# Phase 1 Data Model: User CSV Export

This feature is **read-only** and introduces **no new persistent entities and no schema
change**. It reuses the existing `User` / `Account` / `Group` entities and the existing
per-tenant multi-tenancy routing. Below are the existing entities consumed and the new
in-memory transfer types.

## Existing entities consumed (source of the export)

### User (`com.mgr.api.model.User`) — tenant-routed for this feature
- `id` (Long, shared with `Account` via `@MapsId`)
- `account` (`Account`, one-to-one) — carries the human-facing fields
- Other fields (`gender`, `dateOfBirth`, `dbConfigs`) are **not** exported.

### Account (`com.mgr.api.model.Account`)
- `fullName` (String) → CSV "Họ tên"
- `email` (String) → CSV "Email"
- `group` (`Group`, many-to-one) → source of CSV "Vai trò" (`group.name`)
- `status` (int, from `Auditable`) → CSV "Trạng thái" after mapping (see below)
- `createdDate` (Date, from `Auditable`) → CSV "Ngày tạo" formatted `yyyy-MM-dd`
- **Excluded (sensitive / irrelevant, FR-005)**: `password`, `resetPwdCode`, `resetPwdTime`,
  `attemptCode`, `attemptLogin`, `username`, `phone`, `avatarPath`, `isSuperAdmin`, `kind`,
  any token/2FA data.

### Group (`com.mgr.api.model.Group`)
- `name` — used as the exported role label ("Vai trò"). Exact label source to be confirmed in
  `/speckit-tasks`.

## New in-memory types (not persisted)

### UserExportRow (`com.mgr.api.dto.user.UserExportRow`) — projection / row DTO
One instance per exported user; the only shape that crosses the service→CSV boundary.

| Field | Type | Source | CSV column | Notes |
|-------|------|--------|------------|-------|
| `fullName` | String | `Account.fullName` | Họ tên | May contain commas/quotes/newlines → RFC-4180 quoted by OpenCSV (FR-012) |
| `email` | String | `Account.email` | Email | |
| `roleName` | String | `Account.group.name` | Vai trò | Null-safe (empty string if no group) |
| `status` | int (raw) → String | `Account.status` | Trạng thái | Rendered via `UserExportStatus` mapping |
| `createdDate` | Date → String | `Account.createdDate` | Ngày tạo | Rendered `yyyy-MM-dd` |

- Populated by a JPA projection query on the tenant-scoped `UserRepository` (constructor
  expression or interface projection) selecting **only** the columns above — no entity
  materialization, so sensitive columns are never loaded.
- Column order is fixed and matches FR-004.

### UserExportStatus (`com.mgr.api.constant.UserExportStatus`) — status enum
Two-value account status used for both the request filter and the rendered value.

| Enum | Request value | DB `status` code | CSV display |
|------|---------------|------------------|-------------|
| `ACTIVE` | `active` | `1` (`MgrConstant.STATUS_ACTIVE`) | `active` |
| `LOCKED` | `locked` | `-1` (`MgrConstant.STATUS_LOCK`) | `locked` |

- **Request parsing (FR-006, FR-011)**: `active`/`locked` (case-insensitive) → enum; absent →
  no filter (export all, FR-007); any other value → `BadRequestException` listing the allowed
  values.
- **Rendering**: any internal status other than the two mapped codes is displayed as the
  nearest of `active`/`locked` per the spec Assumptions.

## Relationships & scope

```
Tenant DB (current tenant, resolved by TenantContext)
  └── Account 1..1 User        (exported: fullName, email, status, createdDate)
        └── Group (many-to-one, optional)  → roleName ("Vai trò")
```

- The set of rows = all `User` rows in the **current tenant's** database, optionally filtered
  by `status`. No cross-tenant rows are reachable because the query runs on the tenant-routed
  persistence unit (see `research.md` §1).

## Validation rules (from requirements)

- `status` query param ∈ {`active`, `locked`} or absent (FR-006, FR-007, FR-011).
- Caller must be ADMIN with a resolvable tenant/auth context (FR-009, FR-010); otherwise no
  file is produced.
- Output excludes all sensitive fields (FR-005) and is UTF-8-with-BOM (FR-013).
- Empty result set still yields header-only file (FR-008).
