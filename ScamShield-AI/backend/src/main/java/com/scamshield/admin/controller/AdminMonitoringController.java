package com.scamshield.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.admin.dto.AdminSecurityEventResponse;
import com.scamshield.admin.service.AdminMonitoringService;

@RestController
@RequestMapping("/api/admin/monitoring")
public class AdminMonitoringController {
    private final AdminMonitoringService monitoringService;

    public AdminMonitoringController(AdminMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/security-events")
    public List<AdminSecurityEventResponse> securityEvents() {
        return monitoringService.getSecurityEvents();
    }
}
