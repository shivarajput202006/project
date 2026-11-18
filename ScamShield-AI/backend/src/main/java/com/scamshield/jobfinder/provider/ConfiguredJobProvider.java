package com.scamshield.jobfinder.provider;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.model.JobWorkMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ConfiguredJobProvider implements JobProvider {
    private static final String API_URL = "https://api.adzuna.com/v1/api/jobs";
    private static final Pattern ALLOWED_COUNTRY = Pattern.compile("^[a-z]{2}$");
    private static final Pattern ALLOWED_ADZUNA_HOST = Pattern.compile(
            "(?i)(?:^|\\.)adzuna\\.(?:com|co\\.uk|ca|com\\.au|co\\.nz|in|co\\.za|de|fr|es|it|nl|be|pl|ru|ae|at|ch|com\\.br|com\\.sg)$");
    private static final Set<String> SKILLS = Set.of(
            "java", "spring", "spring boot", "javascript", "typescript", "react", "node.js",
            "python", "django", "flask", "sql", "mongodb", "postgresql", "aws", "azure",
            "docker", "kubernetes", "git", "html", "css", "rest api", "machine learning",
            "data analysis", "excel", "communication", "project management", "customer service");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

    private final String appId;
    private final String appKey;
    private final String country;
    private final RestClient restClient;
    public ConfiguredJobProvider(
            @Value("${app.jobs.adzuna.app-id:}") String appId,
            @Value("${app.jobs.adzuna.app-key:}") String appKey,
            @Value("${app.jobs.adzuna.country:gb}") String country,
            RestClient.Builder builder) {
        this.appId = appId == null ? "" : appId.trim();
        this.appKey = appKey == null ? "" : appKey.trim();
        this.country = country == null ? "" : country.trim().toLowerCase(Locale.ROOT);
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    @Override
    public String sourceName() {
        return "Adzuna";
    }

    @Override
    public String sourceUrl() {
        return adzunaDomain(country);
    }

    @Override
    public boolean isConfigured() {
        return !appId.isBlank()
                && !appKey.isBlank()
                && ALLOWED_COUNTRY.matcher(country).matches();
    }

    @Override
    public JobProviderSearchResult search(JobSearchRequest request) {
        requireConfigured();
        JsonNode response = fetch(request);
        JsonNode results = response.path("results");
        Map<String, Job> jobsById = new LinkedHashMap<>();
        Set<String> seenReferences = new HashSet<>();
        if (results.isArray()) {
            for (JsonNode result : results) {
                mapJob(result).filter(job -> matchesFilters(job, request)).ifPresent(job -> {
                    if (seenReferences.add(job.getProviderReference())) {
                        jobsById.putIfAbsent(job.getSourceJobId(), job);
                    }
                });
            }
        }
        long total = response.path("count").asLong(jobsById.size());
        List<Job> jobs = List.copyOf(jobsById.values());
        return new JobProviderSearchResult(
                jobs,
                total,
                request.page() * request.size() < total);
    }

    @Override
    public Optional<Job> findById(String sourceJobId, String keyword, String location) {
        requireConfigured();
        JobSearchRequest verificationQuery = new JobSearchRequest(
                keyword, location, null, null, null, null, 1, 50);
        JsonNode results = fetch(verificationQuery).path("results");
        if (!results.isArray()) return Optional.empty();
        for (JsonNode result : results) {
            if (sourceJobId.equals(result.path("id").asText())) {
                return mapJob(result);
            }
        }
        return Optional.empty();
    }

    private JsonNode fetch(JobSearchRequest request) {
        int page = request.page();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(API_URL)
                .pathSegment(country, "search", Integer.toString(page))
                .queryParam("app_id", appId)
                .queryParam("app_key", appKey)
                .queryParam("results_per_page", request.size())
                .queryParam("content-type", "application/json");
        if (hasText(request.keyword())) builder.queryParam("what", request.keyword().trim());
        if (hasText(request.location())) builder.queryParam("where", request.location().trim());
        if (request.salaryMin() != null) builder.queryParam("salary_min", request.salaryMin());
        if (request.jobType() == JobType.FULL_TIME) builder.queryParam("full_time", 1);
        if (request.jobType() == JobType.PART_TIME) builder.queryParam("part_time", 1);

        try {
            JsonNode response = restClient.get()
                    .uri(builder.build().encode().toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.path("results").isArray()) {
                throw new JobProviderException(
                        HttpStatusCode.valueOf(502), "The job provider returned an invalid response.");
            }
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 429) {
                throw new JobProviderException(
                        HttpStatusCode.valueOf(429), "The job provider is rate-limited. Please try again later.");
            }
            throw new JobProviderException(
                    HttpStatusCode.valueOf(503), "The job provider is temporarily unavailable.");
        } catch (ResourceAccessException exception) {
            throw new JobProviderException(
                    HttpStatusCode.valueOf(503), "The job provider timed out or could not be reached.");
        } catch (JobProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new JobProviderException(
                    HttpStatusCode.valueOf(503), "The job provider is temporarily unavailable.");
        }
    }

    private Optional<Job> mapJob(JsonNode result) {
        String sourceJobId = result.path("id").asText("");
        String title = cleanText(result.path("title").asText(""));
        String reference = safeProviderReference(result.path("redirect_url").asText(""));
        if (sourceJobId.isBlank() || title.isBlank() || reference == null) return Optional.empty();

        String description = cleanText(result.path("description").asText(""));
        Instant fetchedAt = Instant.now();
        Instant postedAt = parseInstant(result.path("created").asText(null));
        double salaryMin = result.path("salary_min").asDouble(Double.NaN);
        double salaryMax = result.path("salary_max").asDouble(Double.NaN);
        String company = cleanText(result.path("company").path("display_name").asText(""));
        String location = cleanText(result.path("location").path("display_name").asText(""));
        JobType jobType = detectJobType(result, title, description);
        return Optional.of(new Job(
                "adzuna-" + sourceJobId,
                sourceName(),
                sourceJobId,
                title,
                company.isBlank() ? "Not provided" : company,
                location.isBlank() ? "Not provided" : location,
                detectWorkMode(title, description),
                detectExperience(title, description),
                jobType,
                mentionedSkills(title + " " + description),
                description,
                Double.isFinite(salaryMin) ? salaryMin : null,
                Double.isFinite(salaryMax) ? salaryMax : null,
                null,
                result.path("salary_is_predicted").asInt(0) == 1,
                postedAt,
                fetchedAt,
                fetchedAt,
                JobVerificationStatus.SOURCE_LISTED,
                reference,
                null));
    }

    private boolean matchesFilters(Job job, JobSearchRequest request) {
        return (request.workMode() == null || request.workMode() == job.getWorkMode())
                && (request.experience() == null || request.experience() == job.getExperience())
                && (request.jobType() == null || request.jobType() == job.getJobType())
                && (request.salaryMin() == null
                        || (job.getSalaryMax() != null && job.getSalaryMax() >= request.salaryMin()));
    }

    private JobType detectJobType(JsonNode result, String title, String description) {
        String contractTime = result.path("contract_time").asText("").toLowerCase(Locale.ROOT);
        String contractType = result.path("contract_type").asText("").toLowerCase(Locale.ROOT);
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        if (text.contains("internship") || text.contains("intern ")) return JobType.INTERNSHIP;
        if (contractTime.equals("part_time")) return JobType.PART_TIME;
        if (contractTime.equals("full_time") || contractType.equals("permanent")) return JobType.FULL_TIME;
        if (contractType.equals("contract")) return JobType.CONTRACT;
        return null;
    }

    private JobWorkMode detectWorkMode(String title, String description) {
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        if (Pattern.compile("\\bhybrid\\b").matcher(text).find()) return JobWorkMode.HYBRID;
        if (Pattern.compile("\\b(remote|work from home|home-based)\\b").matcher(text).find()) {
            return JobWorkMode.REMOTE;
        }
        if (Pattern.compile("\\b(on-site|onsite|office-based)\\b").matcher(text).find()) {
            return JobWorkMode.ON_SITE;
        }
        return null;
    }

    private JobExperience detectExperience(String title, String description) {
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        if (Pattern.compile("\\b(fresher|fresh graduate)\\b").matcher(text).find()) return JobExperience.FRESHER;
        if (Pattern.compile("\\b(entry[- ]level|graduate role)\\b").matcher(text).find()) {
            return JobExperience.ENTRY_LEVEL;
        }
        if (Pattern.compile("\\b(1\\s*(to|-|–)\\s*3|one to three) years?").matcher(text).find()) {
            return JobExperience.ONE_TO_THREE_YEARS;
        }
        if (Pattern.compile("\\b(3\\s*(to|-|–)\\s*5|three to five) years?").matcher(text).find()) {
            return JobExperience.THREE_TO_FIVE_YEARS;
        }
        return null;
    }

    private List<String> mentionedSkills(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return SKILLS.stream()
                .filter(skill -> Pattern.compile("(?<![a-z0-9])" + Pattern.quote(skill) + "(?![a-z0-9])")
                        .matcher(normalized)
                        .find())
                .sorted()
                .limit(12)
                .toList();
    }

    private String safeProviderReference(String value) {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            if (!uri.isAbsolute()
                    || uri.getRawUserInfo() != null
                    || host == null
                    || !ALLOWED_ADZUNA_HOST.matcher(host).matches()
                    || (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443)
                    || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                return null;
            }
            return new URI(
                    "https",
                    null,
                    host.toLowerCase(Locale.ROOT),
                    uri.getPort() == 80 || uri.getPort() == 443 ? -1 : uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    null).toASCIIString();
        } catch (Exception exception) {
            return null;
        }
    }

    private String cleanText(String value) {
        String cleaned = HtmlUtils.htmlUnescape(HTML_TAG.matcher(value).replaceAll(" "))
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned.length() > 5000 ? cleaned.substring(0, 5000) : cleaned;
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String adzunaDomain(String countryCode) {
        return switch (countryCode) {
            case "gb" -> "https://www.adzuna.co.uk";
            case "us" -> "https://www.adzuna.com";
            case "au" -> "https://www.adzuna.com.au";
            case "ca" -> "https://www.adzuna.ca";
            case "in" -> "https://www.adzuna.in";
            case "nz" -> "https://www.adzuna.co.nz";
            case "za" -> "https://www.adzuna.co.za";
            case "de" -> "https://www.adzuna.de";
            case "fr" -> "https://www.adzuna.fr";
            case "es" -> "https://www.adzuna.es";
            case "it" -> "https://www.adzuna.it";
            case "nl" -> "https://www.adzuna.nl";
            case "be" -> "https://www.adzuna.be";
            case "pl" -> "https://www.adzuna.pl";
            case "at" -> "https://www.adzuna.at";
            case "ae" -> "https://www.adzuna.ae";
            case "ch" -> "https://www.adzuna.ch";
            case "ru" -> "https://www.adzuna.ru";
            case "br" -> "https://www.adzuna.com.br";
            case "sg" -> "https://www.adzuna.com.sg";
            default -> "https://www.adzuna.com";
        };
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new JobProviderException(
                    HttpStatusCode.valueOf(503),
                    "Live job search is unavailable. Configure ADZUNA_APP_ID and ADZUNA_APP_KEY on the backend.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static class JobProviderException extends RuntimeException {
        private final HttpStatusCode status;

        public JobProviderException(HttpStatusCode status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatusCode getStatus() {
            return status;
        }
    }
}
