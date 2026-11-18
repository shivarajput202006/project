package com.scamshield.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;

import org.junit.jupiter.api.Test;

class LimitedBodyHandlerTest {
    @Test
    void acceptsResponsesWithinLimit() {
        var subscriber = LimitedBodyHandler.upTo(5).apply(null);
        subscriber.onSubscribe(new TestSubscription());
        subscriber.onNext(List.of(ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8))));
        subscriber.onComplete();

        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), subscriber.getBody().toCompletableFuture().join());
    }

    @Test
    void cancelsResponsesThatExceedLimit() {
        TestSubscription subscription = new TestSubscription();
        HttpResponse.BodySubscriber<byte[]> subscriber = LimitedBodyHandler.upTo(4).apply(null);
        subscriber.onSubscribe(subscription);
        subscriber.onNext(List.of(ByteBuffer.wrap("12345".getBytes(StandardCharsets.UTF_8))));

        CompletionException exception = assertThrows(
                CompletionException.class, () -> subscriber.getBody().toCompletableFuture().join());
        assertTrue(exception.getCause() instanceof IOException);
        assertTrue(subscription.cancelled);
    }

    private static final class TestSubscription implements Flow.Subscription {
        private boolean cancelled;

        @Override
        public void request(long amount) {
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
