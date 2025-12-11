package com.jonasdurau.ceramicmanagement.resource.transaction.dto;

import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResourceTransactionRequestDTO(
    @NotNull(message = "O tipo de transação é obrigatório")
    TransactionType type,
    @Positive(message = "A quantidade deve ser positiva")
    double quantity
) {}
