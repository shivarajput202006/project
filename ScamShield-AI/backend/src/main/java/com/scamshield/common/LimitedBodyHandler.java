package com.scamshield.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public final class LimitedBodyHandler {
    private LimitedBodyHandler() {
    }

    public static HttpResponse.BodyHandler<byte[]> upTo(int maxBytes) {
        if (maxBytes < 1) {
            throw new IllegalArgumentException("The response body limit must be positive.");
        }
        return ignored -> new LimitedBodySubscriber(maxBytes);
    }

    private static final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final int maxBytes;
        private final ByteArrayOutputStream content = new ByteArrayOutputStream();
        private final CompletableFuture<byte[]> body = new CompletableFuture<>();
        private Flow.Subscription subscription;

        private LimitedBodySubscriber(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        @Override
        public CompletionStage<byte[]> getBody() {
            return body;
        }

        @Override
        public void onSubscribe(Flow.Subscription value) {
            subscription = value;
            subscription.request(1);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            long nextSize = content.size();
            for (ByteBuffer buffer : buffers) {
                nextSize += buffer.remaining();
                if (nextSize > maxBytes) {
                    subscription.cancel();
                    body.completeExceptionally(new IOException("Provider response exceeded the size limit."));
                    return;
                }
            }
            for (ByteBuffer buffer : buffers) {
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk);
                content.writeBytes(chunk);
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable error) {
            body.completeExceptionally(error);
        }

        @Override
        public void onComplete() {
            body.complete(content.toByteArray());
        }
    }
}
