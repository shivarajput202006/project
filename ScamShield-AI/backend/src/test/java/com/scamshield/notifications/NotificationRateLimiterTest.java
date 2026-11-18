package com.scamshield.notifications;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NotificationRateLimiterTest {
    @Test
    void enforcesSixtyActionsPerMinuteForEachUser() {
        NotificationRateLimiter limiter = new NotificationRateLimiter();
        for (int attempt = 0; attempt < 60; attempt++) {
            limiter.check("user-1");
        }

        assertThatThrownBy(() -> limiter.check("user-1"))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Too many notification actions");
    }

    @Test
    void keepsRateLimitsScopedToEachUser() {
        NotificationRateLimiter limiter = new NotificationRateLimiter();
        for (int attempt = 0; attempt < 60; attempt++) {
            limiter.check("user-1");
        }

        limiter.check("user-2");
    }
}
