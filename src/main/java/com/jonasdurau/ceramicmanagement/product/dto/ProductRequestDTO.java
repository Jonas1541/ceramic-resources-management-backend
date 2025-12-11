package com.jonasdurau.ceramicmanagement.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,
    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser positivo")
    BigDecimal price,
    @Positive(message = "A altura deve ser positiva")
    double height,
    @Positive(message = "O comprimento deve ser positivo")
    double length,
    @Positive(message = "A largura deve ser positiva")
    double width,
    @Positive(message = "A quantia de glasura por unidade deve ser positiva")
    double glazeQuantityPerUnit,
    @Positive(message = "O peso deve ser positivo")
    double weight,
    @Positive(message = "O tipo é obrigatório")
    long typeId,
    @Positive(message = "A linha é obrigatória")
    long lineId
) {}
