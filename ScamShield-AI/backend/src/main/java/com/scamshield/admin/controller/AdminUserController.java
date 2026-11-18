package com.scamshield.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.admin.dto.AdminPageResponse;
import com.scamshield.admin.dto.AdminUserResponse;
import com.scamshield.admin.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService userService;

    public AdminUserController(AdminUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public AdminPageResponse<AdminUserResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.list(search, page, size);
    }
}
