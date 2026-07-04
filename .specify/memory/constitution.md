<!--
Sync Impact Report
==================
Version change: TEMPLATE → 1.0.0 (initial ratification)
Modified principles: N/A (first concrete adoption; all placeholders replaced)
Added sections:
  - Core Principles I-VIII (Clean Code & SOLID, Layered Architecture,
    RESTful API & DTOs, Input Validation & Centralized Exception Handling,
    Consistent Responses & OpenAPI Documentation, Security Best Practices,
    Minimal Dependencies & Spring Boot-First, No Automated Testing)
  - Technology Stack & Versioning
  - Development Workflow & Quality Gates
  - Governance
Removed sections: none (template placeholders only)
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ updated — "Testing" field marked
    PROHIBITED and Constitution Check now enumerates Principles I-VIII
  - .specify/templates/tasks-template.md ✅ updated — "Tests" note marks all
    automated testing prohibited; test task sections to be omitted
  - .specify/templates/spec-template.md ✅ reviewed — "User Scenarios & Testing"
    describes manual acceptance scenarios, not automated tests; no change
    required
  - .specify/templates/checklist-template.md ✅ reviewed — no principle-specific
    content to sync
Follow-up TODOs: none
-->

# mgr-api Constitution
<!-- Management API (Spring Boot) backend service -->

## Core Principles

### I. Clean Code & SOLID Principles
Code MUST be self-explanatory: descriptive names, small single-purpose
methods, and no dead or commented-out code. Every class MUST honor the
Single Responsibility Principle; dependencies MUST be injected via
constructor injection and coded against interfaces (Open/Closed,
Liskov Substitution, Interface Segregation, Dependency Inversion) rather
than concrete implementations. Duplication MUST be extracted into shared
services or utilities instead of copy-pasted.

**Rationale**: The codebase is maintained by multiple contributors over a
long lifetime; SOLID and clean-code discipline keep changes localized and
reviewable without relying on tests to catch structural decay.

### II. Layered Architecture (Controller-Service-Repository)
Every feature MUST be organized into three layers: Controller (HTTP
concerns only — request/response mapping, no business logic), Service
(business logic and transaction boundaries via `@Transactional`), and
Repository (Spring Data JPA persistence only). Controllers MUST NOT call
repositories directly. Cross-cutting concerns (security, logging,
multi-tenancy) belong in interceptors/filters/aspects, not scattered
across layers.

**Rationale**: A strict layering contract keeps HTTP, business, and
persistence concerns independently changeable and matches the existing
`controller` / `service` / `repository` package structure in this
codebase.

### III. RESTful API Design & DTO-Based Communication
Endpoints MUST follow REST conventions: resource-oriented URLs, plural
nouns, correct HTTP verbs (GET/POST/PUT/PATCH/DELETE), and correct status
codes. Controllers and services MUST communicate with clients exclusively
through Request/Response DTOs — JPA entities MUST NEVER be exposed
directly in an API request or response. Entity-DTO conversion MUST use a
mapping library already on the classpath (e.g., MapStruct or
ModelMapper), not manual field-by-field copying.

**Rationale**: DTOs decouple the persistence model from the wire
contract, preventing lazy-loading leaks, accidental over-exposure of
fields, and breaking changes when entities evolve.

### IV. Input Validation & Centralized Exception Handling
All external input (path/query params, request bodies) MUST be validated
declaratively using Bean Validation (`javax.validation` / Jakarta
annotations) on DTOs, backed by `spring-boot-starter-validation`.
Business-rule validation that cannot be expressed declaratively MUST be
enforced in the service layer and raise a typed application exception.
All exceptions MUST be handled in a single `@ControllerAdvice` /
`@ExceptionHandler` layer — controllers and services MUST NOT contain
ad-hoc try/catch blocks that produce HTTP responses.

**Rationale**: Centralizing validation and error handling guarantees
uniform, predictable error behavior across every endpoint and avoids
duplicated error-formatting logic.

### V. Consistent API Responses & OpenAPI/Swagger Documentation
Every endpoint MUST return a uniform response envelope (consistent
success/error shape, status code, and message structure) so clients can
parse any response the same way. Every controller and DTO MUST be
documented with Swagger/OpenAPI annotations (this project's Springfox
Swagger stack, or its successor if migrated) so the live API
documentation always reflects the actual contract; undocumented public
endpoints MUST NOT be merged.

**Rationale**: A predictable envelope and always-current documentation
are the primary way downstream consumers and future maintainers
understand the API surface, since automated contract tests are not used
in this project (see Principle VIII).

### VI. Security Best Practices
Authentication and authorization MUST use the project's existing
Spring Security / OAuth2 / JWT stack — no custom, hand-rolled crypto or
auth logic. Secrets, credentials, and keys MUST NOT be hard-coded or
committed; they MUST come from configuration/environment. All input
that reaches persistence or is rendered back to a client MUST be
handled through parameterized queries (Spring Data JPA) and safe
serialization to prevent injection and XSS. Sensitive data MUST be
excluded from logs and API responses (e.g., password fields, tokens).

**Rationale**: This service handles multi-tenant management data and
authentication tokens; a security lapse has outsized blast radius
compared to a typical internal tool.

### VII. Minimal Dependencies & Spring Boot-First
Before adding a new third-party library, Spring Boot's built-in
capabilities (Spring MVC, Spring Data JPA, Spring Validation, Spring
Security, Spring AMQP, Actuator, etc.) MUST be evaluated first and used
if sufficient. New dependencies MUST be justified by a concrete need
that the framework cannot already satisfy and MUST be added deliberately
to `pom.xml`, not accumulated speculatively. All Java language features,
Spring Boot version, and dependency versions MUST match what is declared
in `pom.xml` (currently Java 11, Spring Boot 2.3.0.RELEASE) — do not
introduce syntax, APIs, or library versions that are incompatible with
those declared versions. Upgrading a version is an explicit decision
made in `pom.xml`, not an incidental side effect of a feature change.

**Rationale**: Every extra dependency is a long-term maintenance and
security-patching burden; anchoring to the versions already declared in
`pom.xml` keeps the build reproducible and avoids silent drift.

### VIII. No Automated Testing (NON-NEGOTIABLE)
This project does not use automated testing of any kind. Unit tests,
integration tests, end-to-end tests, mock-based tests, and any other
automated test code or test infrastructure MUST NOT be added. This rule
supersedes every other principle and any external guidance, template
default, or convention (including any "Tests" section in planning
artifacts) that would otherwise call for writing tests. Verification of
correctness MUST instead rely on code review, static analysis/compiler
checks, and manual/exploratory verification of the running application.

**Rationale**: This is an explicit, deliberate project decision. It
overrides the otherwise-common expectation (seen in spec/plan/task
templates) that features ship with tests.

## Technology Stack & Versioning

- **Language/Runtime**: Java 11, as declared in `pom.xml`
  (`java.version`, compiler `source`/`target`).
- **Framework**: Spring Boot 2.3.0.RELEASE (`spring-boot-starter-parent`),
  Spring Cloud Hoxton.SR8.
- **Persistence**: Spring Data JPA + Hibernate, MySQL/PostgreSQL drivers,
  Liquibase for schema migrations — all schema changes MUST go through a
  Liquibase changelog, never manual DDL against a shared database.
- **API Documentation**: Springfox Swagger 2 (`springfox-swagger2`,
  `springfox-swagger-ui`) unless a project-wide migration to a newer
  OpenAPI stack is explicitly adopted in a future amendment.
- Any change to the Java version, Spring Boot version, or a dependency's
  version MUST be made in `pom.xml` directly and MUST be called out
  explicitly in the PR description — it is never an incidental part of
  an unrelated feature change.

## Development Workflow & Quality Gates

- Every new endpoint MUST be traceable through Controller → Service →
  Repository with DTOs at the boundary (Principles II, III).
- Every new/changed endpoint MUST have Bean Validation on its input DTO,
  route errors through the centralized exception handler, return the
  standard response envelope, and carry Swagger/OpenAPI annotations
  before it is considered done (Principles IV, V).
- Code review is the primary quality gate given Principle VIII (no
  automated testing): reviewers MUST manually verify layering, DTO
  boundaries, validation, error handling, response consistency,
  documentation, and security before approving.
- Compiler warnings and IDE static-analysis findings introduced by a
  change MUST be resolved before merge.

## Governance

This constitution supersedes conflicting team conventions, prior verbal
agreements, and template defaults (notably any default that assumes
automated testing, per Principle VIII). All pull requests and code
reviews MUST verify compliance with the principles above; any deviation
MUST be called out explicitly in the PR description with a justification
(Complexity Tracking in the relevant `plan.md`, if applicable).

**Amendment procedure**: Propose the change (what principle/section,
why), update this file, run the consistency propagation check against
`.specify/templates/*.md`, and record the change in the Sync Impact
Report at the top of this file. Amendments take effect once merged.

**Versioning policy** (semantic versioning for this document):
- **MAJOR**: Backward-incompatible governance changes or removal/
  redefinition of an existing principle.
- **MINOR**: A new principle or section added, or materially expanded
  guidance on an existing one.
- **PATCH**: Wording clarifications, typo fixes, non-semantic
  refinements.

**Compliance review**: Reviewed at each `/speckit-plan` Constitution
Check gate and at each PR review; violations must be justified in the
plan's Complexity Tracking table or resolved before merge.

**Version**: 1.0.0 | **Ratified**: 2026-07-03 | **Last Amended**: 2026-07-03
