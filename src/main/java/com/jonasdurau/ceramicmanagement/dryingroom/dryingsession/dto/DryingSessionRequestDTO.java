package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.dto;

import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record DryingSessionRequestDTO(
    @Positive(message = "A quantidade de horas de uso deve ser positiva")
    double hours,
    @NotEmpty(message = "A lista de funcionários não pode estar vazia.")
    @Valid
    List<EmployeeUsageRequestDTO> employeeUsages
) {}
