package com.jonasdurau.ceramicmanagement.glazefiring.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;

public record GlazeFiringResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    double temperature,
    double burnTime,
    double coolingTime,
    double gasConsumption,
    String kilnName,
    List<GlostResponseDTO> glosts,
    List<EmployeeUsageResponseDTO> employeeUsages,
    BigDecimal employeeTotalCost,
    BigDecimal cost
) {}
