package com.scamshield.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import com.scamshield.security.JwtService;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

class AuthControllerTest {
    @Test
    void logoutInvalidatesServerSessionAndExpiresSecureCookie() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, mock(JwtService.class), true);
        User user = new User("Test User", "test@example.com", "password-hash", UserRole.USER, Instant.now());
        MockHttpServletResponse response = new MockHttpServletResponse();

        var result = controller.logout(new UserPrincipal(user), response);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("HttpOnly", "Secure", "SameSite=Lax", "Max-Age=0");
        verify(authService).logout(user);
    }

    @Test
    void logoutDoesNotExpireCookieWhenSessionInvalidationFails() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, mock(JwtService.class), true);
        User user = new User("Test User", "test@example.com", "password-hash", UserRole.USER, Instant.now());
        doThrow(new IllegalStateException("Persistence failed")).when(authService).logout(user);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> controller.logout(new UserPrincipal(user), response))
                .isInstanceOf(IllegalStateException.class);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }
}
