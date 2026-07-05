# Quickstart & Validation Guide: Fitness Exercise Catalog

This guide validates the feature end-to-end **manually** (no automated tests — Principle VIII).
Refer to [contracts/fitness-exercise.openapi.yaml](./contracts/fitness-exercise.openapi.yaml) and [data-model.md](./data-model.md) for exact shapes.

## Prerequisites

- Java 11 + Maven, running `mgr-api` service with a reachable tenant DB.
- An OAuth2/JWT bearer token whose account holds the relevant roles: `EXERCISE_V`, `EXERCISE_L`, `EXERCISE_C`, `EXERCISE_U`, `EXERCISE_D`. Seed these role rows for a test admin before validating authorization behavior.

## Build & generate schema (manual — no XML authored)

From `mgr-api/source/mgr-api`:

```bash
mvn clean compile      # 1. compile — must succeed (primary quality gate)
mvn liquibase:diff     # 2. generate the changelog fragment from the new @Entity
```

Review the generated diff, then apply it via the project's normal Liquibase update step. **Do not hand-edit changelog XML.**

## Validation scenarios

Base path: `/v1/fitness-exercise`. All calls send `Authorization: Bearer <token>`.

1. **Create (US2)** — `POST /create` with a valid body (name, targetMuscleGroup, difficultyLevel=1, instructions).
   Expect `result=true`. Then confirm it appears via list/detail.
2. **Create — invalid difficulty (Edge/US2)** — `POST /create` with `difficultyLevel=4`.
   Expect rejection with a field-level validation error on `difficultyLevel` (from `@ValidDifficultyLevel`), no row persisted.
3. **Create — missing/blank name (Edge/US2)** — omit `name` or send `""`.
   Expect field-level `@NotEmpty` validation error.
4. **Create — oversized instructions (Edge)** — instructions > 2000 chars.
   Expect `@Size` validation error.
5. **Get by id (US1)** — `GET /get/{id}` for the created exercise.
   Expect full details (name, targetMuscleGroup, difficultyLevel, instructions, status).
6. **Get — not found (US1)** — `GET /get/{unknownId}`.
   Expect the standard NOT_FOUND error in the envelope (not an empty success).
7. **List — name filter + active-only (US1)** — `GET /list?name=push&page=0&size=10`.
   Expect a paginated envelope (`content`, `totalElements`, `totalPages`) containing only **active** exercises whose name contains "push". Set an exercise's `status` inactive and confirm it disappears from the list but is still retrievable by `GET /get/{id}`.
8. **Update (US3)** — `PUT /update` with existing `id` and changed fields.
   Expect changes reflected on a subsequent `GET /get/{id}`.
9. **Update — not found (US3)** — `PUT /update` with an unknown `id`.
   Expect NOT_FOUND error.
10. **Update — invalid difficulty (US3)** — `difficultyLevel` out of range → validation error.
11. **Delete (US4)** — `DELETE /delete/{id}` for an existing exercise.
    Expect success; subsequent `GET /get/{id}` and `list` no longer return it (hard delete).
12. **Delete — not found (US4)** — `DELETE /delete/{unknownId}` → NOT_FOUND error.
13. **Authorization (Edge/SC-004)** — repeat create/update/delete/get with a token **lacking** the corresponding role.
    Expect the operation refused (403) before any business logic runs.

## Success-criteria mapping

| Scenario(s) | Success Criterion |
|-------------|-------------------|
| 1, 5, 7 | SC-001, SC-005 |
| 2, 3, 4, 10 | SC-002 |
| all | SC-003 (uniform envelope) |
| 13 | SC-004 |

## Done signal

- `mvn clean compile` succeeds with no new warnings.
- Every scenario above behaves as described.
- Code review confirms: no Service layer bypasses any required logic; entity never crosses the API boundary; all errors flow through the existing `@ControllerAdvice`; every endpoint carries `@PreAuthorize` + Swagger annotations.
