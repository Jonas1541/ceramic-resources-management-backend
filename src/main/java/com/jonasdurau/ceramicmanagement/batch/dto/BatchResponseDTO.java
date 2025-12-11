package com.jonasdurau.ceramicmanagement.batch.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.jonasdurau.ceramicmanagement.batch.machineusage.dto.BatchMachineUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.batch.resourceusage.dto.BatchResourceUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;

public record BatchResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    List<BatchResourceUsageResponseDTO> resourceUsages,
    List<BatchMachineUsageResponseDTO> machineUsages,
    List<EmployeeUsageResponseDTO> employeeUsages,
    double batchTotalWater,
    BigDecimal batchTotalWaterCost,
    double resourceTotalQuantity,
    BigDecimal resourceTotalCost,
    double machinesEnergyConsumption,
    BigDecimal machinesEnergyConsumptionCost,
    BigDecimal employeeTotalCost,
    BigDecimal batchFinalCost,
    double weight
) {}
