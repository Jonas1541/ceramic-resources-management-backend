package com.jonasdurau.ceramicmanagement.machine.dto;

import java.time.Instant;

public record MachineResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    double power
) {}
