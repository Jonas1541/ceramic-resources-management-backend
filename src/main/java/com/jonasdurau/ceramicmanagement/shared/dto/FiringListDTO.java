package com.jonasdurau.ceramicmanagement.shared.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FiringListDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    double temperature,
    double burnTime,
    double coolingTime,
    double gasConsumption,
    String kilnName,
    BigDecimal cost
) {}
