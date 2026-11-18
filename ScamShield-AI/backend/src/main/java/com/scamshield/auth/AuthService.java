package com.scamshield.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.scamshield.auth.dto.LoginRequest;
import com.scamshield.auth.dto.RegisterRequest;
import com.scamshield.security.JwtService;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResult register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match.");
        }
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
        ensureBcryptCanHandle(request.password());

        User user = new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                UserRole.USER,
                Instant.now());
        try {
            User savedUser = userRepository.save(user);
            return new AuthResult(savedUser, jwtService.generateToken(savedUser));
        } catch (DuplicateKeyException exception) {
            throw new EmailAlreadyExistsException();
        }
    }

    public AuthResult login(LoginRequest request) {
        ensureBcryptCanHandle(request.password());
        String email = normalizeEmail(request.email());
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            passwordEncoder.encode(request.password());
            throw new InvalidCredentialsException();
        }

        User existingUser = user.get();
        if (!passwordEncoder.matches(request.password(), existingUser.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResult(existingUser, jwtService.generateToken(existingUser));
    }

    public void logout(User user) {
        user.invalidateSessions();
        userRepository.save(user);
    }

    private void ensureBcryptCanHandle(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidPasswordException(
                    "Password must be no more than 72 UTF-8 bytes when encoded.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthResult(User user, String token) {
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException() {
            super("An account with this email already exists.");
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password.");
        }
    }

    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }
}
