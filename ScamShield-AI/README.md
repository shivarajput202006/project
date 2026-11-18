# ScamShield AI

ScamShield AI is a React application with a Java backend. The modules include the public landing page, authenticated dashboard, scam and URL analysis, live jobs, resume analysis and matching, reports, profile/settings, an AI Career Assistant, and notifications. Live listings require provider credentials and terms-compliant use; Gemini-powered features require the backend Gemini key and an enabled account AI-processing preference.

## Requirements

- Node.js and npm
- Java 21 (the backend targets Java 21)
- Maven
- A MongoDB instance, such as a MongoDB Atlas cluster

## Configure MongoDB and the backend

1. In `backend`, copy `.env.example` to `.env`.
2. Replace the MongoDB placeholder with your own connection URI. Use the `scamshield_db` database and ensure the database user can read and write. URL-encode special characters in the database username or password.
3. Generate a random JWT signing key in PowerShell:

   ```powershell
   $bytes = New-Object byte[] 32
   $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   [Convert]::ToBase64String($bytes)
   $rng.Dispose()
   ```

4. Put the generated value in `JWT_SECRET_BASE64` in `backend/.env`. Do not commit `.env` or share its contents. Set `AUTH_COOKIE_SECURE=true` when deploying behind HTTPS; keep `false` for local HTTP.
5. To use scam analysis, resume analysis, or the AI Career Assistant, add a Gemini API key as `GEMINI_API_KEY` in `backend/.env` or your deployment secret store. The value in `.env.example` is intentionally blank. You can optionally set `GEMINI_MODEL` (defaults to `gemini-2.5-flash`) and `GEMINI_TIMEOUT` (defaults to `20s`). Without a key, the backend starts normally and AI requests return `503 Service Unavailable`; it never returns fabricated responses.
6. To enable URL reputation checks, add a Google Safe Browsing API key as `GOOGLE_SAFE_BROWSING_API_KEY` in `backend/.env` or the deployment secret store. Keep the key server-side. When it is blank, scanner results remain unverified unless heuristic signals need review; the scanner does not fabricate reputation data. Quota and provider errors are reported as unavailable.
7. To enable live jobs, register for an Adzuna API key at [developer.adzuna.com](https://developer.adzuna.com/signup) and set `ADZUNA_APP_ID`, `ADZUNA_APP_KEY`, and optionally `ADZUNA_COUNTRY` (two-letter country code; defaults to `gb`). Credentials are read only by the backend and must never be added to frontend variables. Adzuna's [API documentation](https://developer.adzuna.com/docs/search) describes job search and listing fields; its [terms](https://developer.adzuna.com/docs/terms_of_service) permit personal job search and publishing listings, require each advert's “Adzuna” attribution to be at least 116 × 23 pixels and linked to Adzuna's relevant local domain, and require written licensing for some commercial or product use. Confirm your deployment and use are covered before enabling the provider. Search results are not persisted; the app stores a listing only after the user saves it and the backend confirms it is still returned by Adzuna.
8. Resume analysis uses the same backend-only Gemini key (`GEMINI_API_KEY`). PDF and DOCX uploads are stored privately in MongoDB GridFS. Resume text is sent to Gemini only when the account's AI-processing preference is enabled and the user also accepts the per-analysis disclosure. The upload limit defaults to 5 MB and can be changed with `RESUME_MAX_FILE_SIZE`; multipart requests default to 6 MB and can be changed with `RESUME_MAX_REQUEST_SIZE`. Deleting a resume also removes its private file and saved analyses. Scanned PDFs require OCR, which is not configured.
9. Start the backend from the `backend` directory:

   ```powershell
   mvn spring-boot:run
   ```

Spring Boot imports the local `backend/.env` file when present. The MongoDB URI and JWT secret are required: without them, the backend will not start. Alternatively, set those variables in your shell/environment.

## Run the frontend

Open another terminal in `frontend`:

```powershell
npm install
npm run dev
```

Visit the Vite URL (usually `http://localhost:5173`). The API URL defaults to `http://localhost:8080`; set `VITE_API_BASE_URL` before starting Vite only if the backend is hosted elsewhere. Set `FRONTEND_ORIGIN` in the backend environment to the exact frontend origin.

To build the frontend for production, run `npm run build` in `frontend`.

For production release gates, backend/frontend build configuration, provider and database requirements, and deployment verification steps, see [DEPLOYMENT-READINESS.md](DEPLOYMENT-READINESS.md). No deployment is configured by this repository.

## Authentication notes

- JWTs are sent in an `HttpOnly`, `SameSite=Lax` cookie; browser JavaScript cannot read them. HTTPS deployments must enable the `Secure` cookie flag.
- CSRF protection is enabled. The frontend first requests a CSRF token and sends it in `X-XSRF-TOKEN` for state-changing requests.
- Passwords are never stored in frontend storage. The server stores only BCrypt hashes.
- Registration always assigns `USER`; client-supplied roles are ignored.
- The JWT lasts one hour. “Remember me” is omitted. Logout clears the browser cookie. Password changes increment the user's token version, invalidating previously issued JWTs; account deletion removes the user record, so outstanding tokens can no longer resolve to an account.
- Password recovery is a clear placeholder and does not send an email.
- Authentication protects `/dashboard`; use the same `ProtectedRoute` wrapper for future private routes. The landing page remains public.
- Dashboard `Total Scam Analyses`, `URLs Scanned`, saved jobs, resume analysis count, recent activity, and notification preview read only the authenticated user's records.
- The AI Scam Detector (`/scam-detector`), Smart URL Scanner (`/url-scanner`), Live Job Finder (`/live-jobs`), and Saved Jobs (`/saved-jobs`) use the existing `ProtectedRoute`, JWT cookie, and CSRF conventions. Searches call Adzuna's official API directly with a 10-second read timeout and a per-user 30-searches-per-minute limit. When credentials are missing or the provider is unavailable, the API returns a clear error and never substitutes sample listings. Provider search results remain transient; saving revalidates the provider listing before persisting a bookmark to `jobs` and a user-scoped reference to `saved_jobs`. Removing a saved job deletes its stored listing when no user still references it. A listing marked `SOURCE_LISTED` has not been independently checked for employer authenticity. The provider's `redirect_url` is exposed only as a safe HTTPS Adzuna listing link; no job application URL is fetched by the backend.
- The protected AI Resume Analyzer (`/resume-analyzer`) accepts PDF and DOCX files after validating extension, MIME type, signature, archive contents, size, and extractability. Files are stored in MongoDB GridFS, scoped to the authenticated owner, and deleted with their analysis records. Analysis requires explicit consent before extracted resume text is sent to the configured Gemini service. Scanned PDFs are identified as lacking extractable text; OCR is not configured. Scores are a transparent advisory checklist, not an official ATS score. Recommended roles link into the Live Job Finder; no job listings are generated by resume analysis.
- The protected Job Matching dashboard (`/job-matching`) combines an owner's completed Module 07 resume analysis with current Module 06 provider listings. Default weights are configurable with `JOB_MATCH_WEIGHT_SKILLS`, `JOB_MATCH_WEIGHT_EXPERIENCE`, `JOB_MATCH_WEIGHT_EDUCATION`, `JOB_MATCH_WEIGHT_LOCATION_MODE`, and `JOB_MATCH_WEIGHT_JOB_TYPE`; they must be non-negative and total exactly 100. Defaults are 50/20/10/10/10. Unknown criteria are excluded and remaining weights are renormalized. Provider listings do not distinguish required/preferred skills or reliably supply every comparison field, so the UI explains unavailable criteria and labels missing skills as not evidenced. Match scores are relevance estimates, not hiring probabilities, safety guarantees, or ATS results. The `job_matches` collection stores per-owner matching evidence and provider references, not copied job descriptions; deleting a resume removes its match history. Matching reuses the configured Adzuna provider and per-user search limit; missing credentials or empty provider results never produce sample listings.
- Adzuna's search API does not provide authoritative experience, work-mode, required-skill, or salary-currency fields. Work mode and experience are populated only when recognizable text appears in a listing; skill chips are terms found in the listing, not a provider-confirmed requirements list. Salary currency is not guessed.
- Analysis input is sent to the configured Gemini service. Raw text, sender addresses, full URLs, and uploaded files are not persisted; the saved report contains a generic input summary and the validated, email/phone-redacted analysis. Users should avoid submitting passwords, payment credentials, or identity-document data.
- The Smart URL Scanner accepts HTTP/HTTPS only, normalizes IDNs, detects localhost and private/reserved IP literals, and applies explicitly heuristic-only domain/URL signals. It never resolves or connects to a submitted hostname, opens a submitted URL, follows redirects, or downloads content; DNS rebinding and redirect-to-private SSRF are avoided by making no submitted-site requests. Redirects and domain-registration information are therefore unavailable, not guessed.
- When configured, Google Safe Browsing is contacted at a fixed backend-only endpoint with the submitted URL for a reputation lookup. Its `CONFIRMED_MATCH`, `NO_MATCH`, `RATE_LIMITED`, `UNAVAILABLE`, and `NOT_CONFIGURED` outcomes remain distinct. A no-match result and HTTPS do not prove safety. Heuristic signals (such as a shortened URL, a possible one-character brand lookalike, or multiple subdomain levels) can be false positives and never independently produce a confirmed malicious status.

## Authentication API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/auth/csrf` | Obtain a CSRF token and set the CSRF cookie |
| `POST` | `/api/auth/register` | Register as a `USER` and sign in |
| `POST` | `/api/auth/login` | Verify credentials and sign in |
| `POST` | `/api/auth/logout` | Clear the auth cookie (requires authentication and CSRF token) |
| `GET` | `/api/auth/me` | Return the authenticated user's public profile |
| `GET` | `/api/dashboard/overview` | Return the authenticated user's dashboard overview |
| `POST` | `/api/scam-detector/analyze/text` | Analyze pasted message or job-offer text |
| `POST` | `/api/scam-detector/analyze/email` | Analyze subject, sender address, and email body |
| `POST` | `/api/scam-detector/analyze/url` | Analyze an HTTP/HTTPS URL without opening it |
| `POST` | `/api/scam-detector/analyze/file` | Analyze PNG/JPG/JPEG/WEBP image or PDF/DOCX |
| `GET` | `/api/scam-detector/history` | List up to 25 reports for the authenticated user |
| `GET` | `/api/scam-detector/{id}` | Read a report only if it belongs to the authenticated user |
| `POST` | `/api/url-scanner/scan` | Validate a URL and return address heuristics and optional reputation findings |
| `GET` | `/api/url-scanner/history` | List up to 25 URL scans for the authenticated user |
| `GET` | `/api/url-scanner/{id}` | Read a URL scan only if it belongs to the authenticated user |
| `DELETE` | `/api/url-scanner/{id}` | Delete a URL scan only if it belongs to the authenticated user (requires CSRF) |
| `GET` | `/api/jobs/search` | Search live Adzuna listings; accepts `keyword`, `location`, `workMode`, `experience`, `jobType`, `salaryMin`, `page`, and `size` |
| `GET` | `/api/jobs/filters` | List available job filters and provider configuration status |
| `GET` | `/api/jobs/{id}` | Return a listing only if the authenticated user has saved it |
| `POST` | `/api/jobs/{id}/save` | Revalidate a live Adzuna listing and save it for the authenticated user (requires CSRF) |
| `DELETE` | `/api/jobs/{id}/save` | Remove the authenticated user's saved listing (requires CSRF) |
| `GET` | `/api/jobs/saved` | List up to 100 saved listings belonging to the authenticated user |
| `GET` | `/api/jobs/recommendations` | Match the authenticated user's latest analyzed resume to current provider listings; unavailable when resume data or provider listings are absent |
| `POST` | `/api/resume/upload` | Validate and privately store a PDF/DOCX resume in MongoDB GridFS |
| `POST` | `/api/resume/{id}/analyze` | Extract and analyze an owned resume after explicit Gemini-processing consent |
| `GET` | `/api/resume/{id}` | Return resume metadata only for its authenticated owner |
| `GET` | `/api/resume/{id}/analysis` | Return the latest analysis only for its authenticated owner |
| `GET` | `/api/resume/history` | List up to 25 resumes belonging to the authenticated user |
| `DELETE` | `/api/resume/{id}` | Delete an owned resume, its GridFS file, and analyses (requires CSRF) |
| `GET` | `/api/resume/{id}/recommendations` | Return resume-based role suggestions and current job-provider configuration status |
| `GET` | `/api/reports` | List unified reports with type, status, date, keyword, page, size, and sort filters |
| `GET` | `/api/reports/summary` | Return authenticated-owner report totals by source module |
| `GET` | `/api/reports/categories` | Return report category names and owner-scoped counts |
| `GET` | `/api/reports/{id}` | Read an owned source report |
| `GET` | `/api/reports/{id}/download` | Download a generated PDF for an owned report |
| `DELETE` | `/api/reports/{id}` | Hide an entry from reports history without deleting its original analysis (requires CSRF) |
| `GET` | `/api/profile/me` | Read the authenticated user's profile |
| `PUT` | `/api/profile/me` | Update profile/contact fields; email and role are not mutable here (requires CSRF) |
| `GET`, `POST` | `/api/profile/education` | List or add the authenticated user's education (POST requires CSRF) |
| `PUT`, `DELETE` | `/api/profile/education/{id}` | Update or remove an owned education record (requires CSRF) |
| `PUT` | `/api/profile/skills` | Replace user-provided profile skills without changing resume analysis data (requires CSRF) |
| `GET`, `POST`, `DELETE` | `/api/profile/photo` | Read, upload, or remove a private JPEG/PNG/WEBP profile photo (writes require CSRF) |
| `PUT` | `/api/profile/password` | Verify current password, update BCrypt hash, and invalidate existing tokens (requires CSRF) |
| `POST` | `/api/profile/data-export` | Download a private ZIP containing profile data, supported history, and resume files (requires CSRF) |
| `DELETE` | `/api/profile/me` | Permanently remove account-owned data after password and typed confirmation (requires CSRF) |
| `GET`, `PUT` | `/api/settings` | Read or update the authenticated user's AI-processing preference (PUT requires CSRF) |
| `POST` | `/api/assistant/chat` | Send a career question, optionally continue an owned conversation and select up to four owned context sources |
| `GET` | `/api/assistant/conversations` | Return paginated conversation summaries for the authenticated user |
| `GET` | `/api/assistant/conversations/{id}` | Read an owned conversation and its messages |
| `DELETE` | `/api/assistant/conversations/{id}` | Delete an owned conversation (requires CSRF) |
| `POST` | `/api/assistant/conversations/{id}/feedback` | Save feedback for an assistant response in an owned conversation (requires CSRF) |
| `DELETE` | `/api/assistant/conversations` | Clear the authenticated user's conversation history (requires CSRF) |
| `GET` | `/api/notifications` | List owner-scoped notifications with filter and pagination |
| `GET` | `/api/notifications/unread-count` | Return the authenticated user's unread count |
| `GET` | `/api/notifications/{id}` | Read an owned notification |
| `PATCH` | `/api/notifications/{id}/read` | Mark an owned notification as read (requires CSRF) |
| `PATCH` | `/api/notifications/{id}/unread` | Mark an owned notification as unread (requires CSRF) |
| `PATCH` | `/api/notifications/read-all` | Mark the authenticated user's notifications as read (requires CSRF) |
| `DELETE` | `/api/notifications/{id}` | Delete an owned notification (requires CSRF) |
| `DELETE` | `/api/notifications` | Clear the authenticated user's notifications (requires CSRF) |
| `GET`, `PUT` | `/api/notifications/preferences` | Read or update owner-scoped notification categories (PUT requires CSRF) |

State-changing requests use the existing CSRF token header. JSON requests use `Content-Type: application/json`; file endpoints accept multipart `file` parts. Resume uploads default to 5 MB and can be configured with `RESUME_MAX_FILE_SIZE`.

### Dashboard data dependencies

Scam detector reports are stored in `scam_analyses`; URL scan records are stored in `url_scans` in the configured `scamshield_db` database. Each collection has a compound index on its authenticated `userId` and timestamp. URL history, detail, and deletion queries are scoped to the authenticated user's database ID. The API does not accept a frontend-supplied user ID for access control. URL scans retain the submitted URL as requested; avoid scanning links containing access tokens or other private query values.

The scanner does not connect to submitted domains or resolve their DNS, so it cannot inspect redirect destinations, page content, TLS certificates, or registration data. It supports only standard HTTP/HTTPS ports. With no reputation provider, the result stays `UNVERIFIED` when heuristics do not raise a review signal.

PDF text is extracted with PDFBox; DOCX text is extracted with Apache POI. Images are sent to Gemini as image input. Uploaded bytes are validated on the server and processed in memory; they are not saved to public storage. PDF/DOCX extraction is capped at 50,000 characters, PDFs at 200 pages, images at 20 million pixels, and archives at 20 MB expanded content.

Resume PDF/DOCX bytes are stored in private MongoDB GridFS and only read by owner-scoped backend endpoints. Resume analysis extracts at most 50,000 characters and supports PDFs up to 100 pages. Scanned PDFs require OCR, which is not configured. Delete operations remove the GridFS file and persisted analyses.

### Reports & Analysis History

The protected `/reports` dashboard aggregates authenticated-owner records directly from `scam_analyses`, `url_scans`, `resume_analyses`, and `job_matches`. Search, category/status/date filters, newest/oldest sorting, pagination, report details, summary counts, and PDF export operate on those stored records; no sample reports or statistics are generated. `report_metadata` stores only a source reference and hidden-history marker when a user removes a report. That action does not delete the original module record. Resume report views and PDFs omit personal contact information. Job-match reports contain the saved score/evidence and provider reference; external job listing content is not copied into the report index. For filtered views, use `/api/reports?type=URL_SCAN&fromDate=2026-01-01&toDate=2026-12-31&page=1&size=12&sort=NEWEST`.

### User Profile & Settings

The protected `/profile` and `/settings` pages use the existing authenticated user identity. User profile details, embedded education and user-provided skills are stored separately from extracted resume analysis; uploaded photos are limited to validated private JPEG, PNG, or WEBP files stored in owner-scoped GridFS. The account-level AI preference defaults to off and is an additional gate before the existing per-analysis resume consent. Resume visibility remains private; public sharing is not supported. Email and role are excluded from profile update requests.

The account export endpoint returns a private ZIP of profile/settings, saved-job references, source-module analyses and history metadata, and the user's uploaded resume files. Account deletion requires the current password and the exact `DELETE` confirmation, removes owner-scoped GridFS files and profile/settings/resume/scam/URL/match/saved-job/report records, then removes the user document. Shared provider job-cache listings are retained. The deletion is a sequence of owner-scoped MongoDB operations rather than a multi-collection transaction; if an operation fails, the user record is deleted last and the request can be retried after signing in.

### AI Career Assistant

The protected `/dashboard/assistant` module uses the existing backend-only `GEMINI_API_KEY` and the account AI-processing preference. If that preference is disabled or Gemini is not configured, the API returns an explicit error rather than a fabricated answer. Conversations are stored in `assistant_conversations`, indexed by owner and update time; list results are paginated, each conversation is bounded to 80 messages, prompt history is bounded to the most recent 12 messages/12,000 characters, and each user message is limited to 3,000 characters. The in-process per-user limit is 10 chat requests per minute. Context retrieval is opt-in per request and limited to four owner-scoped resume, job-match, scam, URL, and profile sources; live provider listings are public listing metadata and are labeled as not independently verified. Prompt and retrieved data are treated as untrusted. Markdown is rendered as React text/elements with only HTTP(S) links allowed; raw HTML is not executed. This is informational guidance, not a hiring or safety guarantee.

### Notifications & Alerts

The protected `/notifications` page and dashboard bell read the `notifications` collection. Its indexes support owner/timestamp lookup and event-key deduplication; `notification_preferences` has a unique owner index. Categories can be disabled by the user, but SECURITY and ADMIN notifications always remain enabled. Successful scam analysis, URL scans, resume analysis, job-match generation, report PDF exports, and password changes publish best-effort events; notification persistence failures do not fail the original operation. Event copy avoids submitted content and sensitive resume details. Action links are restricted to approved internal routes. The frontend polls every 30 seconds, cancels the timer on unmount, and avoids overlapping requests. No email, SMS, or WebSocket delivery is configured.

Register and login requests include a CSRF header. Example JSON bodies:

```json
{
  "fullName": "Morgan Lee",
  "email": "morgan@example.com",
  "password": "a-long-example-password",
  "confirmPassword": "a-long-example-password"
}
```

```json
{
  "email": "morgan@example.com",
  "password": "a-long-example-password"
}
```

Do not use the example credentials as a real password.

## Tests

Run backend tests from `backend` with `mvn test`. Run the frontend production build from `frontend` with `npm run build`. Unit and MockMvc tests use a local mock Gemini response and do not require a real Gemini key. To verify live model behavior, configure a valid key and use a test account; no live Gemini request is sent by the automated suite.

## Admin console

The read-only administrator console is available at `/admin` to accounts whose existing role is `ADMIN`. The backend separately enforces that role for every `/api/admin/**` request; registering from the public UI always creates a `USER`.

Implemented endpoints:

- `GET /api/admin/dashboard/overview` — counts from existing user, scam analysis, URL scan, resume analysis, job-match, and stored saved-listing collections.
- `GET /api/admin/users?search=&page=0&size=20` — paginated account directory with only ID, name, email, role, and creation time.
- `GET /api/admin/monitoring/security-events` — recent security-type notifications without notification message content or user identifiers.
- `GET /api/admin/system/health` — MongoDB ping status and process uptime.

This initial console is intentionally read-only. The current data model does not track active-user sessions, job-search events, historical chart snapshots, or admin action audits; those values are displayed as unavailable or clearly called out rather than inferred. User disabling/deletion, role changes, announcements, and other administrative writes are not enabled.

## Main folders

- `frontend/src/modules/landing`: preserved landing page sections
- `frontend/src/modules/auth`: auth pages, state, validation, and route protection
- `frontend/src/modules/dashboard`: protected dashboard, summaries, and dashboard styling
- `frontend/src/modules/admin`: role-gated, read-only administrator console
- `frontend/src/modules/scam-detector`: protected scam analysis input, reports, and history
- `frontend/src/modules/url-scanner`: protected URL analysis, results, and history
- `frontend/src/modules/job-finder`: protected provider-backed job search, listing details, filters, and saved jobs
- `frontend/src/modules/resume-analyzer`: consent-gated resume upload, analysis results, recommendations, and history
- `frontend/src/modules/job-matching`: evidence-based matching of analyzed resumes to current live job listings
- `frontend/src/modules/reports`: unified, authenticated report history, details, filters, and PDF downloads
- `frontend/src/modules/profile`: authenticated profile, education, skills, privacy, security, export, and deletion flows
- `frontend/src/services`: frontend API services
- `frontend/src/utils`: client-side file validation helpers
- `frontend/src/components`: shared layout and UI components
- `backend/src/main/java/com/scamshield`: Spring Boot API and security code
- `backend/src/main/java/com/scamshield/scamdetector`: analysis API, Gemini integration, extraction, URL checks, and user-scoped MongoDB records
- `backend/src/main/java/com/scamshield/urlscanner`: URL validation, local heuristics, optional reputation lookup, and user-scoped scan history
- `backend/src/main/java/com/scamshield/jobfinder`: provider adapter, job search API, matching preparation, and user-scoped bookmarks
- `backend/src/main/java/com/scamshield/resume`: private resume storage, document extraction, Gemini analysis, scoring, and recommendations
- `backend/src/main/java/com/scamshield/jobmatching`: resume-to-listing comparison, explainable scoring, and owner-scoped match history
- `backend/src/main/java/com/scamshield/reports`: report aggregation, source-record authorization, PDF export, and history metadata
- `backend/src/main/java/com/scamshield/profile`: owner-scoped profile, education, skills, photo, privacy, password, export, and account deletion APIs
- `backend/src/main/java/com/scamshield/admin`: administrator-only platform metrics, paginated account listing, security notifications, and database health APIs
- `backend/src/test`: authentication and access-control tests
