package com.jonasdurau.ceramicmanagement.product.transaction.dto;

import java.util.List;

import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ProductTransactionRequestDTO(
    @NotEmpty(message = "A lista de funcionários não pode estar vazia.")
    @Valid
    List<EmployeeUsageRequestDTO> employeeUsages
) {}
