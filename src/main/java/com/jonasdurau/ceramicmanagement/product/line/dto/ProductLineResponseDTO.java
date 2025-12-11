package com.jonasdurau.ceramicmanagement.product.line.dto;

import java.time.Instant;

public record ProductLineResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    int productQuantity
) {}
