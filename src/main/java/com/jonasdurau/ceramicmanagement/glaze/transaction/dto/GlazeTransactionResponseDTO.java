package com.jonasdurau.ceramicmanagement.glaze.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;

public record GlazeTransactionResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    double quantity,
    TransactionType type,
    String glazeColor,
    Long productTxId,
    BigDecimal resourceTotalCostAtTime,
    BigDecimal machineEnergyConsumptionCostAtTime,
    BigDecimal employeeTotalCostAtTime,
    BigDecimal glazeFinalCostAtTime
) {}
