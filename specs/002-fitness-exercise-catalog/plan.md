# Implementation Plan: Fitness Exercise Catalog (CRUD)

**Branch**: `002-fitness-exercise-catalog` | **Date**: 2026-07-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-fitness-exercise-catalog/spec.md`

## Summary

Deliver full CRUD for a `FitnessExercise` catalog (name, target muscle group, difficulty level, step-by-step instructions, activation status) inside the existing `mgr-api` Spring Boot service. The catalog is browsed via a paginated, **name-only** (partial/contains) list that returns **active exercises only**, read one-by-one by identifier, and managed (create/update/hard-delete) by authorized administrators. All input is Bean-Validated (with a **custom `difficultyLevel` constraint** restricting values to the Beginner/Intermediate/Advanced constant set), all responses use the standard `ApiMessageDto<T>` envelope, all errors flow through the existing centralized handler, and every endpoint is `@PreAuthorize`-guarded.

**Technical approach**: Mirror the existing `Category` feature slice (Controller + Repository + MapStruct Mapper + JPA entity + Criteria + Forms + DTO), with one deliberate deviation approved by the caller: **no Service layer** — the Controller calls the Repository directly for these standard CRUD operations (recorded in Complexity Tracking). Schema is produced by the Liquibase **diff** workflow (`mvn clean compile` → `mvn liquibase:diff`), not hand-written changelog XML.

## Technical Context

**Language/Version**: Java 11 (as declared in `mgr-api/source/mgr-api/pom.xml`)

**Primary Dependencies**: Spring Boot 2.3.0.RELEASE (Spring MVC, Spring Data JPA/Hibernate, Spring Validation, Spring Security/OAuth2/JWT), MapStruct, Lombok, Springfox Swagger 2, Liquibase

**Storage**: Relational DB via Spring Data JPA + Hibernate; schema migrations via Liquibase (multi-tenant `tenant` datasource — repository lives under `repository/tenant`)

**Testing**: PROHIBITED — automated testing (unit, integration, e2e, mock, or any other) is forbidden by the constitution (Principle VIII, NON-NEGOTIABLE). Verification is via code review, `mvn clean compile`, and manual/exploratory API testing only.

**Target Platform**: Linux server (containerized Spring Boot backend service)

**Project Type**: Single backend web service (`mgr-api`)

**Performance Goals**: Standard interactive CRUD latency; create-then-read visible within one session (SC-001, < 1 minute). No special throughput target.

**Constraints**: Reuse existing auth stack, response envelope, base entity, and centralized exception handler; stay on Java 11 + Spring Boot 2.3.0.RELEASE; no new dependencies; author only Java entity classes (schema generated via `liquibase:diff`).

**Scale/Scope**: Small catalog feature — one entity, five endpoints (get, list, create, update, delete). No taxonomy/reference tables (muscle group is descriptive text for v1).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Verified against `.specify/memory/constitution.md` (on-disk **v1.0.0**):

- **I. Clean Code & SOLID**: ✅ Constructor/field injection per existing convention; MapStruct removes manual copy; single-purpose controller methods mirroring `CategoryController`.
- **II. Layered Architecture**: ⚠️ **DEVIATION (approved)** — No Service layer; Controller calls Repository directly for standard CRUD. Justified & recorded in **Complexity Tracking** below. This is the reconciliation of the spec's cited constitution v1.1.0 (no Service layer) against on-disk v1.0.0 (Principle II mandates a Service layer); the caller has explicitly authorized the exception for this feature.
- **III. RESTful API & DTOs**: ✅ Resource URLs under `/v1/fitness-exercise`; JPA entity never crosses the API boundary — request `Form`s in, `FitnessExerciseDto` out, converted by MapStruct `@Mapper`.
- **IV. Validation & Centralized Exception Handling**: ✅ `javax.validation` on Forms + a **custom `@ValidDifficultyLevel`** constraint; errors raised as existing typed exceptions (`NotFoundException`, `BadRequestException`) handled by the existing `@ControllerAdvice`. No ad-hoc try/catch in the controller.
- **V. Consistent Responses & OpenAPI**: ✅ Every endpoint returns `ApiMessageDto<T>`; Forms/DTO carry Swagger `@ApiModel`/`@ApiModelProperty` annotations.
- **VI. Security**: ✅ Reuses Spring Security; each endpoint `@PreAuthorize`-guarded by a distinct role; no secrets, no sensitive data in responses/logs; JPA parameterized queries only.
- **VII. Minimal Dependencies & Spring Boot-First**: ✅ No new dependencies; everything used is already on the classpath; stays on Java 11 / Spring Boot 2.3.0.RELEASE.
- **VIII. No Automated Testing (NON-NEGOTIABLE)**: ✅ No test tasks, files, or infrastructure. Verification = compile + code review + manual API testing.

**Gate result**: **PASS** — the single Principle II deviation is explicitly justified in Complexity Tracking; all other principles are satisfied.

## Project Structure

### Documentation (this feature)

```text
specs/002-fitness-exercise-catalog/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── fitness-exercise.openapi.yaml
├── checklists/
│   └── requirements.md  # Spec quality checklist (already present)
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

All paths under `mgr-api/source/mgr-api/src/main/java/com/mgr/api/`:

```text
model/
├── FitnessExercise.java                 # NEW — JPA entity extends Auditable<String>
└── criteria/
    └── FitnessExerciseCriteria.java     # NEW — name (+status) Specification builder

form/fitnessExercise/
├── CreateFitnessExerciseForm.java       # NEW — validated create payload
└── UpdateFitnessExerciseForm.java       # NEW — validated update payload (id required)

dto/fitnessExercise/
└── FitnessExerciseDto.java              # NEW — response DTO extends ABasicAdminDto

mapper/
└── FitnessExerciseMapper.java           # NEW — MapStruct @Mapper (spring component)

repository/tenant/
└── FitnessExerciseRepository.java       # NEW — JpaRepository + JpaSpecificationExecutor

controller/
└── FitnessExerciseController.java       # NEW — extends ABasicController; 5 endpoints

validation/
└── ValidDifficultyLevel.java            # NEW — custom constraint annotation
validation/impl/
└── DifficultyLevelValidator.java        # NEW — ConstraintValidator implementation

constant/
└── MgrConstant.java                     # EDIT — add DIFFICULTY_LEVEL_* constants
dto/
└── ErrorCode.java                       # EDIT — add FITNESS_EXERCISE_* error codes
```

> **Note on `BaseConstant`**: the spec's Implementation Constraints refer to "`BaseConstant`". The actual shared-constants class in this codebase is **`com.mgr.api.constant.MgrConstant`**; the difficulty-level constants are added there. (Recorded in research.md.)

**Structure Decision**: Single existing backend module (`mgr-api`). The feature is a horizontal slice added to the established `model` / `criteria` / `form` / `dto` / `mapper` / `repository/tenant` / `controller` / `validation` package layout, mirroring the `Category` feature — minus the Service layer per the approved deviation.

## Complexity Tracking

> Records the one Constitution Check deviation and its justification so the gate PASSes.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| **Principle II (Layered Architecture)** — omit the Service layer; Controller (`FitnessExerciseController`) calls `FitnessExerciseRepository` directly. | Explicitly requested by the caller for this feature (and mandated by the cited Spring Boot Backend Constitution v1.1.0). These are thin, standard CRUD operations with **no cross-entity business logic, no transaction orchestration across repositories, and no reuse from other callers** — a Service layer would be a pass-through delegating 1:1 to the repository, adding a class and an interface with zero behavior. Write endpoints keep their transactional boundary via `@Transactional` on the controller method (as the existing `delete` in `CategoryController` already does). | Adding a `FitnessExerciseService` + `impl` (the strict Principle II layering) was rejected because it introduces empty indirection with no business logic to host, increasing surface area to review/maintain without improving correctness, decoupling, or reuse for this specific feature. The deviation is **scoped to this feature only** and does not change the project-wide default. |

## Liquibase / Schema Workflow (no hand-written XML)

Per the caller's explicit instruction and the spec's Implementation Constraints, **no Liquibase changelog XML is authored by this plan or by implementation**. Only the Java entity class (`FitnessExercise.java`) is written. The developer generates the schema **manually**, in order:

```bash
# from mgr-api/source/mgr-api
mvn clean compile      # compile bytecode so Hibernate can read the new @Entity
mvn liquibase:diff     # diff JPA model vs DB → generates the changelog fragment
```

This still satisfies the constitution's rule that "all schema changes MUST go through a Liquibase changelog" — the changelog is produced by `liquibase:diff` rather than hand-authored. Implementation tasks MUST NOT create or edit any `*.xml` changelog file directly.
