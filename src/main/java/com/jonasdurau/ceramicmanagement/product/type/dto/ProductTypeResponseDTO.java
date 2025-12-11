package com.jonasdurau.ceramicmanagement.product.type.dto;

import java.time.Instant;

public record ProductTypeResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    int productQuantity
) {}
