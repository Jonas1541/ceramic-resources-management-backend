package com.jonasdurau.ceramicmanagement.resource.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;

public record ResourceTransactionResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    TransactionType type,
    double quantity,
    String resourceName,
    Long batchId,
    Long glazeTxId,
    BigDecimal cost
) {}
