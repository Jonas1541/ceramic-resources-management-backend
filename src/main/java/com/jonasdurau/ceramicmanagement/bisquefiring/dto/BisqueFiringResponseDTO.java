package com.jonasdurau.ceramicmanagement.bisquefiring.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;

public record BisqueFiringResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    double temperature,
    double burnTime,
    double coolingTime,
    double gasConsumption,
    String kilnName,
    List<BiscuitResponseDTO> biscuits,
    List<EmployeeUsageResponseDTO> employeeUsages,
    BigDecimal employeeTotalCost,
    BigDecimal cost
) {}
