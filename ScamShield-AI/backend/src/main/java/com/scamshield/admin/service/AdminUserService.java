package com.scamshield.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.scamshield.admin.dto.AdminPageResponse;
import com.scamshield.admin.dto.AdminUserResponse;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;

@Service
public class AdminUserService {
    private static final int MAX_PAGE_SIZE = 100;
    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AdminPageResponse<AdminUserResponse> list(String search, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(MAX_PAGE_SIZE, size));
        String term = search == null ? "" : search.trim();
        if (term.length() > 100) {
            term = term.substring(0, 100);
        }
        PageRequest pageable = PageRequest.of(
                safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = term.isBlank()
                ? userRepository.findAll(pageable)
                : userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        term, term, pageable);
        return AdminPageResponse.from(users, AdminUserResponse::from);
    }
}
