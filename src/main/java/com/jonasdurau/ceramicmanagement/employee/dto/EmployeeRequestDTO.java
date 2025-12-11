package com.jonasdurau.ceramicmanagement.employee.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmployeeRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,
    @Positive(message = "A categoria é obrigatória")
    long categoryId,
    @NotNull(message = "O custo por hora é obrigatório")
    @Positive(message = "O custo por hora deve ser positivo")
    BigDecimal costPerHour
) {}
