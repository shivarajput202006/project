package com.scamshield.profile.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class ProfileValidationUtilTest {
    @Test
    void acceptsDecodablePngAndRejectsSpoofedImageSignature() {
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/r7sAAAAASUVORK5CYII=");
        assertEquals("image/png", ProfileValidationUtil.validatePhoto(png, "image/png"));
        assertThrows(
                ProfileException.class,
                () -> ProfileValidationUtil.validatePhoto(
                        new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1},
                        "image/png"));
    }

    @Test
    void rejectsDeclaredContentTypeMismatch() {
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/r7sAAAAASUVORK5CYII=");
        assertThrows(
                ProfileException.class,
                () -> ProfileValidationUtil.validatePhoto(png, "image/jpeg"));
    }
}
