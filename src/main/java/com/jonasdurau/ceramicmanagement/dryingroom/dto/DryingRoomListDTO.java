package com.jonasdurau.ceramicmanagement.dryingroom.dto;

import java.time.Instant;

public record DryingRoomListDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    double gasConsumptionPerHour
) {}
