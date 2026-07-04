# Quickstart & Manual Validation: User CSV Export

Per the constitution (Principle VIII, no automated testing), correctness is verified by code
review and the manual/exploratory checks below. See [contracts/users-export.md](./contracts/users-export.md)
and [data-model.md](./data-model.md) for the full contract and field mapping.

## Prerequisites

- `mgr-api` running locally (`mgr-api/source/mgr-api`), reachable at `http://localhost:<port>`.
- A valid OAuth2 access token for an **ADMIN** account whose token grants access to at least
  one tenant (the `X-Tenant` value below must be in the token's allowed-tenant list).
- A second token for a **non-admin** account (for the negative check).
- The target tenant DB has a few users, ideally including at least one `active` and one
  `locked`, and at least one full name containing a comma or quote (to exercise FR-012).

## Build note

- OpenCSV must be present in `pom.xml` (or substitute the already-present `commons-csv` per
  the plan's Complexity Tracking). Confirm the app compiles and starts after the dependency
  change.

## Scenario 1 — Export all users of a tenant (P1, User Story 1)

```bash
curl -sS -D - \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant: $TENANT" \
  "http://localhost:$PORT/v1/users/export" \
  -o users-export.csv
```

**Expect**:
- HTTP `200`, `Content-Type: text/csv; charset=UTF-8`,
  `Content-Disposition: attachment; filename="users-export-*.csv"`.
- File begins with UTF-8 BOM, then the header `Họ tên,Email,Vai trò,Trạng thái,Ngày tạo`.
- One row per user of `$TENANT`; **no** users from any other tenant (SC-001).
- No `password`, token, or 2FA column anywhere (SC-002 / FR-005).
- Open in Excel → Vietnamese diacritics render correctly (SC-005).

## Scenario 2 — Filter by status (P2, User Story 2)

```bash
curl -sS -H "Authorization: Bearer $ADMIN_TOKEN" -H "X-Tenant: $TENANT" \
  "http://localhost:$PORT/v1/users/export?status=locked" -o locked.csv
```

**Expect**: 200; every data row's "Trạng thái" is `locked` (SC-004). Repeat with
`status=active`. If no user matches, the file contains **only** the header row (FR-008).

## Scenario 3 — Access restricted to admins (P1, User Story 3)

```bash
# Non-admin
curl -sS -o /dev/null -w "%{http_code}\n" \
  -H "Authorization: Bearer $NONADMIN_TOKEN" -H "X-Tenant: $TENANT" \
  "http://localhost:$PORT/v1/users/export"          # expect 403, no file

# No / invalid auth
curl -sS -o /dev/null -w "%{http_code}\n" -H "X-Tenant: $TENANT" \
  "http://localhost:$PORT/v1/users/export"          # expect 401, no file
```

**Expect**: refused with the standard error envelope; no CSV bytes produced (SC-003).

## Scenario 4 — Invalid status value (edge case, FR-011)

```bash
curl -sS -H "Authorization: Bearer $ADMIN_TOKEN" -H "X-Tenant: $TENANT" \
  "http://localhost:$PORT/v1/users/export?status=frozen"
```

**Expect**: HTTP `400`, standard error envelope, message states allowed values
(`active`, `locked`). No file.

## Scenario 5 — CSV escaping (edge case, FR-012)

Ensure a user's full name contains a comma or double-quote (e.g. `Trần Thị B, "C"`), export,
then re-open the file in a spreadsheet tool.

**Expect**: the value is preserved intact in a single cell; the file parses cleanly (SC-005).

## Scenario 6 — Empty tenant (edge case, FR-008)

Export for a tenant (allowed by the token) that has no users.

**Expect**: HTTP `200`, file with the header row and zero data rows.

## Review checklist (code review gate)

- [ ] Controller only wires HTTP/response; query + shaping live in `UserExportService` (Layering).
- [ ] `User`/`Account` entities never cross the boundary; only `UserExportRow` does (DTO).
- [ ] Projection selects no sensitive columns (password/tokens/2FA) (FR-005).
- [ ] Query runs on the tenant-routed `repository.tenant.UserRepository` (tenant isolation).
- [ ] ADMIN enforced before any byte is written; errors go through `@ControllerAdvice`.
- [ ] BOM written before header; UTF-8; `yyyy-MM-dd` dates; Vietnamese headers.
- [ ] Swagger/OpenAPI annotations present on the endpoint.
- [ ] No automated tests added (Principle VIII).
