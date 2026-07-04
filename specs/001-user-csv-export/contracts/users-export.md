# Contract: Export tenant users as CSV

## Endpoint

```
GET /v1/users/export
```

- **Controller**: `com.mgr.api.controller.UserController#exportUsers`
- **Produces**: `text/csv; charset=UTF-8`
- **Auth**: OAuth2 Bearer token (existing). ADMIN only.
- **Multi-tenancy**: requires `X-Tenant` header identifying the target tenant; must be one of
  the caller's allowed tenants (validated by `LogInterceptor`).

## Request

### Headers
| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <access_token>` |
| `X-Tenant` | Yes | Tenant identifier; must be in the token's allowed-tenant list. Absent → falls back to master/default scope (not the intended tenant export). |

### Query parameters
| Param | Required | Allowed values | Description |
|-------|----------|----------------|-------------|
| `status` | No | `active`, `locked` (case-insensitive) | Filters exported users by account status. Omitted → all users regardless of status (FR-007). |

## Success response — 200 OK

- **Content-Type**: `text/csv; charset=UTF-8`
- **Content-Disposition**: `attachment; filename="users-export-<yyyyMMdd>.csv"`
- **Body**: UTF-8 **with BOM** (`EF BB BF`), one header row + zero-or-more data rows.

### Header row (fixed order, FR-004)
```
Họ tên,Email,Vai trò,Trạng thái,Ngày tạo
```

### Data row columns (in order)
1. Full name (`Account.fullName`)
2. Email (`Account.email`)
3. Role / "Vai trò" (`Account.group.name`)
4. Status (`active` | `locked`)
5. Creation date (`yyyy-MM-dd`)

Values containing `,`, `"`, or newlines are RFC-4180 quoted (FR-012). Empty result set →
header row only, still 200 (FR-008).

### Example body
```
Họ tên,Email,Vai trò,Trạng thái,Ngày tạo
Nguyễn Văn A,a@example.com,Quản trị,active,2026-01-15
"Trần Thị B, C",b@example.com,Nhân viên,locked,2026-02-03
```

## Error responses

All errors use the standard `ApiMessageDto` envelope via the global `@ControllerAdvice`
(`result=false`, `code`, `message`). No file bytes are written on the error path.

| Status | When | Notes |
|--------|------|-------|
| 400 Bad Request | `status` value outside {`active`,`locked`} (FR-011) | Message states allowed values |
| 401 Unauthorized | Missing/invalid token, no resolvable auth (FR-010) | No file produced |
| 403 Forbidden | Authenticated but not ADMIN (FR-009), or `X-Tenant` not in allowed list | No file produced |

## Invariants (verification hooks — see quickstart.md)

- Every row belongs to the tenant in `X-Tenant`; no cross-tenant rows (SC-001).
- Exactly 5 columns; no `password`/token/2FA field ever appears (SC-002, FR-005).
- Non-admin / no-tenant callers get no file (SC-003).
- With `status` filter, 100% of rows match; no match → header-only file (SC-004).
- File opens correctly in Excel with Vietnamese diacritics intact even when fields contain
  commas/quotes/newlines (SC-005).
