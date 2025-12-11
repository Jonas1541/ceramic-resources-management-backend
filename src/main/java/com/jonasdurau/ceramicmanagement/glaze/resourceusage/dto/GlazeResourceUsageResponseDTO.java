package com.jonasdurau.ceramicmanagement.glaze.resourceusage.dto;

public record GlazeResourceUsageResponseDTO(
    Long resourceId,
    String resourceName,
    double quantity
) {}
