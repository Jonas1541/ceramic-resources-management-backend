package com.jonasdurau.ceramicmanagement.shared.dto;

import jakarta.validation.constraints.Positive;

public record EmployeeUsageRequestDTO(
    @Positive(message = "O tempo de trabalho deve ser positivo")
    double usageTime,
    @Positive(message = "O ID do funcionário deve ser positivo.")
    long employeeId
) {}
