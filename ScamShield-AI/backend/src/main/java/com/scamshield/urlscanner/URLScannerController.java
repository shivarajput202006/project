package com.scamshield.urlscanner;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.security.UserPrincipal;

@RestController
@RequestMapping("/api/url-scanner")
public class URLScannerController {
    private final URLScannerService scannerService;

    public URLScannerController(URLScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping("/scan")
    public URLScanResponse scan(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody URLScanRequest request) {
        return scannerService.scan(principal, request);
    }

    @GetMapping("/history")
    public List<URLScanResponse> history(@AuthenticationPrincipal UserPrincipal principal) {
        return scannerService.history(principal);
    }

    @GetMapping("/{id}")
    public URLScanResponse get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return scannerService.get(principal, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        scannerService.delete(principal, id);
        return ResponseEntity.noContent().build();
    }
}
