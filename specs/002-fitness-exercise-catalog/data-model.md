# Phase 1 Data Model: Fitness Exercise Catalog

## Entity: `FitnessExercise`

- **Package**: `com.mgr.api.model`
- **Base class**: `Auditable<String>` (provides `id`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`, `status`)
- **Table**: `db_mgr_fitness_exercise` (`TablePrefix.PREFIX_TABLE + "fitness_exercise"`)
- **Listeners**: `@EntityListeners(AuditingEntityListener.class)`
- **Datasource**: tenant (repository under `repository/tenant`)

### Fields

| Field | Java type | Column | DB type | Nullable | Notes |
|-------|-----------|--------|---------|----------|-------|
| `id` | `Long` | `id` | BIGINT (PK) | no | Inherited; generated via `IdGenerator` |
| `name` | `String` | `name` | `VARCHAR(255)` | no | **Not unique** (names may repeat). Non-blank, ≤ 255 |
| `targetMuscleGroup` | `String` | `target_muscle_group` | `VARCHAR(255)` | no | Descriptive text for v1 (no taxonomy entity). Non-blank |
| `difficultyLevel` | `Integer` | `difficulty_level` | `INT` | no | One of {1=Beginner, 2=Intermediate, 3=Advanced} |
| `instructions` | `String` | `instructions` | `TEXT` | no | Step-by-step guidance. Non-blank, ≤ 2000 |
| `status` | `int` | `status` | `INT` | no | Inherited; default `1` (active). `-2` etc. per `MgrConstant` |
| `createdBy` / `createdDate` / `modifiedBy` / `modifiedDate` | audit | audit cols | — | no | Inherited from `Auditable` |

### Validation rules (enforced on request Forms, not the entity)

- `name`: `@NotEmpty`, `@Size(max = 255)`
- `targetMuscleGroup`: `@NotEmpty`, `@Size(max = 255)`
- `difficultyLevel`: `@NotNull` + **custom `@ValidDifficultyLevel`** (value ∈ {1,2,3})
- `instructions`: `@NotEmpty`, `@Size(max = 2000)`
- Update form additionally requires `id` (`@NotNull`)

### Relationships

- **None** in v1. `targetMuscleGroup` is free text; no managed muscle-group reference entity (explicitly out of scope).

### Lifecycle / state

- `status` (inherited): `1` = active/visible (default). Inactive/hidden values excluded from the list endpoint.
- **Deletion**: hard delete (row removed). No optimistic locking — last valid write wins (v1).

## Difficulty-level constants (added to `MgrConstant`)

```java
public static final int DIFFICULTY_LEVEL_BEGINNER     = 1;
public static final int DIFFICULTY_LEVEL_INTERMEDIATE = 2;
public static final int DIFFICULTY_LEVEL_ADVANCED     = 3;
```

## Criteria: `FitnessExerciseCriteria`

- **Package**: `com.mgr.api.model.criteria`
- Fields: `name` (String), `status` (Integer)
- `getSpecification()` predicates:
  - if `name != null` → `LIKE lower(name) %name%`
  - if `status != null` → `equal(status)`
- The **list endpoint sets `criteria.setStatus(MgrConstant.STATUS_ACTIVE)`** before building the specification, so only active exercises are returned regardless of client input.

## DTO: `FitnessExerciseDto`

- **Package**: `com.mgr.api.dto.fitnessExercise`; extends `ABasicAdminDto` (`id`, `status`, `createdDate`, `modifiedDate`)
- Adds: `name`, `targetMuscleGroup`, `difficultyLevel`, `instructions`

## Forms

**`CreateFitnessExerciseForm`** (`form/fitnessExercise`): `name`, `targetMuscleGroup`, `difficultyLevel`, `instructions` (all validated as above).

**`UpdateFitnessExerciseForm`** (`form/fitnessExercise`): `id` (`@NotNull`) + same fields as create.

## Mapper: `FitnessExerciseMapper`

- MapStruct `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`
- `fromCreateFormToEntity(CreateFitnessExerciseForm)` → `FitnessExercise` (`@BeanMapping(ignoreByDefault = true)`, map the 4 business fields)
- `fromEntityToDto(FitnessExercise)` → `FitnessExerciseDto` (map business fields + id/status/createdDate/modifiedDate)
- `fromEntityToDtoList(List<FitnessExercise>)` → `List<FitnessExerciseDto>`
