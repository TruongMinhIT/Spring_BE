# Feature Specification: User CSV Export

**Feature Branch**: `001-user-csv-export`

**Created**: 2026-07-04

**Status**: Draft

**Input**: User description: "Export danh sách người dùng ra CSV — admin cần tải về danh sách người dùng của một tenant dưới dạng file CSV. Chỉ role ADMIN mới được xuất; file chỉ chứa người dùng thuộc tenant của người gọi; không đưa dữ liệu nhạy cảm vào file. Có thể lọc theo trạng thái (đang hoạt động / bị khoá). Mỗi dòng gồm: họ tên, email, vai trò, trạng thái, ngày tạo."

## Clarifications

### Session 2026-07-04

- Q: What character encoding should the CSV file use? → A: UTF-8 with BOM (for correct Vietnamese diacritic rendering in Excel).
- Q: In what language should the CSV header row labels appear? → A: Vietnamese (Họ tên, Email, Vai trò, Trạng thái, Ngày tạo).
- Q: How should the creation date be formatted in the CSV? → A: ISO 8601 date, yyyy-MM-dd.
- Q: Is there an expected upper bound on users per tenant per single synchronous request? → A: Small (≤ ~10k); build the file in memory and return synchronously.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Export all users of a tenant (Priority: P1)

An administrator of a tenant wants to obtain the full list of users belonging to their
tenant as a downloadable CSV file, so they can review, archive, or share the data outside
the system.

**Why this priority**: This is the core value of the feature — without the ability to
produce a downloadable file of the tenant's users, the feature delivers nothing. It is the
minimum viable slice.

**Independent Test**: Sign in as a tenant administrator, request a user export without any
filter, and confirm a CSV file downloads containing exactly the users of that tenant with
the expected columns and no sensitive data.

**Acceptance Scenarios**:

1. **Given** an administrator signed in to a tenant that has several users, **When** they
   request a user export, **Then** they receive a downloadable CSV file containing one row
   per user of that tenant plus a header row.
2. **Given** the CSV file has been produced, **When** the administrator opens it, **Then**
   each row shows the user's full name, email, role, status, and creation date, and no
   password, token, or other sensitive field appears.
3. **Given** an administrator of Tenant A requests an export, **When** the file is produced,
   **Then** it contains only Tenant A's users and none from any other tenant.

---

### User Story 2 - Export users filtered by status (Priority: P2)

An administrator wants to export only users in a specific account status (active or locked),
so they can work with a targeted subset instead of the whole list.

**Why this priority**: Filtering adds meaningful convenience but the feature is still
valuable without it (User Story 1 alone is usable). It builds directly on the export flow.

**Independent Test**: Sign in as a tenant administrator, request an export filtered to
"locked" users, and confirm the file contains only locked users of that tenant.

**Acceptance Scenarios**:

1. **Given** an administrator of a tenant with both active and locked users, **When** they
   request an export filtered to "active", **Then** the file contains only active users of
   that tenant.
2. **Given** an administrator requests an export filtered to "locked", **When** no user in
   the tenant currently has that status, **Then** the file is produced with only the header
   row and no data rows.

---

### User Story 3 - Access is restricted to administrators (Priority: P1)

A non-administrator user (or an unauthenticated caller) must not be able to export the user
list, so that tenant member data is not exposed to unauthorized parties.

**Why this priority**: This is a security boundary tied directly to the core flow; the
export must never be available to non-admins. It is required for the feature to be safe to
release.

**Independent Test**: Attempt the export as a signed-in non-admin user and as a caller with
no valid tenant/authentication, and confirm both are refused and no file is produced.

**Acceptance Scenarios**:

1. **Given** a signed-in user without the administrator role, **When** they attempt a user
   export, **Then** the request is refused and no file is produced.
2. **Given** a caller with no valid authentication or no resolvable tenant, **When** they
   attempt a user export, **Then** the request is refused and no file is produced.

---

### Edge Cases

- **Tenant has no users**: the export succeeds and produces a file containing only the
  header row and no data rows.
- **Status filter matches no users**: the export succeeds and produces a header-only file.
- **Invalid status filter value** (not "active" or "locked"): the request is rejected with a
  clear error indicating the allowed values.
- **Caller is not an administrator**: the request is refused (see User Story 3).
- **Caller has no valid tenant context**: the request is refused; no data is returned.
- **A field contains characters that conflict with CSV formatting** (comma, quote, or line
  break inside a name): the value is preserved intact and the file remains correctly
  parseable.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow an authenticated administrator to request an export of
  the users belonging to their own tenant.
- **FR-002**: The system MUST deliver the export as a downloadable CSV file.
- **FR-003**: The export MUST include only users belonging to the tenant of the requesting
  administrator, and MUST NOT include users from any other tenant.
- **FR-004**: Each data row MUST contain, in order, the user's full name, email, role,
  account status, and creation date. A header row MUST be present with the Vietnamese column
  labels: "Họ tên", "Email", "Vai trò", "Trạng thái", "Ngày tạo". The creation date MUST be
  rendered in ISO 8601 date format (yyyy-MM-dd).
- **FR-005**: The export MUST NOT include any sensitive data, including but not limited to
  passwords, password hashes, security tokens, or two-factor secrets.
- **FR-006**: The system MUST allow the administrator to optionally filter the exported users
  by account status, restricted to the values "active" and "locked".
- **FR-007**: When no status filter is provided, the system MUST export all users of the
  tenant regardless of status.
- **FR-008**: When the resolved set of users is empty (tenant has no users, or the status
  filter matches none), the system MUST still produce a valid CSV file containing the header
  row and no data rows.
- **FR-009**: The system MUST reject an export request from any caller who does not hold the
  administrator role, and MUST NOT produce a file in that case.
- **FR-010**: The system MUST reject an export request from a caller with no valid
  authentication or no resolvable tenant context, and MUST NOT produce a file in that case.
- **FR-011**: The system MUST reject an export request whose status filter value is outside
  the allowed set, returning a clear error that states the permitted values.
- **FR-012**: The system MUST format field values so that names or other fields containing
  commas, quotes, or line breaks remain intact and the resulting file is correctly parseable
  as CSV.
- **FR-013**: The system MUST encode the CSV file as UTF-8 with a byte-order mark (BOM), so
  that names containing Vietnamese diacritics render correctly when opened in a standard
  spreadsheet tool (e.g., Microsoft Excel).

### Key Entities *(include if feature involves data)*

- **User**: A member of a tenant. Relevant attributes for this feature: full name, email,
  role, account status (active or locked), and creation date. Belongs to exactly one tenant.
- **Tenant**: The organizational boundary that owns a set of users. The requesting
  administrator's tenant determines which users are eligible for export.
- **User Export File**: The CSV artifact produced by the feature. Contains a header row and
  zero or more user rows, scoped to a single tenant, excluding sensitive fields.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A tenant administrator can obtain a downloadable CSV of their tenant's users in
  a single request, with 100% of returned rows belonging to their own tenant.
- **SC-002**: The exported file contains exactly the five expected columns (full name, email,
  role, status, creation date) and zero sensitive fields, verified by inspecting the file.
- **SC-003**: 100% of export attempts by non-administrators or callers without a valid tenant
  are refused and produce no file.
- **SC-004**: When a status filter is applied, 100% of exported rows match the requested
  status; when no rows match, a header-only file is still produced.
- **SC-005**: Files remain correctly parseable as CSV in a standard spreadsheet tool even
  when user fields contain commas, quotes, or line breaks, and Vietnamese diacritics in names
  render correctly (UTF-8 with BOM) when the file is opened in Microsoft Excel.

## Assumptions

- "Administrator" refers to the existing ADMIN role in the system; authorization and tenant
  identity are derived from the caller's existing authenticated session, consistent with how
  the rest of the application scopes data per tenant.
- Account status is a two-value concept for this feature: "active" and "locked". Any other
  internal status representation is mapped to one of these for export purposes.
- The export reflects the users as they exist at the moment of the request; it is not a
  scheduled or historical snapshot.
- "Full name" is whatever the system already stores as the user's display/full name; no new
  name field is introduced by this feature.
- Date values are rendered in ISO 8601 date format (yyyy-MM-dd) in the file.
- The export is generated on demand and returned synchronously; asynchronous/background
  generation and email delivery are out of scope for this version.
- A tenant is expected to hold at most ~10,000 users for export purposes. At that scale the
  file is built in memory and returned in a single synchronous request; streaming or
  background generation is not required for this version.
