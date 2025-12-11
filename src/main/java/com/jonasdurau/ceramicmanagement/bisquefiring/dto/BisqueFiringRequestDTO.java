package com.jonasdurau.ceramicmanagement.bisquefiring.dto;

import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record BisqueFiringRequestDTO(
    @Positive(message = "A temperatura deve ser positiva")
    double temperature,
    @Positive(message = "O tempo de queima deve ser positivo")
    double burnTime,
    @PositiveOrZero(message = "O tempo de resfriamento deve ser maior ou igual a zero")
    double coolingTime,
    @NotEmpty(message = "A queima deve ter produtos")
    List<@Positive(message = "O id dos produtos devem ser positivos") Long> biscuits,
    @NotEmpty(message = "A lista de funcionários não pode estar vazia.")
    @Valid
    List<EmployeeUsageRequestDTO> employeeUsages
) {}
