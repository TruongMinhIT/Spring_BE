---
description: "Task list for Fitness Exercise Catalog (CRUD)"
---

# Tasks: Fitness Exercise Catalog (CRUD)

**Input**: Design documents from `/specs/002-fitness-exercise-catalog/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**Tests**: PROHIBITED. The project constitution (Principle VIII, NON-NEGOTIABLE) forbids all automated testing. No test tasks are generated. Verification = `mvn clean compile` + code review + manual API testing per [quickstart.md](./quickstart.md).

**Organization**: Tasks are grouped by user story to enable independent implementation and delivery.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story the task belongs to (US1–US4)

## Path Conventions

All source paths are under the module root:
`mgr-api/source/mgr-api/src/main/java/com/mgr/api/`
(abbreviated below as `.../api/`). Mirrors the existing `Category` feature slice. **No Service layer** (approved Principle II deviation — see plan.md Complexity Tracking).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Shared constants and error codes used across all stories.

- [X] T001 Add difficulty-level constants (`DIFFICULTY_LEVEL_BEGINNER=1`, `DIFFICULTY_LEVEL_INTERMEDIATE=2`, `DIFFICULTY_LEVEL_ADVANCED=3`) to `.../api/constant/MgrConstant.java`
- [X] T002 [P] Add `FITNESS_EXERCISE_ERROR_NOT_FOUND` error code (e.g., `"ERROR-FITNESS-EXERCISE-0000"`) to `.../api/dto/ErrorCode.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Entity, persistence, and controller shell that every user story depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 Create `FitnessExercise` JPA entity in `.../api/model/FitnessExercise.java` — extends `Auditable<String>`, `@Table(name = TablePrefix.PREFIX_TABLE + "fitness_exercise")`, `@EntityListeners(AuditingEntityListener.class)`, fields `name` (`VARCHAR(255)`, **not unique**), `targetMuscleGroup` (`VARCHAR(255)`), `difficultyLevel` (`Integer`), `instructions` (`columnDefinition = "TEXT"`); Lombok `@Getter/@Setter`
- [X] T004 Create `FitnessExerciseRepository` in `.../api/repository/tenant/FitnessExerciseRepository.java` — extends `JpaRepository<FitnessExercise, Long>` and `JpaSpecificationExecutor<FitnessExercise>` (depends on T003)
- [X] T005 Create `FitnessExerciseController` shell in `.../api/controller/FitnessExerciseController.java` — `@RestController`, `@RequestMapping("/v1/fitness-exercise")`, `@CrossOrigin`, `@Slf4j`, `extends ABasicController`, `@Autowired FitnessExerciseRepository` (no endpoints yet) (depends on T004)

**Checkpoint**: Module compiles with entity, repository, and empty controller — story implementation can begin.

---

## Phase 3: User Story 1 - Browse and view the exercise catalog (Priority: P1) 🎯 MVP

**Goal**: Authorized viewers can retrieve a paginated, name-filtered list of **active** exercises and fetch full details of one exercise by id.

**Independent Test**: Seed a few exercises directly in the DB, call `GET /v1/fitness-exercise/list?name=...` and `GET /v1/fitness-exercise/get/{id}`; confirm stored data is returned in the envelope, inactive exercises are excluded from the list, and an unknown id returns the standard not-found error.

- [X] T006 [P] [US1] Create `FitnessExerciseDto` in `.../api/dto/fitnessExercise/FitnessExerciseDto.java` — extends `ABasicAdminDto`; fields `name`, `targetMuscleGroup`, `difficultyLevel`, `instructions`; Swagger `@ApiModelProperty`
- [X] T007 [P] [US1] Create `FitnessExerciseCriteria` in `.../api/model/criteria/FitnessExerciseCriteria.java` — fields `name`, `status`; `getSpecification()` adds `LIKE lower(name)` and `equal(status)` predicates (depends on T003)
- [X] T008 [US1] Create `FitnessExerciseMapper` in `.../api/mapper/FitnessExerciseMapper.java` — MapStruct `@Mapper(componentModel = "spring", unmappedTargetPolicy = IGNORE, nullValuePropertyMappingStrategy = IGNORE)`; `fromEntityToDto(FitnessExercise)` + `fromEntityToDtoList(List<FitnessExercise>)` (depends on T003, T006)
- [X] T009 [US1] Inject `FitnessExerciseMapper` into the controller and implement `GET /get/{id}` in `.../api/controller/FitnessExerciseController.java` — `@PreAuthorize("hasRole('EXERCISE_V')")`, not-found → `NotFoundException(..., ErrorCode.FITNESS_EXERCISE_ERROR_NOT_FOUND)`, returns `ApiMessageDto<FitnessExerciseDto>` (depends on T005, T008)
- [X] T010 [US1] Implement `GET /list` in `.../api/controller/FitnessExerciseController.java` — `@PreAuthorize("hasRole('EXERCISE_L')")`, accepts `FitnessExerciseCriteria` + `Pageable`, **forces `criteria.setStatus(MgrConstant.STATUS_ACTIVE)`**, returns `ApiMessageDto<ResponseListDto<FitnessExerciseDto>>` (depends on T007, T008, T009)

**Checkpoint**: US1 fully functional — catalog is readable (MVP deliverable).

---

## Phase 4: User Story 2 - Create a new exercise (Priority: P2)

**Goal**: Authorized administrators can add a new exercise; invalid submissions (bad difficulty, missing/oversized fields) are rejected with field-level errors.

**Independent Test**: `POST /v1/fitness-exercise/create` with a valid body → becomes retrievable via US1 reads; submit `difficultyLevel=4` or blank `name` → field-level validation error, nothing persisted.

- [X] T011 [P] [US2] Create custom constraint annotation `@ValidDifficultyLevel` in `.../api/validation/ValidDifficultyLevel.java` — `anyOf()` defaults to the three `MgrConstant.DIFFICULTY_LEVEL_*` values, `allowNull()` default `false` (mirror `ValidPostType`) (depends on T001)
- [X] T012 [P] [US2] Create `DifficultyLevelValidator` in `.../api/validation/impl/DifficultyLevelValidator.java` — `implements ConstraintValidator<ValidDifficultyLevel, Integer>` (mirror `PostTypeValidator`) (depends on T011)
- [X] T013 [US2] Create `CreateFitnessExerciseForm` in `.../api/form/fitnessExercise/CreateFitnessExerciseForm.java` — `@NotEmpty`+`@Size(max=255)` on `name` and `targetMuscleGroup`, `@NotNull`+`@ValidDifficultyLevel` on `difficultyLevel`, `@NotEmpty`+`@Size(max=2000)` on `instructions`; Swagger `@ApiModel`/`@ApiModelProperty` (depends on T011)
- [X] T014 [US2] Add `fromCreateFormToEntity(CreateFitnessExerciseForm)` to `.../api/mapper/FitnessExerciseMapper.java` — `@BeanMapping(ignoreByDefault = true)`, map the four business fields (depends on T008, T013)
- [X] T015 [US2] Implement `POST /create` in `.../api/controller/FitnessExerciseController.java` — `@Transactional`, `@PreAuthorize("hasRole('EXERCISE_C')")`, `@Valid @RequestBody` + `BindingResult`, **no name-uniqueness check**, `repository.save(...)`, returns `ApiMessageDto<String>` (depends on T005, T014)

**Checkpoint**: US1 + US2 work independently — catalog is readable and growable.

---

## Phase 5: User Story 3 - Update an existing exercise (Priority: P3)

**Goal**: Authorized administrators can update an existing exercise; unknown id → not-found, invalid difficulty → validation error.

**Independent Test**: `PUT /v1/fitness-exercise/update` on an existing exercise → changes reflected on `GET /get/{id}`; unknown id → not-found error; `difficultyLevel=4` → validation error.

- [X] T016 [P] [US3] Create `UpdateFitnessExerciseForm` in `.../api/form/fitnessExercise/UpdateFitnessExerciseForm.java` — `@NotNull id` plus the same validated fields as the create form (depends on T011)
- [X] T017 [US3] Implement `PUT /update` in `.../api/controller/FitnessExerciseController.java` — `@Transactional`, `@PreAuthorize("hasRole('EXERCISE_U')")`, `@Valid @RequestBody` + `BindingResult`, not-found → `NotFoundException`, apply changed fields, `repository.save(...)`, returns `ApiMessageDto<String>` (depends on T005, T016)

**Checkpoint**: US1–US3 all independently functional.

---

## Phase 6: User Story 4 - Remove an exercise (Priority: P3)

**Goal**: Authorized administrators can hard-delete an exercise; unknown id → not-found.

**Independent Test**: `DELETE /v1/fitness-exercise/delete/{id}` on an existing exercise → gone from list/detail; unknown id → not-found error.

- [X] T018 [US4] Implement `DELETE /delete/{id}` in `.../api/controller/FitnessExerciseController.java` — `@Transactional`, `@PreAuthorize("hasRole('EXERCISE_D')")`, not-found → `NotFoundException`, `repository.deleteById(id)` (hard delete), returns `makeSuccessResponse(...)` (depends on T005)

**Checkpoint**: All four CRUD stories independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Verification, documentation, and schema generation (no automated tests — Principle VIII).

- [X] T019 [P] Verify Swagger annotations present on the controller, both forms, and the DTO (Principle V)
- [X] T020 Generate schema manually from `mgr-api/source/mgr-api`: run `mvn clean compile` then `mvn liquibase:diff`; review the generated changelog fragment — **do NOT hand-edit changelog XML** (compile verified with JDK 11 — BUILD SUCCESS; `liquibase:diff` requires a live tenant DB connection, run by the developer)
- [ ] T021 Seed the five per-operation roles (`EXERCISE_V/_L/_C/_U/_D`) for a test admin, then execute all manual validation scenarios in [quickstart.md](./quickstart.md)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories.
- **User Stories (Phase 3–6)**: All depend on Foundational. US1 is the MVP. US2/US3/US4 depend only on the foundation, but all touch the same `FitnessExerciseController.java` file, so their controller-endpoint tasks (T015, T017, T018) must be serialized against each other even though their prep tasks (forms, validators) are parallelizable.
- **Polish (Phase 7)**: Depends on the desired stories being complete.

### Key task dependencies

- T004 → T003; T005 → T004
- T008 → T003, T006; T009 → T005, T008; T010 → T007, T008, T009
- T012 → T011; T013 → T011; T014 → T008, T013; T015 → T005, T014
- T016 → T011; T017 → T005, T016
- T018 → T005

### Parallel Opportunities

- T002 runs parallel to T001.
- Within US1: T006 and T007 are `[P]` (different files); T008 waits on T006.
- Within US2: T011/T012 (validation pair) and T013 (form) can proceed alongside; T016 (US3 form) is also `[P]` and can be built at the same time as the US2 forms.
- Endpoint tasks on the shared controller (T009/T010/T015/T017/T018) are **not** parallel with each other.

---

## Parallel Example: User Story 1

```bash
# After Foundational (T003–T005), launch the independent-file tasks together:
Task: "Create FitnessExerciseDto in .../api/dto/fitnessExercise/FitnessExerciseDto.java"      # T006
Task: "Create FitnessExerciseCriteria in .../api/model/criteria/FitnessExerciseCriteria.java" # T007
# Then T008 (mapper) → T009 (get) → T010 (list) sequentially.
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Phase 1 Setup → 2. Phase 2 Foundational → 3. Phase 3 US1 → **STOP & VALIDATE** the read catalog → deploy/demo.

### Incremental Delivery

Foundation → US1 (read, MVP) → US2 (create) → US3 (update) → US4 (delete). Each story adds value without breaking the previous ones. Finish with Phase 7 (schema diff + manual validation).

---

## Notes

- **No Service layer**: controller calls the repository directly (approved deviation, plan.md Complexity Tracking).
- **No name-uniqueness** check anywhere (clarification 2026-07-04).
- **List returns active only**; detail-by-id is not status-restricted.
- **Hard delete** (`deleteById`), not soft delete.
- **No Liquibase XML authored** — schema via `mvn liquibase:diff` only.
- `[P]` = different files, no incomplete dependencies. Commit after each task or logical group.
