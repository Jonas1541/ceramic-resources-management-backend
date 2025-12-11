package com.jonasdurau.ceramicmanagement.glaze.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.glaze.machineusage.dto.GlazeMachineUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.dto.GlazeResourceUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;

public record GlazeResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String color,
    List<GlazeResourceUsageResponseDTO> resourceUsages,
    List<GlazeMachineUsageResponseDTO> machineUsages,
    List<EmployeeUsageResponseDTO> employeeUsages,
    BigDecimal employeeTotalCost,
    BigDecimal unitCost,
    double currentQuantity,
    BigDecimal currentQuantityPrice
) {
    public GlazeResponseDTO {
        resourceUsages = resourceUsages == null ? new ArrayList<>() : resourceUsages;
        machineUsages = machineUsages == null ? new ArrayList<>() : machineUsages;
        employeeUsages = employeeUsages == null ? new ArrayList<>() : employeeUsages;
    }
}
