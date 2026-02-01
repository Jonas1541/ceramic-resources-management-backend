package com.jonasdurau.ceramicmanagement.employee.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.jonasdurau.ceramicmanagement.employee.category.dto.EmployeeCategoryResponseDTO;

public record EmployeeResponseDTO(
        Long id,
        Instant createdAt,
        Instant updatedAt,
        String name,
        List<EmployeeCategoryResponseDTO> categories,
        BigDecimal costPerHour) {
}
