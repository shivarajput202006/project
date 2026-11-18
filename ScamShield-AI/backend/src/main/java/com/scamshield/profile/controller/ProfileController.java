package com.scamshield.profile.controller;

import java.time.Duration;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.scamshield.profile.dto.ChangePasswordRequest;
import com.scamshield.profile.dto.DeleteAccountRequest;
import com.scamshield.profile.dto.EducationRequest;
import com.scamshield.profile.dto.ProfilePhotoResponse;
import com.scamshield.profile.dto.ProfileResponse;
import com.scamshield.profile.dto.UpdateProfileRequest;
import com.scamshield.profile.dto.UpdateSkillsRequest;
import com.scamshield.profile.model.Education;
import com.scamshield.profile.model.ProfileSkill;
import com.scamshield.profile.service.AccountDataExportService;
import com.scamshield.profile.service.AccountManagementService;
import com.scamshield.profile.service.EducationService;
import com.scamshield.profile.service.PasswordManagementService;
import com.scamshield.profile.service.ProfilePhotoService;
import com.scamshield.profile.service.ProfilePhotoService.PhotoContent;
import com.scamshield.profile.service.ProfileService;
import com.scamshield.profile.service.SkillsService;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.security.JwtAuthenticationFilter;
import com.scamshield.security.UserPrincipal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final EducationService educationService;
    private final SkillsService skillsService;
    private final ProfilePhotoService photoService;
    private final PasswordManagementService passwordService;
    private final AccountDataExportService exportService;
    private final AccountManagementService accountService;
    private final boolean secureCookie;

    public ProfileController(
            ProfileService profileService,
            EducationService educationService,
            SkillsService skillsService,
            ProfilePhotoService photoService,
            PasswordManagementService passwordService,
            AccountDataExportService exportService,
            AccountManagementService accountService,
            @Value("${app.auth.cookie-secure}") boolean secureCookie) {
        this.profileService = profileService;
        this.educationService = educationService;
        this.skillsService = skillsService;
        this.photoService = photoService;
        this.passwordService = passwordService;
        this.exportService = exportService;
        this.accountService = accountService;
        this.secureCookie = secureCookie;
    }

    @GetMapping("/me")
    public ProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.get(principal);
    }

    @PutMapping("/me")
    public ProfileResponse updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.update(principal, request);
    }

    @GetMapping("/education")
    public List<Education> getEducation(@AuthenticationPrincipal UserPrincipal principal) {
        return educationService.list(userId(principal));
    }

    @PostMapping("/education")
    public ResponseEntity<Education> addEducation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.status(201).body(educationService.add(userId(principal), request));
    }

    @PutMapping("/education/{id}")
    public Education updateEducation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody EducationRequest request) {
        return educationService.update(userId(principal), id, request);
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<Void> deleteEducation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        educationService.delete(userId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/skills")
    public List<ProfileSkill> updateSkills(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateSkillsRequest request) {
        return skillsService.replace(userId(principal), request);
    }

    @PostMapping("/photo")
    public ProfilePhotoResponse uploadPhoto(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        return photoService.upload(userId(principal), file);
    }

    @GetMapping("/photo")
    public ResponseEntity<ByteArrayResource> getPhoto(@AuthenticationPrincipal UserPrincipal principal) {
        PhotoContent photo = photoService.get(userId(principal));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(new ByteArrayResource(photo.bytes()));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto(@AuthenticationPrincipal UserPrincipal principal) {
        photoService.delete(userId(principal));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response) {
        passwordService.change(principal, request);
        expireAuthCookie(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/data-export")
    public ResponseEntity<ByteArrayResource> exportData(@AuthenticationPrincipal UserPrincipal principal) {
        byte[] archive = exportService.create(userId(principal));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(archive.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("scamshield-account-export.zip").build().toString())
                .cacheControl(CacheControl.noStore())
                .body(new ByteArrayResource(archive));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletResponse response) {
        accountService.deleteAccount(principal, request);
        expireAuthCookie(response);
        return ResponseEntity.noContent().build();
    }

    private String userId(UserPrincipal principal) {
        if (principal == null || principal.getUser() == null
                || principal.getUser().getId() == null || principal.getUser().getId().isBlank()) {
            throw new IllegalStateException("Authenticated user has no database identity.");
        }
        return principal.getUser().getId();
    }

    private void expireAuthCookie(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }
}
