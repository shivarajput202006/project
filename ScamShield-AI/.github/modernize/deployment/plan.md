# ScamShield AI — Module 15 integration and deployment-readiness plan

## Goal and scope

Perform a bounded final integration/readiness pass on the existing ScamShield AI repository. Preserve all implemented features and keep the application modules as-is. Inspect the React frontend, Spring Boot backend, provider configuration, existing security audit, and current tests. Make only narrowly justified integration, deployment-documentation, or configuration changes.

This is preparation only. Do not provision Azure or other cloud resources, deploy, publish images, generate duplicate application modules, introduce a new hosting architecture, or handle/commit secrets. No target Azure subscription, region, resource group, or database endpoint has been provided, so infrastructure generation and cloud deployment are out of scope.

## Repository findings informing this plan

- `frontend/` is one React/Vite SPA built with npm. `frontend/dist` is a build output and `frontend/node_modules` is installed locally; neither is source.
- `backend/` is one Java 21-targeted Spring Boot/Maven API. Its feature packages are in-process modules, not independent deployable services.
- MongoDB is required through `MONGODB_URI`; the example configuration describes MongoDB Atlas. No database credentials or live provider account were supplied.
- Gemini, Google Safe Browsing, and Adzuna are optional backend-only providers. Their values must be supplied through a deployment secret store/environment; no credentials are supplied.
- Frontend API URLs are compiled from `VITE_API_BASE_URL`; backend credentialed CORS uses the exact `FRONTEND_ORIGIN`.
- `SECURITY-AUDIT.md` already records two open critical Spring MVC advisories and known lint limitations. Do not attempt a framework major upgrade in this bounded pass.
- The runtime environment reports Java 26, whereas the backend targets Java 21. Record this as a verification limitation and do not claim Java 21 validation unless a Java 21 runtime is actually used.

## Planned files

1. `.github/modernize/deployment/plan.md` — this bounded plan and validation checklist.
2. `.github/modernize/deployment/progress.md` — task state, branch/VCS note, and real validation outcomes.
3. `DEPLOYMENT-READINESS.md` — concise release gates, configuration inventory, security/operational limits, and ordered manual deployment steps with secret-safe placeholders.
4. `README.md` — add a link to the deployment-readiness guide if a suitable location exists; otherwise do not duplicate its content.
5. `.gitignore` — exclude Maven's generated `backend/target/` output, which was not covered by the existing ignore rules.
6. `.github/modernize/deployment/summary.md` — completion summary after all checks.

No application source, existing feature implementation, provider key, database secret, generated distribution, or dependency version is to be changed in this scope.

## Execution sequence

1. Inspect integration contracts, build scripts, environment configuration, hosting/deployment artifacts, and the recorded security audit.
2. Record the repository/version-control status through the approved version-control tool if available. The workspace currently appears not to contain Git metadata; do not invoke direct Git commands.
3. Write the deployment-readiness guide based only on verified repository configuration. Clearly distinguish verified settings from values/resources that the operator must supply.
4. Run real frontend tests, lint, and production build; run real backend tests and package build. Record exact outcomes, counts where available, and runtime limitations.
5. Reinspect the final documentation and changed-file list. Do not deploy or claim live service/database validation.
6. Write the completion summary and progress file with final outcomes.

## Required validation

- Frontend tests: `npm test` from `frontend`.
- Frontend lint: `npm run lint` from `frontend`. Report existing failures rather than broadening the task into a cleanup pass.
- Frontend production build: `npm run build` from `frontend`.
- Backend tests: `mvn test` from `backend`.
- Backend package: `mvn -DskipTests package` from `backend` (only after test result is recorded).
- Documentation/config review: verify the guide does not contain real secrets, advertises a secure HTTPS cookie setting for production, documents build-time frontend API configuration, exact CORS origin configuration, database/provider prerequisites, SPA route fallback, and known release blockers.

## Out of scope / release gates

- Cloud resource provisioning, authentication to Azure, deployment, DNS/TLS provisioning, quota/pricing checks, and live endpoint verification.
- Replacing or upgrading Spring Boot/Framework to remediate the two open critical Spring MVC findings. This needs a separate compatibility and security-validation task.
- Database/provider provisioning or verification; the caller must supply valid production resources and credentials through secure channels.
- Java 21 runtime validation if only Java 26 is available.
- Fixing existing frontend lint errors outside this integration scope.
- Adding a new public health endpoint/Actuator dependency. The existing system-health API is under `/api/admin/system/health` and requires an administrator; do not present it as an unauthenticated platform health probe.
