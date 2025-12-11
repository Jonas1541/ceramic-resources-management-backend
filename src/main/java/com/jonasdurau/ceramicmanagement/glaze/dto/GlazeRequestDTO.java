package com.jonasdurau.ceramicmanagement.glaze.dto;

import java.util.List;

import com.jonasdurau.ceramicmanagement.glaze.machineusage.dto.GlazeMachineUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.dto.GlazeResourceUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record GlazeRequestDTO(
    @NotBlank(message = "a cor é obrigatória")
    String color,
    @NotEmpty(message = "A lista de recursos não pode estar vazia.")
    @Valid
    List<GlazeResourceUsageRequestDTO> resourceUsages,
    @NotEmpty(message = "A lista de máquinas não pode estar vazia.")
    @Valid
    List<GlazeMachineUsageRequestDTO> machineUsages,
    @NotEmpty(message = "A lista de funcionários não pode estar vazia.")
    @Valid
    List<EmployeeUsageRequestDTO> employeeUsages
) {}
