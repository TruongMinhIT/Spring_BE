---
description: "Task list for User CSV Export"
---

# Tasks: User CSV Export

**Input**: Design documents from `/specs/001-user-csv-export/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/users-export.md

**Tests**: PROHIBITED by the constitution (Principle VIII, NON-NEGOTIABLE). No test tasks are
generated. Verification is manual via [quickstart.md](./quickstart.md).

**Organization**: Tasks are grouped by user story. The three stories share one endpoint, so
US3 (authorization) and US2 (status filter) layer onto the US1 endpoint but each remains
independently verifiable.

**Base path**: all Java paths are under `mgr-api/source/mgr-api/src/main/java/com/mgr/api/`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1 / US2 / US3 (setup, foundational, polish have no story label)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Make the chosen CSV library available.

- [X] T001 Add the OpenCSV dependency (`com.opencsv:opencsv`) to `mgr-api/source/mgr-api/pom.xml` in the `<dependencies>` block, with a `<!-- -->` comment justifying it per Constitution Principle VII (see plan.md Complexity Tracking). If the reviewer opts to reuse `commons-csv` instead, skip this task and note the substitution.

**Checkpoint**: Project compiles with the CSV library on the classpath.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared types and persistence access that every user story depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 [P] Create the status enum `UserExportStatus` in `constant/UserExportStatus.java` with values `ACTIVE` (request `active`, db code `MgrConstant.STATUS_ACTIVE`=1, display `active`) and `LOCKED` (request `locked`, db code `MgrConstant.STATUS_LOCK`=-1, display `locked`); add a case-insensitive `fromRequestValue(String)` returning `Optional`/throwing for invalid input, plus `toDbCode()` and `displayValue()` helpers. (data-model.md → UserExportStatus)
- [X] T003 [P] Create the projection/row DTO `UserExportRow` in `dto/user/UserExportRow.java` with fields `fullName`, `email`, `roleName`, `status` (raw int), `createdDate` (Date) and a matching all-args constructor for a JPA constructor expression. Exclude every sensitive field. (data-model.md → UserExportRow, FR-005)
- [X] T004 Create the tenant-scoped repository `UserRepository` in `repository/tenant/UserRepository.java` extending `JpaRepository<User, Long>` (bound to `tenantEntityManagerFactory` via package `repository.tenant`). Add a JPQL constructor-expression projection `@Query("select new com.mgr.api.dto.user.UserExportRow(a.fullName, a.email, g.name, a.status, a.createdDate) from User u join u.account a left join a.group g ...")` returning `List<UserExportRow>`, ordered by `a.createdDate`. Select only non-sensitive columns. (research.md §1, §5; data-model.md)
- [X] T005 Create the service contract `UserExportService` in `service/UserExportService.java` with a method that takes an optional status filter and writes CSV to an `OutputStream` (e.g. `void writeUsersCsv(UserExportStatus statusFilter, OutputStream out)`), and its `service/impl/UserExportServiceImpl.java` skeleton (`@Service`, `@Autowired` tenant `UserRepository`). (plan.md structure)

**Checkpoint**: Shared types, tenant query, and service seam exist and compile.

---

## Phase 3: User Story 1 - Export all users of a tenant (Priority: P1) 🎯 MVP

**Goal**: An admin can download a `text/csv` file containing every user of their current
tenant, with the five specified columns, UTF-8 BOM, Vietnamese headers, and `yyyy-MM-dd` dates.

**Independent Test**: As an admin with `X-Tenant` set, `GET /v1/users/export` returns a CSV
with one row per tenant user (and none from other tenants), no sensitive fields, opening
correctly in Excel. (quickstart.md Scenario 1; SC-001, SC-002)

- [X] T006 [US1] Implement the all-users projection fetch in `repository/tenant/UserRepository.java` (the base variant of the T004 query with no status predicate) returning all current-tenant users. Confirm the query runs on the tenant-routed persistence unit so results are auto-scoped to `TenantContext`. (research.md §1)
- [X] T007 [US1] Implement `UserExportServiceImpl.writeUsersCsv(...)` in `service/impl/UserExportServiceImpl.java`: write the UTF-8 BOM bytes (`0xEF 0xBB 0xBF`) to the raw `OutputStream`, wrap it in an `OutputStreamWriter(out, StandardCharsets.UTF_8)` + OpenCSV `CSVWriter`, write the header row `Họ tên,Email,Vai trò,Trạng thái,Ngày tạo`, then stream each `UserExportRow` (status via `UserExportStatus.displayValue()`, date formatted `yyyy-MM-dd`, null-safe `roleName`), and flush. Empty result → header-only file. (research.md §3, §5, §6, §7; FR-004, FR-008, FR-012, FR-013)
- [X] T008 [US1] Add the endpoint `exportUsers` to `controller/UserController.java`: `@GetMapping(value = "/export", produces = "text/csv")`, inject `HttpServletResponse`, set `Content-Type: text/csv; charset=UTF-8` and `Content-Disposition: attachment; filename="users-export-<yyyyMMdd>.csv"`, delegate to `UserExportService` with `response.getOutputStream()`, no status filter yet. Add Springfox Swagger annotations (`@ApiOperation`). Inject the new `UserExportService`. (contracts/users-export.md; Constitution V)

**Checkpoint**: US1 is fully functional — admin export of all tenant users works end to end.

---

## Phase 4: User Story 3 - Access restricted to administrators (Priority: P1)

**Goal**: Only ADMIN callers with a resolvable tenant/auth context can export; everyone else
is refused with no file produced.

**Independent Test**: Non-admin → 403 no file; missing/invalid auth → 401 no file; `X-Tenant`
not in allowed list → 403. (quickstart.md Scenario 3; SC-003, FR-009, FR-010)

- [X] T009 [US3] Guard `exportUsers` in `controller/UserController.java` for ADMIN only: add `@PreAuthorize(...)` consistent with existing endpoints, AND enforce the admin check from the JWT (`getSessionFromToken().getUserKind() == MgrConstant.USER_KIND_ADMIN` or `isSuperAdmin()`), throwing the existing `ForbiddenException`/`BadRequestException` when not admin — before any output is written. (research.md §2; FR-009)
- [X] T010 [US3] Verify the auth/tenant refusal path produces no bytes: ensure the admin check and (existing interceptor) tenant validation run before `response.getOutputStream()` is touched, so failures are rendered by the global `@ControllerAdvice` in the standard `ApiMessageDto` envelope (401/403). Confirm `X-Tenant`-not-allowed is already rejected by `LogInterceptor`. (research.md §2, §4; FR-010)

**Checkpoint**: US1 + US3 — export works and is locked down to admins of the current tenant.

---

## Phase 5: User Story 2 - Export users filtered by status (Priority: P2)

**Goal**: An admin can pass `?status=active|locked` to export only matching users; invalid
values are rejected; no match yields a header-only file.

**Independent Test**: `?status=locked` → only locked rows; `?status=active` → only active
rows; `?status=frozen` → 400 listing allowed values; no-match → header-only file.
(quickstart.md Scenarios 2 & 4; SC-004, FR-006, FR-007, FR-011)

- [X] T011 [US2] Add the status-filtered projection variant in `repository/tenant/UserRepository.java`: same constructor-expression query with a `where a.status = :statusCode` predicate (or a nullable-param query that ignores the filter when null). (data-model.md; FR-006)
- [X] T012 [US2] Wire the filter through `UserExportServiceImpl`: when a `UserExportStatus` is provided use the filtered query, otherwise the all-users query (FR-007). (service/impl/UserExportServiceImpl.java)
- [X] T013 [US2] Add the optional `@RequestParam(required = false) String status` to `exportUsers` in `controller/UserController.java`; resolve it via `UserExportStatus.fromRequestValue(...)`, throwing `BadRequestException` (message states allowed values `active`, `locked`) for invalid input; pass the resolved filter to the service. Update Swagger param docs. (contracts/users-export.md; FR-011)

**Checkpoint**: All three stories functional — export, admin-only, and status filtering.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T014 [P] Confirm no sensitive field (password, resetPwdCode, tokens, 2FA) is reachable in `UserExportRow`, the JPQL query, or logs (FR-005; quickstart.md review checklist).
- [X] T015 [P] Verify Springfox Swagger annotations render the `/v1/users/export` endpoint (params, produces `text/csv`, responses) in Swagger UI (Constitution V).
- [ ] T016 Run all [quickstart.md](./quickstart.md) scenarios (1–6) against a running instance and confirm expected outcomes, including Excel diacritic rendering and CSV escaping (SC-005).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: T001 — start immediately.
- **Foundational (Phase 2)**: T002–T005 depend on T001 (compile with CSV lib). **Blocks all stories.**
- **US1 (Phase 3)**: depends on Foundational. MVP.
- **US3 (Phase 4)**: depends on US1 endpoint existing (T008).
- **US2 (Phase 5)**: depends on Foundational; layers onto the US1 endpoint (T008) and service (T007). Independent of US3.
- **Polish (Phase 6)**: after the desired stories are complete.

### User Story Dependencies

- **US1 (P1)**: after Foundational. No dependency on other stories.
- **US3 (P1)**: adds an authorization guard to the US1 endpoint (T008).
- **US2 (P2)**: adds a query param + filtered query to the US1 endpoint/service; independently testable.

### Within Each User Story

- Repository query → service → controller endpoint.
- Core implementation before integration/validation wiring.

### Parallel Opportunities

- Foundational: **T002** and **T003** are independent files → run in parallel. T004 depends on T003 (uses `UserExportRow`); T005 after T003.
- Polish: **T014** and **T015** in parallel.
- US2 and US3 touch overlapping files (`UserController.java`), so serialize their controller edits; their repository/service edits can proceed independently.

---

## Parallel Example: Phase 2 Foundational

```bash
# Independent new files — run together:
Task: "Create UserExportStatus enum in constant/UserExportStatus.java"
Task: "Create UserExportRow DTO in dto/user/UserExportRow.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 3)

1. Phase 1: Setup (T001).
2. Phase 2: Foundational (T002–T005).
3. Phase 3: US1 export (T006–T008) → **validate quickstart Scenario 1**.
4. Phase 4: US3 admin lockdown (T009–T010) → the export is now safe to expose (both P1).
5. Deploy/demo the secured MVP.

### Incremental Delivery

1. Setup + Foundational → foundation ready.
2. US1 + US3 → secured export MVP → demo.
3. US2 → status filtering → demo.
4. Polish (T014–T016) → final verification via quickstart.

---

## Notes

- [P] = different files, no dependencies.
- No automated tests (Constitution Principle VIII) — verify via quickstart.md and code review.
- Keep entities out of the boundary: only `UserExportRow` crosses service→CSV (Constitution III).
- Validate auth + status before writing the first byte so errors use the standard envelope.
- Commit after each task or logical group.
