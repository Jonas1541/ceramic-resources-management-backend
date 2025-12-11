package com.jonasdurau.ceramicmanagement.product.line.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductLineRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name
) {}
