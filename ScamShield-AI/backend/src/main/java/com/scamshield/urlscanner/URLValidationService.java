package com.scamshield.urlscanner;

import org.springframework.stereotype.Service;

@Service
public class URLValidationService {
    private final SafeURLValidator safeURLValidator;

    public URLValidationService(SafeURLValidator safeURLValidator) {
        this.safeURLValidator = safeURLValidator;
    }

    public SafeURLValidator.ValidatedUrl validate(String submittedUrl) {
        return safeURLValidator.validate(submittedUrl);
    }
}
