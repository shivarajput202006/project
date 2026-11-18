package com.scamshield.scamdetector.util;

import com.scamshield.scamdetector.model.InputType;

public enum DetectedFileType {
    PNG("image/png", InputType.IMAGE),
    JPEG("image/jpeg", InputType.IMAGE),
    WEBP("image/webp", InputType.IMAGE),
    PDF("application/pdf", InputType.PDF),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", InputType.DOCX);

    private final String mimeType;
    private final InputType inputType;

    DetectedFileType(String mimeType, InputType inputType) {
        this.mimeType = mimeType;
        this.inputType = inputType;
    }

    public String mimeType() {
        return mimeType;
    }

    public InputType inputType() {
        return inputType;
    }
}
