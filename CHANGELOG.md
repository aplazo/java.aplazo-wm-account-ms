# Changelog

All notable changes to the **wm-account-ms** microservice are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-06-29

### Added
- **auth-hydra token revocation Feign client (BNPL-977, contract C-NEW-01).** Adds `AuthHydraClient`, a synchronous outbound client to auth-hydra `POST /hydra/revoke` (body `{ subject, clientId, externalReferenceId? }` → `{ sessionsRevoked }`). This is step ② of the Unlink chain and the PRIMARY security barrier: token revocation must succeed before any DB write (ADR-012 D-02/D-03, BNPL-955 §3.6/§5.4). Fail-fast by design — `Retryer.NEVER_RETRY` and a dedicated `AuthHydraErrorDecoder` map any non-2xx to a `500 HYDRA_REVOCATION_FAILED` so the Unlink aborts with no state change; timeouts propagate immediately with no retry. Authentication is service-to-service (IAM roles + CloudMap), so no app-level credentials are attached. Timeouts are configurable (`aplazo.url.api.auth-hydra.connect-timeout-ms` / `read-timeout-ms`, default 2s/5s).

### Changed
- Enabled `@EnableFeignClients` (scoped to `mx.aplazo.microservices.wm.account.feign`) on `WMAccountMsApp`.
- Bumped version to `1.1.0` across all modules (`pom.xml`, `wm-account-ms-client`, `wm-account-ms-service`).

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

[1.1.0]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/aplazo/java.aplazo-wm-account-ms/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/aplazo/java.aplazo-wm-account-ms/releases/tag/v1.0.0
