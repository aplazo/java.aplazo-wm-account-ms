# Changelog

All notable changes to the **wm-account-ms** microservice are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-06-30

### Added
- **Unlink shared-operation contract and persistence layer (BNPL-960).** Lays the reusable core that BNPL-886 will implement on top of:
  - **Client module (API contract):** `WmAccountUnlinkClient` (`POST /wm/account/unlink`, C-02 — the `/internal/` prefix dropped per ADR-012 D-12, auth `ROLE_API` per D-13), `UnlinkAccountRequest` / `UnlinkAccountResponse` DTOs, and the contract enums `UnlinkReason`, `UnlinkSource`, `UnlinkInitiatedBy` (BNPL-955 §3.2).
  - **Persistence (implemented):** `CustomerWmUnlinkRecord` JPA entity mapping `wm_integration.customer_wm_unlink_record` (ADR-012 D-04, one row per Unlink event) and `CustomerWmUnlinkRecordRepository`, including the most-recent-not-yet-relinked finder (C-NEW-03 target, BNPL-955 §3.8).
  - **Interfaces (definition only — implemented under BNPL-886):** `UnlinkService` core contract; `WmAccountUnlinkController` skeleton wiring the C-02 handler to the contract (throws `UnsupportedOperationException` until the core lands); `PartnersUnlinkNotifyClient` outbound contract for the post-Unlink notification (C-NEW-02, `POST /wm/unlink/notify`) with its DTOs; and the canonical `UnlinkErrorCode` set.
- The Unlink business logic itself (state guard → sync auth-hydra revoke → atomic DB write → sync partners-ms notify) is **not** included here — it is delivered by BNPL-886. The schema migration for `customer_wm_unlink_record` is owned by P4/DBA.

### Changed
- Bumped version to `1.2.0` across all modules (`pom.xml`, `wm-account-ms-client`, `wm-account-ms-service`).

## [1.1.0] - 2026-06-29

### Added
- **auth-hydra token revocation Feign client (BNPL-977, contract C-NEW-01).** Adds `AuthHydraClient`, a synchronous outbound client to auth-hydra `POST /hydra/revoke` (body `{ subject, clientId, externalReferenceId? }` → `{ sessionsRevoked }`). This is step ② of the Unlink chain and the PRIMARY security barrier: token revocation must succeed before any DB write (ADR-012 D-02/D-03, BNPL-955 §3.6/§5.4). Fail-fast by design — `Retryer.NEVER_RETRY` and a dedicated `AuthHydraErrorDecoder` map any non-2xx to a `500 HYDRA_REVOCATION_FAILED` so the Unlink aborts with no state change; timeouts propagate immediately with no retry. Authentication is service-to-service (IAM roles + CloudMap), so no app-level credentials are attached. Timeouts are configurable (`aplazo.url.api.auth-hydra.connect-timeout-ms` / `read-timeout-ms`, default 2s/5s).

### Changed
- Enabled `@EnableFeignClients` (scoped to `mx.aplazo.microservices.wm.account.feign`) on `WMAccountMsApp`.
- Bumped version to `1.1.0` across all modules (`pom.xml`, `wm-account-ms-client`, `wm-account-ms-service`).

## [1.0.3] - 2026-06-25

### Added
- **Local development environment (`docker-compose.yml`).** Single-command local setup via `docker compose up -d`. Includes:
  - **PostgreSQL 14** (`localhost:5432`) — database (`wm_account_db` / `wm_account_user` / `wm_account_pass`).
  - **LocalStack 3** (`localhost:4566`) — AWS SQS emulator. The queue `wm_customer_account_changes_local` is created automatically on startup via `scripts/localstack/init-sqs.sh`.
- **`application-local.yml`** fully configured with hardcoded values that match the docker-compose services. No environment variables required to start locally — just `docker compose up -d` and run with `-Dspring.profiles.active=local`.
- **`run-local.sh`** — convenience script that starts the docker-compose stack, waits for services to be healthy, exports the LocalStack AWS credentials, and runs the Spring Boot application with the `local` profile.

### Changed
- `application-api.yml` — added fallback value for `queueName` (`wm_customer_account_changes_local`) so the application does not crash on startup when `API_APLAZO_CUSTOMER_ACCOUNT_CHANGES_SQS_QUEUE_NAME` is not set.

### Changed
- **`CustomerAccountFrozenEventProcessor` — status-aware logging.** The processor now decodes the Base64-encoded JSON inner payload into `CustomerAccountFrozenPayload` and branches on `currentStatus`:
  - `BLOCKED` → emits a dedicated log line with `customerId` and `currentReason`.
  - `BANNED`  → logs `customerId` and the received status (read-only, no side-effects).
  - Any other value → logs the unhandled status for observability.
  Decode errors are caught and logged as warnings without propagating exceptions.
- Added `CustomerAccountFrozenPayload` POJO (`customerId`, `currentStatus`, `currentReason`, `previousStatus`, `previousReason`) for inner payload deserialization.
- Added `FROZEN_STATUS_BLOCKED` and `FROZEN_STATUS_BANNED` constants to `CustomerAccountEventConstants`.
- Renamed `setUp()` to `initListener()` in `CustomerAccountEventsListenerTest` to avoid conflict with the `@BeforeAll static setUp()` in `AbstractAplazoUnitTest`.
- Expanded `CustomerAccountFrozenEventProcessorTest` with dedicated test cases for `BLOCKED`, `BANNED`, unknown status, and invalid/non-JSON payloads.

## [1.0.2] - 2026-06-24

### Fixed
- **Dockerfile `ENTRYPOINT` with invalid JSON (BNPL-958).** The exec-form `ENTRYPOINT` contained a double comma (`,,`) that invalidated the JSON array. Docker could not parse the exec form and fell back to shell form, so the container failed to start. This caused ECS tasks (`cl-aplazostg` / `svc-wm-account-ms-stg`) to fail to start repeatedly, exceeding the deployment circuit breaker and triggering a failed rollback in the Staging pipeline.

### Changed
- Bumped version to `1.0.2` across all modules (`pom.xml`, `wm-account-ms-client`, `wm-account-ms-service`).

## [1.0.1] - 2026-06-23

### Fixed
- Unblocked OpenAPI/Swagger generation on the scaffold (BNPL-958).

### Removed
- Removed unused scaffold artifacts that blocked OpenAPI generation: placeholder fields in `WMAccountMsRequest` and `WMAccountMsResponse`, and the `WMAccountMsClient` Feign interface.

### Changed
- Adjusted `WMAccountMsServiceClient` and `application-test.yml` accordingly.
- Bumped version to `1.0.1` across all modules.

## [1.0.0] - 2026-06-17

### Added
- Initial project structure for the **wm-account-ms** microservice (multi-module Maven: `wm-account-ms-client`, `wm-account-ms-service`).
- Infrastructure set up (BNPL-958): Dockerfile, Jenkins pipeline (`jenkins/Jenkinsfile.yaml`), and base configuration.

[1.2.0]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.2...v1.1.0
[1.0.3]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/aplazo/java.aplazo-wm-account-ms/releases/tag/v1.0.0
