package com.jonasdurau.ceramicmanagement.batch.dto;

import java.util.List;

import com.jonasdurau.ceramicmanagement.batch.machineusage.dto.BatchMachineUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.batch.resourceusage.dto.BatchResourceUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record BatchRequestDTO(
    @NotEmpty(message = "A lista de recursos não pode estar vazia.")
    @Valid
    List<BatchResourceUsageRequestDTO> resourceUsages,
    @NotEmpty(message = "A lista de máquinas não pode estar vazia.")
    @Valid
    List<BatchMachineUsageRequestDTO> machineUsages,
    @NotEmpty(message = "A lista de funcionários não pode estar vazia.")
    @Valid
    List<EmployeeUsageRequestDTO> employeeUsages
) {}
