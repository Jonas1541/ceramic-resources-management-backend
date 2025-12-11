package com.jonasdurau.ceramicmanagement.resource.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;

public record ResourceResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    ResourceCategory category,
    BigDecimal unitValue,
    double currentQuantity,
    BigDecimal currentQuantityPrice
) {}
