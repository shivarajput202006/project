package com.scamshield.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.admin.dto.AdminSystemHealthResponse;
import com.scamshield.admin.service.AdminSystemHealthService;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {
    private final AdminSystemHealthService healthService;

    public AdminSystemController(AdminSystemHealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public AdminSystemHealthResponse health() {
        return healthService.getHealth();
    }
}
