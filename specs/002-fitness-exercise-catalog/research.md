# Phase 0 Research: Fitness Exercise Catalog (CRUD)

All Technical Context unknowns are resolved against the **existing `mgr-api` codebase conventions** (the authoritative reference is the `Category` feature slice). No open `NEEDS CLARIFICATION` items remain.

## Decision 1 — Feature slice shape (no Service layer)

- **Decision**: Implement the feature as Controller → Repository directly, plus MapStruct Mapper, JPA entity, Criteria, Forms, and a response DTO. No Service class/interface.
- **Rationale**: Caller-approved deviation from Principle II (see plan.md Complexity Tracking). Operations are thin standard CRUD with no cross-entity business logic; the existing `CategoryController` already calls `CategoryRepository` and even `@Transactional`-annotates its `delete` at the controller level, so this is consistent with observed patterns.
- **Alternatives considered**: Full `Service` + `ServiceImpl` layering (strict Principle II) — rejected as empty pass-through indirection for this feature.

## Decision 2 — Difficulty level modeling & custom validation

- **Decision**: `difficultyLevel` is a non-null `Integer` restricted to a fixed set: **Beginner=1, Intermediate=2, Advanced=3**, added to `MgrConstant` as `DIFFICULTY_LEVEL_BEGINNER/INTERMEDIATE/ADVANCED`. Enforced by a **custom constraint** `@ValidDifficultyLevel` (in `validation/`) + `DifficultyLevelValidator` (in `validation/impl/`), mirroring the existing `@ValidPostType` / `PostTypeValidator` pair.
- **Rationale**: Matches the spec's Implementation Constraints (custom annotation + impl referencing shared constants) and the established validator pattern in the codebase.
- **Alternatives considered**: A Java `enum` — rejected because the codebase models such fixed sets as integer constants validated by custom annotations (e.g., post type, gender, nation type), and the spec mandates an integer-backed set in shared constants.
- **Constant-class note**: The spec names `BaseConstant`; the actual shared-constants class here is **`com.mgr.api.constant.MgrConstant`**. Constants are added there.

## Decision 3 — Name uniqueness (clarified: NOT unique)

- **Decision**: Do **not** enforce name uniqueness. Unlike `CategoryController` (which calls `findFirstByName` and rejects duplicates), the create/update flows here perform **no** name-existence check, and the `name` column carries **no** unique constraint.
- **Rationale**: Clarification session 2026-07-04 — names may repeat; only the identifier is unique.
- **Alternatives considered**: Case-insensitive uniqueness — rejected per clarification.

## Decision 4 — List filtering & default visibility (clarified)

- **Decision**: `FitnessExerciseCriteria` exposes a single user-facing filter: `name` (case-insensitive `LIKE %name%`). The list endpoint additionally **forces `status = STATUS_ACTIVE`** so only active/visible exercises are returned by default. Pagination via Spring `Pageable`; response wrapped in `ResponseListDto`.
- **Rationale**: Clarifications — list is name-only and returns active exercises only. Detail-by-id (`get`) is NOT status-restricted.
- **Alternatives considered**: Multi-field/status filters and returning all statuses — rejected per clarifications.

## Decision 5 — Delete semantics (clarified: hard delete)

- **Decision**: `delete` performs a **hard delete** (`repository.deleteById`) after a not-found check. Method is `@Transactional` on the controller, mirroring `CategoryController.delete`. No child/dependency checks (the entity has no relationships in v1).
- **Rationale**: Clarification — hard delete, permanent row removal. The inherited `status` field governs visibility of still-existing rows, independent of deletion.
- **Alternatives considered**: Soft delete via status flip — rejected per clarification.

## Decision 6 — Permissions / role naming

- **Decision**: Guard endpoints with `@PreAuthorize("hasRole('<ROLE>')")` using the codebase's per-operation role convention (e.g., `CAT_V/_C/_U/_D/_L`). For this feature use prefix **`EXERCISE`**: `EXERCISE_V` (get detail), `EXERCISE_L` (list), `EXERCISE_C` (create), `EXERCISE_U` (update), `EXERCISE_D` (delete).
- **Rationale**: The spec maps operations to distinct `_C/_U/_D/_V` suffixes; the codebase additionally uses a separate `_L` for list (see `CategoryController`). Following the concrete codebase convention avoids collapsing list and detail into one permission.
- **Alternatives considered**: A single `_V` for both list and detail — rejected to match the existing `_V` + `_L` split; role rows are seeded by the developer/DBA outside this feature's Java code.

## Decision 7 — Entity base, table, auditing, multi-tenancy

- **Decision**: `FitnessExercise extends Auditable<String>` (supplies `id`, `createdBy/Date`, `modifiedBy/Date`, `status=1` default). Table `db_mgr_fitness_exercise` via `TablePrefix.PREFIX_TABLE`. Repository under `repository/tenant` (tenant datasource). `@EntityListeners(AuditingEntityListener.class)`.
- **Rationale**: Inherits the application's base-entity, auditing, and multi-tenant conventions exactly as `Category` does; the spec assumptions defer these to existing conventions.
- **Alternatives considered**: none — dictated by codebase conventions.

## Decision 8 — Schema via liquibase:diff (no XML authored)

- **Decision**: Author only `FitnessExercise.java`. Generate schema manually with `mvn clean compile` then `mvn liquibase:diff`. No `*.xml` changelog is written or edited by implementation.
- **Rationale**: Caller instruction + spec Implementation Constraints; still routes schema through Liquibase per constitution Tech Stack section.
- **Alternatives considered**: Hand-written changelog XML — explicitly prohibited.

## Field sizing (clarified)

| Field | Type | Constraint |
|-------|------|-----------|
| `name` | `VARCHAR(255)` | non-blank, ≤ 255 |
| `targetMuscleGroup` | `VARCHAR(255)` | non-blank (descriptive text, v1) |
| `difficultyLevel` | `INT` | one of {1,2,3} via `@ValidDifficultyLevel` |
| `instructions` | `TEXT` (or `VARCHAR(2000)`) | non-blank, ≤ 2000 |
| `status` | `INT` | inherited from `Auditable` (default 1 = active) |
