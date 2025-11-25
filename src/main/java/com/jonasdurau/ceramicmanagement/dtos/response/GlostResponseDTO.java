package com.jonasdurau.ceramicmanagement.dtos.response;

public record GlostResponseDTO(
    Long productId,
    Long productTxId,
    String unitName,
    String productName,
    String glazeColor,
    Double quantity
) {}
