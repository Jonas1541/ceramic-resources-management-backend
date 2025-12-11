package com.jonasdurau.ceramicmanagement.product.type.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductTypeRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name
) {}
