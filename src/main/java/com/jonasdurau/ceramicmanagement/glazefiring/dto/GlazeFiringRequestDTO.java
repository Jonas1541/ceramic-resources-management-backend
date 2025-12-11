package com.jonasdurau.ceramicmanagement.glazefiring.dto;

import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record GlazeFiringRequestDTO(
    @Positive(message = "A temperatura deve ser positiva")
    double temperature,
    @Positive(message = "O tempo de queima deve ser positivo")
    double burnTime,
    @PositiveOrZero(message = "O tempo de resfriamento deve ser maior ou igual a zero")
    double coolingTime,
    @NotEmpty(message = "A queima deve ter produtos")
    @Valid
    List<GlostRequestDTO> glosts,
    @NotEmpty(message = "A lista de funcionários não pode estar vazia.")
    @Valid
    List<EmployeeUsageRequestDTO> employeeUsages
) {}
