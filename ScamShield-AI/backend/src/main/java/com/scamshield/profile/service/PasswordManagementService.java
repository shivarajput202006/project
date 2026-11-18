package com.scamshield.profile.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.profile.dto.ChangePasswordRequest;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.notifications.NotificationEventPublisher;
import com.scamshield.notifications.NotificationType;

@Service
public class PasswordManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired(required = false)
    public void setNotificationEventPublisher(NotificationEventPublisher publisher) {
        this.notificationEventPublisher = publisher;
    }

    public PasswordManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void change(UserPrincipal principal, ChangePasswordRequest request) {
        String userId = principal.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ProfileException.notFound("The authenticated account could not be found."));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ProfileException.unauthorized("Current password is incorrect.");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw ProfileException.badRequest("New password and confirmation do not match.");
        }
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ProfileException.badRequest("New password must be no more than 72 UTF-8 bytes.");
        }
        String password = request.newPassword();
        if (password.equals(request.currentPassword())
                || !password.matches("(?s).{12,}")
                || !password.matches("(?s).*\\p{Lu}.*")
                || !password.matches("(?s).*\\p{Ll}.*")
                || !password.matches("(?s).*\\d.*")
                || !password.matches("(?s).*[^\\p{L}\\p{N}].*")) {
            throw ProfileException.badRequest(
                    "Choose a new password with at least 12 characters, including uppercase, lowercase, "
                            + "a number, and a symbol.");
        }
        user.updatePasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        if (notificationEventPublisher != null) {
            notificationEventPublisher.publish(
                    userId,
                    "SECURITY:PASSWORD_CHANGED:" + userId + ":" + user.getTokenVersion(),
                    NotificationType.SECURITY,
                    "Password changed",
                    "Your account password was updated. If you did not make this change, secure your account.",
                    "/settings");
        }
    }
}
