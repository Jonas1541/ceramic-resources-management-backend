package com.jonasdurau.ceramicmanagement.product.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductOutgoingReason;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;

public record ProductTransactionResponseDTO(
    Long id,
    String unitName,
    Instant createdAt,
    Instant updatedAt,
    Instant outgoingAt,
    ProductState state,
    ProductOutgoingReason outgoingReason,
    String productName,
    Long bisqueFiringId,
    Long glazeFiringId,
    String glazeColor,
    double glazeQuantity,
    List<EmployeeUsageResponseDTO> employeeUsages,
    BigDecimal employeeTotalCost,
    BigDecimal batchCost,
    BigDecimal bisqueFiringCost,
    BigDecimal glazeFiringCost,
    BigDecimal glazeTransactionCost,
    BigDecimal totalCost,
    BigDecimal profit
) {}
