package com.jonasdurau.ceramicmanagement.batch.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BatchListDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    BigDecimal batchFinalCost
) {}