package com.jonasdurau.ceramicmanagement.employee.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmployeeRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String name,
        @NotNull(message = "Pelo menos uma categoria é obrigatória") // Assuming we want at least one
        List<Long> categoryIds,
        @NotNull(message = "O custo por hora é obrigatório") @Positive(message = "O custo por hora deve ser positivo") BigDecimal costPerHour) {
}
