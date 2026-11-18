package com.scamshield.profile.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.profile.dto.SettingsResponse;
import com.scamshield.profile.dto.UpdateSettingsRequest;
import com.scamshield.profile.service.SettingsService;
import com.scamshield.security.UserPrincipal;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponse getSettings(@AuthenticationPrincipal UserPrincipal principal) {
        return settingsService.get(principal.getUser().getId());
    }

    @PutMapping
    public SettingsResponse updateSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateSettingsRequest request) {
        return settingsService.update(principal.getUser().getId(), request);
    }
}
