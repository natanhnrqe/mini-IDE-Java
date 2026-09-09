package com.eyecode.lessons.practice;

import java.util.Objects;

public record PracticeVerificationResult(PracticeVerificationStatus status, String message) {
    public PracticeVerificationResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(message, "message");
    }

    public boolean successful() {
        return status == PracticeVerificationStatus.SUCCESS;
    }
}
