package com.jonasdurau.ceramicmanagement.company.dto;

public record CleanupResultDTO(
    int deletedCount,
    int failedCount,
    String message
) {}