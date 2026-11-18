# ScamShield AI deployment readiness

## Status and scope

This is a deployment-preparation guide, not evidence of a deployed or production-approved service. No Azure account, subscription, resource group, region, production database endpoint, or live provider credentials were supplied. No cloud resources were created and no production integration test was attempted. The Maven test run did log a test-context connection to local `localhost:27017`; that is not verification of an authenticated or production database.

The verified application topology is one React/Vite single-page application (`frontend/`) and one Java 21-targeted Spring Boot/Maven API (`backend/`). Backend feature packages are in-process modules, not separate services. MongoDB is required; Gemini, Google Safe Browsing, and Adzuna integrations are optional. No Dockerfile, IaC, CI/CD workflow, or hosting configuration is present.

For a future Azure deployment, a reasonable topology to evaluate is Azure Static Web Apps for the built frontend, Azure App Service (Linux, Java 21) for the API, and a private, backed-up MongoDB deployment compatible with the application's MongoDB driver (the documented setup uses MongoDB Atlas). This is a recommendation, not an infrastructure decision: validate current regional availability, pricing, network access, and organizational policy before selecting resources. Do not substitute Cosmos DB or another database without compatibility testing.

## Release gates

1. **Resolve the recorded critical dependency findings.** `SECURITY-AUDIT.md` reports open critical findings CVE-2026-47884 and CVE-2026-47890 in the resolved Spring MVC 6.2.19 line. It notes that the advisories list no patched Spring 6.2 release and recommends Spring 7.0.9 or later. Reachability was not established, but these findings remain release blockers until a separate Spring 7 / Spring Boot 4 compatibility upgrade or a documented security exception is completed and validated.
2. **Build and test on Java 21.** The Maven project targets Java 21. Validation performed with Java 26 does not replace a Java 21 CI/release build.
3. **Supply and verify production dependencies.** Configure a real MongoDB URI and provider credentials only in the hosting platform's secret store or managed secret references. None are configured or verified in this workspace.
4. **Use HTTPS and align browser origins.** Set `AUTH_COOKIE_SECURE=true`, set `FRONTEND_ORIGIN` to the exact deployed frontend origin, and set `VITE_API_BASE_URL` to the HTTPS API URL before building the frontend. The backend permits credentialed CORS only for the configured exact origin. Auth cookies use `SameSite=Lax`; host frontend and API on same-site domains (for example, `app.example.com` and `api.example.com`). A truly cross-site frontend/API deployment will need an explicit cookie/CORS/CSRF design review before changing SameSite behavior.
5. **Account for operational limits.** The notification rate limiter is process-local and not shared across instances. No Spring Actuator or unauthenticated health probe is configured. `/api/admin/system/health` is an administrator-only endpoint and is not suitable as a public or anonymous platform probe. Define an approved probe/monitoring design before enabling platform health checks or horizontal scaling.
6. **Exercise production routes and persistence.** Verify SPA route fallback for direct navigation/refresh, browser cookie + CSRF behavior, database connectivity, backups/restore, and the optional provider behavior in the target environment.

## Configuration inventory

| Variable | Required? | Where it is used / deployment guidance |
|---|---|---|
| `MONGODB_URI` | Yes | Backend MongoDB connection; use a production database, least-privilege account, TLS, and network restrictions. Resume files and profile images are stored in MongoDB GridFS, so persistence and backup/restore matter. |
| `JWT_SECRET_BASE64` | Yes | Backend JWT signing secret; generate a random secret of at least 32 bytes and store only in a secret manager. Never reuse the example placeholder or commit a real value. |
| `AUTH_COOKIE_SECURE` | Yes for production | Set `true` behind HTTPS. The local example uses `false` only for local HTTP development. |
| `FRONTEND_ORIGIN` | Yes for deployed browser access | Exact origin including scheme and host, e.g. `https://app.example.com`; no path or trailing API route. |
| `VITE_API_BASE_URL` | Yes for production frontend build | Build-time Vite variable, e.g. `https://api.example.com`; it is compiled into frontend assets and is not a runtime secret. Without it, frontend services fall back to `http://localhost:8080`. |
| `GEMINI_API_KEY` | Optional | Enables Gemini-backed scam, resume, and assistant features. Keep backend-only. If unset, those requests are unavailable rather than fabricated. |
| `GOOGLE_SAFE_BROWSING_API_KEY` | Optional | Enables the configured reputation provider. Keep backend-only; without it, reputation is unverified. |
| `ADZUNA_APP_ID`, `ADZUNA_APP_KEY` | Optional | Enable live job search. Keep both backend-only and confirm Adzuna terms/licensing and attribution requirements for the intended deployment. |
| `ADZUNA_COUNTRY` | Optional | Provider country code; defaults to `gb`. |
| `GEMINI_MODEL`, `GEMINI_TIMEOUT` | Optional | Model and request timeout; defaults are `gemini-2.5-flash` and `20s`. |
| `RESUME_MAX_FILE_SIZE`, `RESUME_MAX_REQUEST_SIZE` | Optional | Upload and multipart limits; documented defaults are 5 MB and 6 MB. |
| `JOB_MATCH_WEIGHT_SKILLS`, `JOB_MATCH_WEIGHT_EXPERIENCE`, `JOB_MATCH_WEIGHT_EDUCATION`, `JOB_MATCH_WEIGHT_LOCATION_MODE`, `JOB_MATCH_WEIGHT_JOB_TYPE` | Optional | Defaults are 50/20/10/10/10; values must be non-negative and total exactly 100. |

`backend/.env.example` contains placeholders and development defaults only. Do not copy it unchanged into production or store real secrets in frontend variables, source control, build logs, or deployment documentation.

## Ordered deployment preparation and release procedure

Do not run the deployment steps until the release gates above are accepted and the operator authorizes a target environment.

1. **Select and provision approved resources.** Choose the Azure subscription, region, resource group, names, budget, identity/network policy, and hosting plan. Provision an Azure Static Web App, an Azure App Service on Linux configured for Java SE 21, and a persistent MongoDB service; configure DNS/TLS and private/network-restricted database access. This repository does not contain IaC to provision them. These commands below assume those resources already exist; do not run them before the operator authorizes a target.
2. **Set backend configuration in the hosting secret store.** Supply `MONGODB_URI` and a newly generated `JWT_SECRET_BASE64` through secret references. Set `AUTH_COOKIE_SECURE=true` and exact `FRONTEND_ORIGIN`. Add optional provider keys only when accounts, quotas, consent, terms, and intended use are approved. Do not put credentials in the frontend or command-line history.
3. **Build and test the backend with JDK 21.** From the repository root in PowerShell, run:

   ```powershell
   Set-Location .\backend
   mvn clean verify
   ```

   Confirm `target\scamshield-backend-0.1.0.jar` exists. Stop if any test fails or the critical dependency gate has not been resolved/accepted. After the approved App Service name and resource group are known, deploy the JAR using Azure CLI:

   ```powershell
   az webapp deploy --resource-group <resource-group> --name <api-app-name> --src-path target\scamshield-backend-0.1.0.jar --type jar
   ```

   Configure the Java SE 21 runtime and backend application settings in the App Service before validating startup. Keep secret values in App Service secret references or the approved Key Vault integration, not in this guide or shell history.
4. **Build the frontend for the actual API URL.** From the repository root in PowerShell, set the API URL before building:

   ```powershell
   Set-Location .\frontend
   npm ci
   $env:VITE_API_BASE_URL = 'https://<api-hostname>'
   npm test
   npm run build
   ```

   Publish only `frontend/dist/` to the Static Web App. After obtaining its deployment token through the approved secret store, the Static Web Apps CLI can deploy the built directory without putting the token in source control:

   ```powershell
   npx @azure/static-web-apps-cli deploy ./dist --deployment-token $env:SWA_CLI_DEPLOYMENT_TOKEN --env production
   ```

   Configure/verify SPA fallback to `index.html` so direct navigation and refresh on routes such as `/dashboard` work.
5. **Verify configuration and connectivity without exposing secrets.** Confirm the frontend asset bundle targets the intended API host; confirm the API accepts only the expected frontend CORS origin; confirm HTTPS and secure cookies; check `GET /api/auth/csrf`; register a controlled test account and verify login, `GET /api/auth/me`, logout, and CSRF-protected requests. Verify MongoDB-backed behavior and recovery using approved test data. Do not use the admin-only health route as an unauthenticated probe.
6. **Verify optional providers separately.** With approved test data and valid credentials, confirm Gemini, Safe Browsing, and Adzuna behavior, failure handling, quotas, consent, provider terms, and any required attribution. Features whose provider is unconfigured should remain unavailable/unverified as documented.
7. **Monitor and operate.** Configure platform logs/alerts without logging tokens, submitted sensitive URLs, resumes, or provider secrets; set retention and backup policies; test restore; document incident response and rollback. Reassess scaling because rate limiting is process-local.

## Verification record for this preparation pass

The local frontend build in this preparation pass ran without `VITE_API_BASE_URL`; the generated bundle therefore uses the development fallback `http://localhost:8080` and is not a production deployment artifact. Rebuild with the target HTTPS API URL before publishing. Results of the actual local tests/builds and the environment limitations are recorded in [the Module 15 progress file](.github/modernize/deployment/progress.md) and [completion summary](.github/modernize/deployment/summary.md). Local builds and the test-context localhost MongoDB connection do not verify deployed services, production credentials, production database access, DNS/TLS, or provider access.

Command references: [Azure CLI `az webapp deploy`](https://learn.microsoft.com/en-us/cli/azure/webapp?view=azure-cli-latest#az-webapp-deploy) and [Azure Static Web Apps CLI `swa deploy`](https://azure.github.io/static-web-apps-cli/docs/cli/swa-deploy/). Verify the current platform instructions and organizational policies when resources are selected.
