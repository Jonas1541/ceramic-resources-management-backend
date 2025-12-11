package com.jonasdurau.ceramicmanagement.company.dto;

import java.time.Instant;

public record DeletionStatusResponseDTO(
    boolean isMarkedForDeletion,
    Instant deletionScheduledAt
) {}
