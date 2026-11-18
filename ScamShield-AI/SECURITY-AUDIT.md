# Security and QA Audit

Date: 2026-11-09

## Executive summary

Hardening and test infrastructure changes are complete. The npm dependency audit is clean and all 176 backend tests, 6 frontend tests, and the production frontend build pass. The audit is not fully clear: the resolved Spring MVC version is affected by two critical advisories with no patched Spring 6.2 release identified by the upstream advisories. Fixing those findings requires a major Spring Framework/Spring Boot upgrade, which was not applied or tested here.

## Findings and follow-ups

| Severity | Finding | Status |
|---|---|---|
| Critical | [CVE-2026-47884](https://github.com/advisories/GHSA-pc63-qcmh-9cmg) affects `spring-webmvc` 6.2.19 (XsltView path limitation). | Open. The upstream advisory lists no patched 6.2 version; the scanner recommends Spring 7.0.9 or later. Reachability through this application's routes was not established. |
| Critical | [CVE-2026-47890](https://github.com/advisories/GHSA-j9f9-w8pj-32f8) affects `spring-webmvc` 6.2.19 (SSE fragment rendering). | Open. The upstream advisory lists no patched 6.2 version; the scanner recommends Spring 7.0.9 or later. No SSE or WebFlux usage was found in the application source, but this does not remove the dependency advisory. |

Both findings require a Spring 7 / Spring Boot 4 migration to move to the scanner's patched version. That breaking upgrade needs a separate compatibility plan and full integration validation; it was not silently introduced during this targeted audit. These are affected-library findings, not proof that either vulnerable code path is reachable or exploitable in this deployment.

## Changes made

- Updated the Spring Boot parent to 3.5.16 and resolved patched dependency lines for Tomcat, Jackson, Logback, Log4j, and Commons Lang. The final resolved Spring Framework version remains 6.2.19.
- Made secure auth-cookie behavior the default (`AUTH_COOKIE_SECURE=true`). The example environment file retains `false` for local HTTP development only.
- Logout now increments and persists the user's token version before expiring the auth cookie, invalidating previously issued JWTs. Added tests for successful invalidation and for not clearing the cookie when persistence fails.
- Added a streaming 1 MiB response limit for the three Gemini integrations, with tests for both accepted and oversized bodies.
- Made notification rate-limit bucket updates atomic per user and periodically remove expired buckets. The limiter remains process-local and is not shared across multiple application instances.
- Added lazy loading and a Suspense boundary for routed frontend pages.
- Added Vitest/React Testing Library tests and ESLint configuration for frontend checks.

## Files changed

- Backend: `backend/pom.xml`, `backend/src/main/resources/application.properties`, `backend/src/main/java/com/scamshield/user/User.java`, `backend/src/main/java/com/scamshield/auth/AuthService.java`, `backend/src/main/java/com/scamshield/auth/AuthController.java`, `backend/src/main/java/com/scamshield/notifications/NotificationRateLimiter.java`, and the three Gemini service classes under `assistant`, `resume`, and `scamdetector`.
- New backend files: `backend/src/main/java/com/scamshield/common/LimitedBodyHandler.java`, plus `AuthControllerTest.java`, `LimitedBodyHandlerTest.java`, and `NotificationRateLimiterTest.java`.
- Frontend: `frontend/package.json`, `frontend/package-lock.json`, `frontend/src/routes/AppRoutes.jsx`, and new `frontend/eslint.config.js`, `frontend/vitest.config.js`, `frontend/src/test/setup.js`, `frontend/src/modules/admin/AdminRoute.test.jsx`, and `frontend/src/modules/admin/adminService.test.js`.
- Audit deliverable: `SECURITY-AUDIT.md`.

## Scan and test results

| Check | Result |
|---|---|
| Maven CVE scan of resolved runtime dependencies | 2 critical findings remain: the Spring MVC advisories above. Other previously identified patch-line dependency findings were cleared. |
| `npm audit` | 0 vulnerabilities across the lockfile. |
| npm advisory scan of direct dependencies | No known findings. |
| `mvn -f backend/pom.xml test` | Passed: 176 tests, 0 failures/errors. Runs on Java 26.0.2; the project targets Java 21, but a Java 21 runtime was unavailable for this validation. |
| `npm test` | Passed: 6 tests across 2 files. |
| `npm run build` | Passed. Route-level chunks are emitted; the former single 675.86 kB JS entry is now a 274.48 kB entry (87.49 kB gzip), a 59% reduction in entry size. This compares the initial entry, not the sum of all lazy-loaded assets. |
| ESLint on changed route and test files | Passed. |
| Full `npm run lint` | Fails on 21 existing errors and 2 warnings across earlier module files (including React effect/state patterns and unused values). No lint errors were reported in the changed route or newly added tests. |

## Runtime verification limits

There is no backend `.env` file and `MONGODB_URI` and `JWT_SECRET_BASE64` are not set in the environment. A TCP check found localhost port 27017 accepting connections, but the application could not be verified against an authenticated MongoDB connection or through live JWT-protected APIs without those settings. No API integration or two-user ownership test was run in this pass.

The existing security controls reviewed include signed JWT validation with token-version checks, owner-scoped data access, bounded resume parsing/storage, and URL scanning that does not fetch arbitrary submitted URLs. This code review and the tests are not a guarantee of security.

## Commands

```powershell
# Frontend
cd frontend
npm ci
npm test
npm run lint
npm run build

# Backend tests
cd ..\backend
mvn test
```

To start the backend, configure `MONGODB_URI`, `JWT_SECRET_BASE64`, and `AUTH_COOKIE_SECURE` for the target environment, then run `mvn spring-boot:run` from `backend`. Use `AUTH_COOKIE_SECURE=false` only for local HTTP development; use `true` behind HTTPS.
