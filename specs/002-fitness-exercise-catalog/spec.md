# Feature Specification: Fitness Exercise Catalog (CRUD)

**Feature Branch**: `002-fitness-exercise-catalog`

**Created**: 2026-07-04

**Status**: Draft

**Input**: User description: "Create a CRUD feature for the FitnessExercise model. FitnessExercise manages a catalog of physical exercises (e.g., Push-ups, Squats) including their target muscle groups and instructions within a healthcare and fitness application."

## Clarifications

### Session 2026-07-04

- Q: Must an exercise name be unique across the catalog? → A: No — names may repeat; only the identifier is unique.
- Q: Which criteria should the list endpoint support for search/filtering? → A: Name only (partial/contains match).
- Q: What should the delete operation actually do? → A: Hard delete — permanently remove the row from storage.
- Q: By default, which exercises should the list endpoint return? → A: Only active/visible exercises; inactive ones are excluded.
- Q: What maximum lengths should apply to name and instructions? → A: Name ≤ 255 characters, instructions ≤ 2000 characters.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse and view the exercise catalog (Priority: P1)

A fitness content consumer (trainer or end user) needs to see the list of available exercises and open any single exercise to read its full details — name, target muscle group, difficulty level, and step-by-step instructions — so they can select appropriate exercises for a program.

**Why this priority**: A catalog has no value unless it can be read. Viewing (list + detail) is the minimum slice that delivers user value on its own, independently of who can edit it.

**Independent Test**: Seed a few exercises, then request the paginated list and request one exercise by its identifier; confirm the returned data matches what was stored and that a request for a non-existent exercise returns a clear "not found" response.

**Acceptance Scenarios**:

1. **Given** several exercises exist in the catalog, **When** a viewer requests the exercise list with search/pagination criteria, **Then** a paginated set of matching exercises is returned in a consistent response envelope.
2. **Given** an exercise with a known identifier exists, **When** a viewer requests that exercise by identifier, **Then** its full details (name, target muscle group, difficulty level, instructions, status) are returned.
3. **Given** no exercise exists for a given identifier, **When** a viewer requests it, **Then** a clear "exercise not found" error is returned rather than an empty or malformed success response.

---

### User Story 2 - Create a new exercise (Priority: P2)

A catalog administrator needs to add a new exercise to the catalog by supplying its name, target muscle group, difficulty level, and instructions, so the catalog stays current with the exercises the program offers.

**Why this priority**: Content must be creatable to grow the catalog, but it depends on the read slice (P1) to be meaningfully verified and used.

**Independent Test**: Submit a valid new-exercise request and confirm it becomes retrievable via the list and detail endpoints; submit an invalid request (e.g., missing name or out-of-range difficulty level) and confirm it is rejected with field-level validation errors.

**Acceptance Scenarios**:

1. **Given** a valid new-exercise submission, **When** an administrator creates it, **Then** the exercise is persisted and appears in subsequent list/detail reads.
2. **Given** a submission whose difficulty level is not one of the allowed catalog levels, **When** an administrator submits it, **Then** the request is rejected with a validation error identifying the difficulty-level field.
3. **Given** a submission missing a required field (e.g., name), **When** an administrator submits it, **Then** the request is rejected with a validation error naming the missing field.

---

### User Story 3 - Update an existing exercise (Priority: P3)

A catalog administrator needs to correct or refine an existing exercise (e.g., fix instructions, re-classify its difficulty level or muscle group) so the catalog stays accurate.

**Why this priority**: Editing is valuable but lower urgency than being able to view and create content; the catalog is still usable without it.

**Independent Test**: Update an existing exercise's fields, then read it back and confirm the changes are reflected; attempt to update a non-existent exercise and confirm a "not found" error.

**Acceptance Scenarios**:

1. **Given** an existing exercise, **When** an administrator submits a valid update, **Then** the changed fields are persisted and reflected in subsequent reads.
2. **Given** an update targeting a non-existent identifier, **When** an administrator submits it, **Then** a clear "exercise not found" error is returned.
3. **Given** an update whose difficulty level is invalid, **When** an administrator submits it, **Then** the request is rejected with a validation error on the difficulty-level field.

---

### User Story 4 - Remove an exercise (Priority: P3)

A catalog administrator needs to remove an exercise that is no longer offered so users are not shown obsolete content.

**Why this priority**: Cleanup capability is useful but the least urgent of the CRUD operations for an MVP.

**Independent Test**: Delete an existing exercise and confirm it no longer appears in list/detail reads; attempt to delete a non-existent exercise and confirm a "not found" error.

**Acceptance Scenarios**:

1. **Given** an existing exercise, **When** an administrator deletes it, **Then** it no longer appears in subsequent list/detail reads.
2. **Given** a delete targeting a non-existent identifier, **When** an administrator submits it, **Then** a clear "exercise not found" error is returned.

---

### Edge Cases

- What happens when the difficulty level is outside the allowed catalog set (e.g., 0, 4, or negative)? → The request is rejected with a field-level validation error before any persistence occurs.
- What happens when a required text field (name, instructions) is blank or exceeds the maximum allowed length (name > 255 or instructions > 2000 characters)? → The request is rejected with a field-level validation error.
- What happens when a user without the appropriate permission attempts a create/update/delete/view operation? → The operation is refused on authorization grounds before any business logic runs.
- How does the system handle a read, update, or delete for an identifier that does not exist? → A consistent "exercise not found" error is returned in the standard envelope.
- What happens when two administrators edit the same exercise concurrently? → Last valid write wins for v1 (no optimistic-locking guarantee assumed).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow an authorized viewer to retrieve a paginated list of fitness exercises, filterable by a partial (contains) match on the exercise name. No other filter criteria are supported in v1.
- **FR-002**: System MUST allow an authorized viewer to retrieve the full details of a single exercise by its identifier.
- **FR-003**: System MUST allow an authorized administrator to create a new exercise with a name, target muscle group, difficulty level, and instructions.
- **FR-004**: System MUST allow an authorized administrator to update an existing exercise's fields.
- **FR-005**: System MUST allow an authorized administrator to hard-delete an existing exercise (permanent removal of the record from storage); after deletion it is retrievable by no read operation. The activation/visibility status (FR-012) governs whether a still-existing exercise is shown, and is independent of deletion.
- **FR-006**: System MUST validate all submitted exercise data before persistence, rejecting invalid submissions with field-level error messages in the standard response envelope.
- **FR-007**: System MUST restrict the difficulty level to a fixed, well-defined set of catalog levels (Beginner, Intermediate, Advanced) and reject any value outside that set.
- **FR-008**: System MUST require a non-blank exercise name (maximum 255 characters) and non-blank instructions (maximum 2000 characters), rejecting any submission that is blank or exceeds these lengths with a field-level validation error.
- **FR-009**: System MUST enforce distinct permissions for viewing, creating, updating, and deleting exercises, refusing any operation the caller is not authorized for.
- **FR-010**: System MUST return every response — success or error — in the application's uniform response envelope with a consistent status and message structure.
- **FR-011**: System MUST return a clear, consistent "not found" error when an operation targets an exercise identifier that does not exist.
- **FR-012**: System MUST expose each exercise's activation/visibility status so obsolete or hidden exercises can be distinguished from active ones.
- **FR-013**: The list endpoint MUST return only active/visible exercises by default; exercises whose status is inactive/hidden MUST be excluded from list results. (Single-exercise detail reads by identifier are not restricted by status.)

### Key Entities *(include if feature involves data)*

- **FitnessExercise**: A single physical exercise in the catalog. Key attributes: unique identifier (the only uniqueness guarantee — names are not required to be unique and may repeat); name (e.g., "Push-ups"); target muscle group (e.g., "Chest", "Legs"); difficulty level (one of Beginner / Intermediate / Advanced); instructions (step-by-step guidance text); status (active/visible flag). Standard auditing/ownership attributes follow the application's existing base-entity conventions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An authorized administrator can create a new exercise and see it returned in a subsequent catalog read within the same session, in under 1 minute of interaction.
- **SC-002**: 100% of submissions with a difficulty level outside the allowed catalog set are rejected before persistence, with no invalid records ever stored.
- **SC-003**: Every create, read, update, and delete response — including every validation and not-found error — is returned in the uniform response envelope (100% envelope consistency).
- **SC-004**: 100% of create, update, and delete attempts by callers lacking the corresponding permission are refused.
- **SC-005**: A catalog viewer can retrieve a page of exercises and open any single exercise's full details without encountering malformed or partial responses in 100% of read attempts against existing data.

## Assumptions

- The feature reuses the application's existing authentication/authorization stack; the four operations map to distinct permission suffixes for Create, Update, Delete, and View (`_C`, `_U`, `_D`, `_V`).
- The feature reuses the application's existing uniform response envelope and centralized error-handling mechanism.
- Difficulty level is modeled as a small fixed integer-backed set of catalog levels (Beginner, Intermediate, Advanced) whose canonical values live in the shared application constants; no free-form difficulty values are permitted.
- Target muscle group is treated as descriptive catalog text for v1 (not a separately managed reference entity); introducing a managed muscle-group taxonomy is out of scope for this feature.
- Multi-tenancy and auditing follow the application's existing base-entity conventions and are inherited rather than redefined here. Deletion, however, is a **hard delete** (permanent row removal) for this feature — not a soft delete — even though the entity retains an activation/visibility status field for hiding still-existing exercises.
- Concurrency is "last valid write wins" for v1; optimistic locking is out of scope.
- Per project policy, correctness is verified by code review, compilation, and manual/exploratory API testing — no automated tests are in scope.

## Implementation Constraints *(carried into `/speckit-plan`)*

> These are recorded here so they propagate to planning; they intentionally constrain HOW and originate from the caller's stated Spring Boot Backend Constitution (cited as v1.1.0). **Divergence note:** the constitution currently on disk is v1.0.0, whose Principle II mandates a Controller-Service-Repository layering (a Service layer). The constraints below instead omit the Service layer. This conflict MUST be reconciled at the `/speckit-plan` Constitution Check gate before implementation.

- **Architecture**: Provide a Controller, a Repository, and a MapStruct Mapper only — no Service layer for these standard CRUD operations; lookup and persistence logic lives in the Controller.
- **Controller**: Extends the shared base controller (`ABasicController`); write endpoints are `@Transactional`; endpoints are protected with `@PreAuthorize` using the `_C` / `_U` / `_D` / `_V` permission suffixes; all responses wrapped in the standard `ApiMessageDto<T>` envelope.
- **Mapping**: A MapStruct `@Mapper` interface using the exact `@Mapper` configuration specified by the constitution; no manual setter chains.
- **Validation**: `javax.validation` annotations on request forms; the `difficultyLevel` integer field requires a **custom constraint annotation** (under `validation/`) plus its implementation (under `validation/impl/`), both referencing the Beginner/Intermediate/Advanced constants in `BaseConstant`.
- **Testing**: No unit, integration, or API tests are to be generated; verification ends at compilation plus manual API testing.
- **Database/Schema**: Author only the Java entity class(es). Do NOT hand-write Liquibase changelog XML. Schema is generated by running, in order, `mvn clean compile` then `mvn liquibase:diff`, executed manually by the developer against the compiled bytecode.
