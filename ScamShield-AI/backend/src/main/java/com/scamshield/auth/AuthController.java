package com.scamshield.auth;

import java.time.Duration;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.auth.dto.AuthResponse;
import com.scamshield.auth.dto.LoginRequest;
import com.scamshield.auth.dto.RegisterRequest;
import com.scamshield.auth.dto.UserResponse;
import com.scamshield.security.JwtAuthenticationFilter;
import com.scamshield.security.JwtService;
import com.scamshield.security.UserPrincipal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            @Value("${app.auth.cookie-secure}") boolean secureCookie) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.secureCookie = secureCookie;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken, HttpServletResponse response) {
        String token = csrfToken.getToken();
        ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", token)
                .httpOnly(false)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());
        return Map.of("token", token);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request);
        addAuthCookie(response, result.token());
        return ResponseEntity.status(201).body(new AuthResponse(UserResponse.from(result.user())));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        addAuthCookie(response, result.token());
        return new AuthResponse(UserResponse.from(result.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserPrincipal principal, HttpServletResponse response) {
        authService.logout(principal.getUser());
        ResponseCookie expiredCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return new AuthResponse(UserResponse.from(principal.getUser()));
    }

    private void addAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie authCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
    }
}
