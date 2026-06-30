# Changelog

All notable changes to the **wm-account-ms** microservice are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[1.0.3]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/aplazo/java.aplazo-wm-account-ms/releases/tag/v1.0.0
