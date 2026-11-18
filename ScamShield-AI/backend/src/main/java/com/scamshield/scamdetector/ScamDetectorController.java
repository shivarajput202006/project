package com.scamshield.scamdetector;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.scamshield.scamdetector.dto.ScamAnalysisRequest;
import com.scamshield.scamdetector.dto.ScamAnalysisResponse;
import com.scamshield.scamdetector.service.ScamDetectorService;
import com.scamshield.security.UserPrincipal;

@RestController
@RequestMapping("/api/scam-detector")
public class ScamDetectorController {
    private final ScamDetectorService scamDetectorService;

    public ScamDetectorController(ScamDetectorService scamDetectorService) {
        this.scamDetectorService = scamDetectorService;
    }

    @PostMapping(path = "/analyze/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScamAnalysisResponse analyzeText(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScamAnalysisRequest.Text request) {
        return scamDetectorService.analyzeText(principal, request);
    }

    @PostMapping(path = "/analyze/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScamAnalysisResponse analyzeEmail(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScamAnalysisRequest.EmailMessage request) {
        return scamDetectorService.analyzeEmail(principal, request);
    }

    @PostMapping(path = "/analyze/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScamAnalysisResponse analyzeUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScamAnalysisRequest.Url request) {
        return scamDetectorService.analyzeUrl(principal, request);
    }

    @PostMapping(path = "/analyze/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ScamAnalysisResponse analyzeFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file) {
        return scamDetectorService.analyzeFile(principal, file);
    }

    @GetMapping("/history")
    public List<ScamAnalysisResponse> history(@AuthenticationPrincipal UserPrincipal principal) {
        return scamDetectorService.getHistory(principal);
    }

    @GetMapping("/{id}")
    public ScamAnalysisResponse report(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return scamDetectorService.getReport(principal, id);
    }
}
