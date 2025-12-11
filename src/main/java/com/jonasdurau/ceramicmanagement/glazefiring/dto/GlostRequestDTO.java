package com.jonasdurau.ceramicmanagement.glazefiring.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GlostRequestDTO(
    @NotNull(message = "O id do produto deve ser informado")
    @Positive(message = "O id da transação do produto deve ser positivo")
    Long productTransactionId,
    @Positive(message = "O id da glasura deve ser positivo")
    Long glazeId
) {}
