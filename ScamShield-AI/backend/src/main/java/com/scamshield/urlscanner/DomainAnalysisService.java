package com.scamshield.urlscanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class DomainAnalysisService {
    private static final Set<String> KNOWN_BRANDS =
            Set.of("amazon", "apple", "facebook", "google", "indeed", "linkedin", "microsoft", "paypal");
    private static final Set<String> SHORTENERS =
            Set.of("bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly", "cutt.ly");

    public Analysis analyze(SafeURLValidator.ValidatedUrl url) {
        String domain = url.normalizedDomain().toLowerCase(Locale.ROOT);
        String[] labels = domain.split("\\.");
        List<URLRiskIndicator> indicators = new ArrayList<>();

        if (!"DOMAIN".equals(url.hostnameType())) {
            indicators.add(new URLRiskIndicator(
                    "IP_HOST",
                    "IP address used as the host",
                    "An IP address is used instead of a named domain. This is a review heuristic, not proof of malicious activity.",
                    "MEDIUM",
                    true));
        }
        if (labels.length >= 5) {
            indicators.add(new URLRiskIndicator(
                    "DEEP_SUBDOMAIN",
                    "Many subdomain levels",
                    "This hostname has several subdomain levels. Complex hostnames can be harder to verify, but are not proof of fraud.",
                    "LOW",
                    true));
        }
        if (SHORTENERS.contains(domain)) {
            indicators.add(new URLRiskIndicator(
                    "URL_SHORTENER",
                    "URL shortener",
                    "This domain is commonly used to shorten links, which can hide the final destination. The destination was not opened.",
                    "MEDIUM",
                    true));
        }
        String lowerUrl = url.submittedUrl().toLowerCase(Locale.ROOT);
        if (hasSuspiciousUrlCharacters(lowerUrl)) {
            indicators.add(new URLRiskIndicator(
                    "SUSPICIOUS_URL_CHARACTERS",
                    "Potentially deceptive URL characters",
                    "The URL contains characters or encodings sometimes used to obscure a destination. Encoded characters can also be legitimate.",
                    "MEDIUM",
                    true));
        }
        if (domain.startsWith("xn--") || domain.contains(".xn--")) {
            indicators.add(new URLRiskIndicator(
                    "IDN_PUNYCODE",
                    "Internationalized domain encoding",
                    "The hostname contains an IDN punycode label. Internationalized names can be legitimate, but visual lookalikes are possible.",
                    "LOW",
                    true));
        }
        if (isMixedScript(url.originalHostname())) {
            indicators.add(new URLRiskIndicator(
                    "MIXED_SCRIPT",
                    "Mixed writing systems in hostname",
                    "The hostname combines writing systems. This can occur legitimately but may also be used for visual impersonation.",
                    "MEDIUM",
                    true));
        }
        if (hasLookalikeLabel(labels)) {
            indicators.add(new URLRiskIndicator(
                    "LOOKALIKE_LABEL",
                    "Possible lookalike brand text",
                    "A hostname label is one character away from a small built-in list of well-known brand names. This is only a heuristic and may be a false positive.",
                    "MEDIUM",
                    true));
        }
        return new Analysis(List.copyOf(indicators));
    }

    private boolean hasSuspiciousUrlCharacters(String url) {
        return url.contains("%2f")
                || url.contains("%5c")
                || url.contains("%40")
                || url.contains("%00")
                || url.codePoints().anyMatch(point ->
                        point == 0x200b || point == 0x202e || point == 0x2066 || point == 0x2067 || point == 0x2068)
                || hasAtSignOutsideAuthority(url);
    }

    private boolean hasAtSignOutsideAuthority(String url) {
        int scheme = url.indexOf("://");
        if (scheme < 0) {
            return false;
        }
        int pathStart = url.indexOf('/', scheme + 3);
        int queryStart = url.indexOf('?', scheme + 3);
        int suffixStart = pathStart < 0 ? queryStart : queryStart < 0 ? pathStart : Math.min(pathStart, queryStart);
        return suffixStart >= 0 && url.substring(suffixStart).contains("@");
    }

    private boolean hasLookalikeLabel(String[] labels) {
        for (String label : labels) {
            String candidate = label.replaceAll("[^a-z0-9]", "");
            if (candidate.length() < 5 || KNOWN_BRANDS.contains(candidate)) {
                continue;
            }
            for (String brand : KNOWN_BRANDS) {
                if (editDistanceAtMostOne(candidate, brand)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean editDistanceAtMostOne(String left, String right) {
        if (Math.abs(left.length() - right.length()) > 1) {
            return false;
        }
        int leftIndex = 0;
        int rightIndex = 0;
        int edits = 0;
        while (leftIndex < left.length() && rightIndex < right.length()) {
            if (left.charAt(leftIndex) == right.charAt(rightIndex)) {
                leftIndex++;
                rightIndex++;
                continue;
            }
            if (++edits > 1) {
                return false;
            }
            if (left.length() > right.length()) {
                leftIndex++;
            } else if (right.length() > left.length()) {
                rightIndex++;
            } else {
                leftIndex++;
                rightIndex++;
            }
        }
        return edits + (left.length() - leftIndex) + (right.length() - rightIndex) <= 1;
    }

    private boolean isMixedScript(String domain) {
        boolean latin = false;
        boolean other = false;
        for (int offset = 0; offset < domain.length();) {
            int point = domain.codePointAt(offset);
            offset += Character.charCount(point);
            if (point == '.') {
                continue;
            }
            if (point < 128) {
                if (Character.isLetter(point)) {
                    latin = true;
                }
                continue;
            }
            Character.UnicodeScript script = Character.UnicodeScript.of(point);
            if (script == Character.UnicodeScript.LATIN) {
                latin = true;
            } else if (script != Character.UnicodeScript.COMMON
                    && script != Character.UnicodeScript.INHERITED) {
                other = true;
            }
        }
        return latin && other;
    }

    public record Analysis(List<URLRiskIndicator> indicators) {
        public Analysis {
            indicators = List.copyOf(indicators);
        }
    }
}
