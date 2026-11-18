# API & Service Communication Contracts

The application exposes 57 unversioned REST endpoints through one Spring MVC backend; the React client calls it synchronously over HTTP. Backend modules use direct service calls and synchronous MongoDB/provider access, with no gateway or message broker.

## Service Catalog

| Service | Port | Category | Purpose |
|---|---:|---|---|
| ScamShield AI Backend (`backend`, `scamshield-backend`) | 8080 (Spring Boot default) | API Layer | Authentication, dashboard, analysis, job search, matching, reports, profile, and account settings APIs. |
| ScamShield AI Frontend (`frontend`, Vite app) | 5173 (Vite default) | Frontend | React single-page application that calls the backend API. |

The backend is a single Maven application; its feature packages are in-process modules, not independently deployed services. No Docker Compose or Kubernetes service definitions were found.

## API Endpoints Inventory

The backend does not define a URL, header, or query-based API version.

| Service | Method | Path | Request Type | Response Type |
|---|---|---|---|---|
| AuthController | GET | `/api/auth/csrf` | Cookie/token context | `Map<String,String>` (200) |
| AuthController | POST | `/api/auth/register` | JSON `RegisterRequest` | `AuthResponse` (201) |
| AuthController | POST | `/api/auth/login` | JSON `LoginRequest` | `AuthResponse` (200) |
| AuthController | POST | `/api/auth/logout` | Auth cookie and CSRF token | Empty (204) |
| AuthController | GET | `/api/auth/me` | Auth cookie | `AuthResponse` (200) |
| DashboardController | GET | `/api/dashboard/overview` | Auth cookie | `DashboardResponse` (200) |
| ScamDetectorController | POST | `/api/scam-detector/analyze/text` | JSON `ScamAnalysisRequest.Text` | `ScamAnalysisResponse` (200) |
| ScamDetectorController | POST | `/api/scam-detector/analyze/email` | JSON `ScamAnalysisRequest.EmailMessage` | `ScamAnalysisResponse` (200) |
| ScamDetectorController | POST | `/api/scam-detector/analyze/url` | JSON `ScamAnalysisRequest.Url` | `ScamAnalysisResponse` (200) |
| ScamDetectorController | POST | `/api/scam-detector/analyze/file` | Multipart `file` | `ScamAnalysisResponse` (200) |
| ScamDetectorController | GET | `/api/scam-detector/history` | Auth cookie | `List<ScamAnalysisResponse>` (200) |
| ScamDetectorController | GET | `/api/scam-detector/{id}` | Path `id` | `ScamAnalysisResponse` (200) |
| URLScannerController | POST | `/api/url-scanner/scan` | JSON `URLScanRequest` | `URLScanResponse` (200) |
| URLScannerController | GET | `/api/url-scanner/history` | Auth cookie | `List<URLScanResponse>` (200) |
| URLScannerController | GET | `/api/url-scanner/{id}` | Path `id` | `URLScanResponse` (200) |
| URLScannerController | DELETE | `/api/url-scanner/{id}` | Path `id` | Empty (204) |
| JobFinderController | GET | `/api/jobs/search` | Query: keyword, location, workMode, experience, jobType, salaryMin, page, size | `JobSearchResponse` (200) |
| JobFinderController | GET | `/api/jobs/filters` | No body | `JobFiltersResponse` (200) |
| JobFinderController | GET | `/api/jobs/saved` | Auth cookie | `List<JobResponse>` (200) |
| JobFinderController | GET | `/api/jobs/recommendations` | Auth cookie | `JobRecommendationResponse` (200) |
| JobFinderController | POST | `/api/jobs/{id}/save` | Path `id`, JSON `JobSaveRequest` | `JobResponse` (200) |
| JobFinderController | DELETE | `/api/jobs/{id}/save` | Path `id` | Empty (204) |
| JobFinderController | GET | `/api/jobs/{id}` | Path `id` | `JobResponse` (200) |
| ResumeController | POST | `/api/resume/upload` | Multipart `file` | `ResumeUploadResponse` (201) |
| ResumeController | POST | `/api/resume/{id}/analyze` | Path `id`, JSON `ResumeAnalyzeRequest` | `ResumeAnalysisResponse` (200) |
| ResumeController | GET | `/api/resume/history` | Auth cookie | `List<ResumeHistoryItem>` (200) |
| ResumeController | GET | `/api/resume/{id}/analysis` | Path `id` | `ResumeAnalysisResponse` (200) |
| ResumeController | GET | `/api/resume/{id}/recommendations` | Path `id` | `ResumeRecommendationResponse` (200) |
| ResumeController | GET | `/api/resume/{id}` | Path `id` | `ResumeUploadResponse` (200) |
| ResumeController | DELETE | `/api/resume/{id}` | Path `id` | Empty (204) |
| JobMatchingController | POST | `/api/job-matching/analyze` | JSON `JobMatchRequest` | `JobMatchSearchResponse` (200) |
| JobMatchingController | GET | `/api/job-matching/recommendations` | Query: resumeId, keyword, location, workMode, jobType, page, size | `JobMatchSearchResponse` (200) |
| JobMatchingController | GET | `/api/job-matching/{jobId}` | Path provider job ID | `JobMatchResponse` (200) |
| JobMatchingController | GET | `/api/job-matching/history` | Optional query `resumeId` | `List<JobMatchHistoryResponse>` (200) |
| JobMatchingController | DELETE | `/api/job-matching/history/{id}` | Path match record `id` | Empty (204) |
| JobMatchingController | POST | `/api/job-matching/refresh` | JSON `JobMatchRequest` | `JobMatchSearchResponse` (200) |
| ReportController | GET | `/api/reports` | Query: type, status, fromDate, toDate, keyword, page, size, sort | `ReportPageResponse` (200) |
| ReportController | GET | `/api/reports/summary` | Auth cookie | `ReportSummaryResponse` (200) |
| ReportController | GET | `/api/reports/categories` | Auth cookie | `List<ReportCategoryResponse>` (200) |
| ReportController | GET | `/api/reports/{id}` | Path report ID | `ReportResponse` (200) |
| ReportController | GET | `/api/reports/{id}/download` | Path report ID | `application/pdf` bytes (200) |
| ReportController | DELETE | `/api/reports/{id}` | Path report ID and CSRF token | Empty (204); hides history reference only |
| ProfileController | GET | `/api/profile/me` | Auth cookie | `ProfileResponse` (200) |
| ProfileController | PUT | `/api/profile/me` | JSON `UpdateProfileRequest` | `ProfileResponse` (200) |
| ProfileController | GET | `/api/profile/education` | Auth cookie | `List<Education>` (200) |
| ProfileController | POST | `/api/profile/education` | JSON `EducationRequest` | `Education` (201) |
| ProfileController | PUT | `/api/profile/education/{id}` | Path ID, JSON `EducationRequest` | `Education` (200) |
| ProfileController | DELETE | `/api/profile/education/{id}` | Path ID and CSRF token | Empty (204) |
| ProfileController | PUT | `/api/profile/skills` | JSON `UpdateSkillsRequest` | `List<ProfileSkill>` (200) |
| ProfileController | GET | `/api/profile/photo` | Auth cookie | Private image bytes (200) |
| ProfileController | POST | `/api/profile/photo` | Multipart `file` | `ProfilePhotoResponse` (200) |
| ProfileController | DELETE | `/api/profile/photo` | CSRF token | Empty (204) |
| ProfileController | PUT | `/api/profile/password` | JSON `ChangePasswordRequest` | Empty (204), expires current auth cookie |
| ProfileController | POST | `/api/profile/data-export` | Auth cookie and CSRF token | `application/zip` bytes (200) |
| ProfileController | DELETE | `/api/profile/me` | JSON `DeleteAccountRequest` and CSRF token | Empty (204), expires current auth cookie |
| SettingsController | GET | `/api/settings` | Auth cookie | `SettingsResponse` (200) |
| SettingsController | PUT | `/api/settings` | JSON `UpdateSettingsRequest` | `SettingsResponse` (200) |

Validation and authorization failures use the application's JSON error handler; applicable status codes include 400, 401, 403, 404, and provider-specific errors.

## Management & Observability Endpoints

| Service | Endpoint | Custom Metrics (if any) |
|---|---|---|
| ScamShield AI Backend | None identified; Spring Boot Actuator is not configured. | No custom metrics identified. |

## DTOs & Contracts

Controllers expose request and response DTOs, including `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserResponse`, `DashboardResponse`, `RecentActivityResponse`, `ScamAnalysisRequest`, `ScamAnalysisResponse`, `URLScanRequest`, `URLScanResponse`, `JobSearchRequest`, `JobSearchResponse`, `JobFiltersResponse`, `JobSaveRequest`, `JobResponse`, `JobMatchResponse`, `JobRecommendationResponse`, `ResumeAnalyzeRequest`, `ResumeUploadResponse`, `ResumeAnalysisResponse`, `ResumeHistoryItem`, `ResumeRecommendationResponse`, `JobMatchRequest`, `JobMatchSearchResponse`, `JobMatchHistoryResponse`, `SkillComparisonResponse`, `MatchExplanationResponse`, `ReportFilterRequest`, `ReportPageResponse`, `ReportSummaryResponse`, `ReportCategoryResponse`, `ReportResponse`, `ReportSectionResponse`, `ProfileResponse`, `UpdateProfileRequest`, `EducationRequest`, `Education`, `SkillRequest`, `UpdateSkillsRequest`, `ChangePasswordRequest`, `SettingsResponse`, `UpdateSettingsRequest`, `ProfilePhotoResponse`, and `DeleteAccountRequest`. Most request and response DTOs are immutable Java records; `ScamAnalysisRequest` groups its input variants as nested request types. `ProfileResponse` composes account identity with the separate profile document; `Education` and `ProfileSkill` are user-profile response models.

`Job` and `Resume` are service-level persisted domain models, not gateway aggregation DTOs. `JobResponse` and recommendation responses compose backend module results for the client. Jackson handles JSON serialization; multipart upload endpoints consume `multipart/form-data`. No OpenAPI/Swagger, protobuf, or GraphQL contracts were found. See `data-architecture.md` for persistence details.

## Communication Patterns

- The React/Vite client calls the Spring MVC API synchronously. Controllers delegate to in-process feature services; there is no API gateway, service discovery, load balancer, message broker, or asynchronous job queue.
- Feature services synchronously read and write MongoDB through Spring Data repositories; resume file content uses private GridFS storage. Resume-based job recommendations call job-finder services directly in-process.
- Job matching loads the authenticated owner's selected resume analysis, queries the existing rate-limited Adzuna provider for up to three resume-recommended roles, ranks current results, and stores score evidence/provider references without copying job listing descriptions. Listing details are refreshed from the provider when requested.
- Scam and resume analysis call Google Gemini over HTTPS using Java `HttpClient`. The configured request timeout defaults to 20 seconds and the connect timeout uses that same value. Provider errors, including rate limits and server errors, are returned as failures rather than retried into success-shaped results.
- Job search calls Adzuna through Spring `RestClient`, with a 5-second connect timeout and 10-second read timeout. Search requests are limited to 30 per user per minute in the running backend process; provider 429 responses are surfaced, and no retry or circuit breaker is configured.
- URL reputation lookups call Google Safe Browsing over HTTPS using Java `HttpClient`, with a 3-second connect timeout and 5-second request timeout. Missing credentials and provider failures are reported as unavailable; no retry or circuit breaker is configured.
- Reports & Analysis History aggregates owner-scoped scam analysis, URL scan, resume analysis, and saved job-match records through synchronous repository calls. It does not duplicate source documents. `report_metadata` stores only the report's source reference and an owner-scoped hidden-history timestamp; removing a history entry does not delete the source record. PDF downloads resolve the current owner-owned source record and render stored report sections with PDFBox; a deleted or unauthorized source record is not exported.
- Profile and settings APIs derive owner IDs from the authenticated principal. Profile extensions and settings are stored in owner-unique MongoDB documents; education is embedded and profile skills are marked user-provided. Profile photos are signature-checked, private GridFS objects, and image reads require the owning account.
- Resume AI analysis requires both the user's persistent AI-processing preference and per-request consent. The settings preference defaults to disabled; disabling it blocks new resume analysis but does not delete stored results.
- Password changes verify the existing BCrypt hash, update it using the configured BCrypt encoder, increment a JWT token version, and expire the current auth cookie. The authentication filter checks the token version on subsequent requests.
- Account deletion re-authenticates with the current password and typed confirmation, deletes the authenticated owner's GridFS and owner-scoped source records, then deletes the user document. Shared provider job cache records are not removed. Mongo cleanup is synchronous and sequential, not a multi-collection transaction; the user record is deleted last so a failed cleanup leaves an account that can retry.
- Account exports are generated synchronously as private ZIP responses. They contain account-owned profile/settings, analysis history, saved-job references, and resume file bytes, and exclude password hashes and server storage IDs.
- Authentication uses stateless JWT cookies with Spring Security and CSRF protection. Registration, login, and CSRF-token retrieval are public; other API routes require authentication. CORS allows credentials for the configured frontend origin. The application does not configure inbound TLS; HTTPS termination is not described in the repository.
- Backend API availability depends on configured MongoDB connectivity. No other startup dependency ordering or downstream fallback orchestration was found.

## Service Technology Matrix

| Service | Web | Data Access | Discovery | Gateway | Actuator | Cache | Metrics |
|---|---|---|---|---|---|---|---|
| ScamShield AI Backend | Spring MVC | Spring Data MongoDB, GridFS | None | None | None | None identified | None identified |
| ScamShield AI Frontend | React SPA / Vite | None | None | None | N/A | Browser/runtime only | None identified |

## Service Communication Sequence

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
~~~mermaid
sequenceDiagram
    participant Client as "React Client"
    participant Security as "Spring Security"
    participant SettingsController as "Settings Controller"
    participant SettingsService as "Settings Service"
    participant SettingsRepo as "Settings Repository"
    participant ResumeController as "Resume Controller"
    participant ResumeService as "Resume Analysis Service"
    participant PrivacyService as "Privacy Service"
    participant ProfileController as "Profile Controller"
    participant AccountService as "Account Management Service"
    participant FileStore as "Private GridFS"
    participant Gemini as "Configured Gemini API"
    participant Mongo as "MongoDB"

    Client->>Security: PUT /api/settings with JWT and CSRF
    Security->>SettingsController: Authenticated settings request
    SettingsController->>SettingsService: Update current owner's preference
    SettingsService->>Mongo: Update user_settings by owner ID
    Mongo-->>SettingsService: Persisted preference
    SettingsService-->>SettingsController: SettingsResponse
    SettingsController-->>Client: 200 SettingsResponse

    Client->>Security: POST /api/resume/{id}/analyze with consent
    Security->>ResumeController: Authenticated analysis request
    ResumeController->>ResumeService: Analyze owned resume
    ResumeService->>PrivacyService: Check AI preference and request consent
    PrivacyService->>SettingsRepo: Read current owner's setting
    SettingsRepo->>Mongo: Query user_settings by owner
    Mongo-->>SettingsRepo: Preference value
    SettingsRepo-->>PrivacyService: Enabled or disabled
    PrivacyService-->>ResumeService: Authorization decision
    alt AI preference disabled
        ResumeService-->>ResumeController: 400 preference required
        ResumeController-->>Client: Analysis not started
    else AI preference enabled and per-request consent given
        ResumeService->>FileStore: Read owner-scoped resume file
        FileStore-->>ResumeService: Resume bytes
        ResumeService->>Gemini: Send extracted text for requested analysis
        Gemini-->>ResumeService: Analysis result
        ResumeService->>Mongo: Save owner-scoped resume analysis
        Mongo-->>ResumeService: Persisted analysis
        ResumeService-->>ResumeController: ResumeAnalysisResponse
        ResumeController-->>Client: 200 ResumeAnalysisResponse
    end

    Client->>Security: DELETE /api/profile/me with password and confirmation
    Security->>ProfileController: Authenticated CSRF-verified request
    ProfileController->>AccountService: Verify password and typed confirmation
    AccountService->>FileStore: Delete owner's resume and profile photo files
    FileStore-->>AccountService: Owner files removed
    AccountService->>Mongo: Delete owner-scoped profile analyses reports and saved references
    Mongo-->>AccountService: Owned records removed
    AccountService->>Mongo: Delete users record last
    Mongo-->>AccountService: Account removed
    AccountService-->>ProfileController: Complete and expire auth cookie
    ProfileController-->>Client: 204 No Content
    opt Cleanup operation fails before user deletion
        AccountService-->>ProfileController: Error; user record retained for retry
        ProfileController-->>Client: Cleanup error response
    end
~~~
