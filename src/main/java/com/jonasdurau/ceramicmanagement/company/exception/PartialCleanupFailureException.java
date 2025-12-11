package com.jonasdurau.ceramicmanagement.company.exception;

import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;

public class PartialCleanupFailureException extends BusinessException {
    private final int successCount;
    private final int failureCount;

    public PartialCleanupFailureException(int successCount, int failureCount, String message) {
        super(message);
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
}
