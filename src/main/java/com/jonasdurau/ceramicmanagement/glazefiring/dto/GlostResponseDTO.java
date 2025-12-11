package com.jonasdurau.ceramicmanagement.glazefiring.dto;

public record GlostResponseDTO(
    Long productId,
    Long productTxId,
    String unitName,
    String productName,
    String glazeColor,
    Double quantity
) {}
