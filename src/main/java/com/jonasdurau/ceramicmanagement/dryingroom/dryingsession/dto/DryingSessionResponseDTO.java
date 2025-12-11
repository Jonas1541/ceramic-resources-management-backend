package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;

public record DryingSessionResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    double hours,
    List<EmployeeUsageResponseDTO> employeeUsages,
    BigDecimal employeeTotalCost,
    BigDecimal costAtTime
) {}
