package com.jonasdurau.ceramicmanagement.employee.category.dto;

import jakarta.validation.constraints.NotBlank;

public record EmployeeCategoryRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name
) {}
