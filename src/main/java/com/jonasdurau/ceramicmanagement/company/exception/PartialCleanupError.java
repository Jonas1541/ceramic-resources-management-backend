package com.jonasdurau.ceramicmanagement.company.exception;

import java.time.Instant;

import com.jonasdurau.ceramicmanagement.shared.exception.StandardError;

public class PartialCleanupError extends StandardError {
    private int successCount;
    private int failureCount;

    public PartialCleanupError(Instant timestamp, int status, String error, String message, String path, int successCount, int failureCount) {
        super(timestamp, status, error, message, path);
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }
}