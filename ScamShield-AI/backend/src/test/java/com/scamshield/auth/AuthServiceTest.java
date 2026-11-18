package com.scamshield.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.scamshield.auth.dto.LoginRequest;
import com.scamshield.auth.dto.RegisterRequest;
import com.scamshield.security.JwtService;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String PASSWORD = "Correct horse 42!";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerSavesUserWithBcryptHashAndIssuesToken() {
        RegisterRequest request = new RegisterRequest(
                "  Morgan Lee  ", "  Morgan@example.com ", PASSWORD, PASSWORD);
        when(userRepository.existsByEmail("morgan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-test-token");

        AuthService.AuthResult result = authService.register(request);

        assertThat(result.user().getEmail()).isEqualTo("morgan@example.com");
        assertThat(result.user().getFullName()).isEqualTo("Morgan Lee");
        assertThat(result.user().getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, result.user().getPasswordHash())).isTrue();
        assertThat(result.user().getRole()).isEqualTo(UserRole.USER);
        assertThat(result.token()).isEqualTo("signed-test-token");
    }

    @Test
    void duplicateEmailIsRejectedBeforeSaving() {
        when(userRepository.existsByEmail("morgan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(AuthService.EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrationIgnoresSubmittedAdminRole() throws Exception {
        RegisterRequest request = new ObjectMapper().readValue(
                """
                {
                  "fullName": "Morgan Lee",
                  "email": "morgan@example.com",
                  "password": "Correct horse 42!",
                  "confirmPassword": "Correct horse 42!",
                  "role": "ADMIN"
                }
                """,
                RegisterRequest.class);
        when(userRepository.existsByEmail("morgan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-test-token");

        AuthService.AuthResult result = authService.register(request);

        assertThat(result.user().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void loginAcceptsMatchingPassword() {
        User user = savedUser(PASSWORD);
        when(userRepository.findByEmail("morgan@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("signed-test-token");

        AuthService.AuthResult result =
                authService.login(new LoginRequest("Morgan@example.com", PASSWORD));

        assertThat(result.user()).isSameAs(user);
        assertThat(result.token()).isEqualTo("signed-test-token");
    }

    @Test
    void loginRejectsIncorrectPasswordWithGenericMessage() {
        User user = savedUser(PASSWORD);
        when(userRepository.findByEmail("morgan@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("Morgan@example.com", "Wrong password 43!")))
                .isInstanceOf(AuthService.InvalidCredentialsException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void logoutInvalidatesExistingTokenVersions() {
        User user = savedUser(PASSWORD);
        when(userRepository.save(user)).thenReturn(user);

        authService.logout(user);

        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    void registrationRejectsMismatchedConfirmation() {
        RegisterRequest request = new RegisterRequest(
                "Morgan Lee", "morgan@example.com", PASSWORD, "A different password 43!");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthService.InvalidPasswordException.class)
                .hasMessage("Passwords do not match.");
        verify(userRepository, never()).save(any(User.class));
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest("Morgan Lee", "Morgan@example.com", PASSWORD, PASSWORD);
    }

    private User savedUser(String password) {
        return new User(
                "Morgan Lee",
                "morgan@example.com",
                passwordEncoder.encode(password),
                UserRole.USER,
                Instant.now());
    }
}
