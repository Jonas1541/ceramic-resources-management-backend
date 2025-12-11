package com.jonasdurau.ceramicmanagement.employee.category.dto;

import java.time.Instant;

public record EmployeeCategoryResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name
) {}
