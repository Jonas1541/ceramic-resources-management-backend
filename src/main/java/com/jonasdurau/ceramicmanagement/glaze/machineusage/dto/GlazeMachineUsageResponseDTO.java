package com.jonasdurau.ceramicmanagement.glaze.machineusage.dto;

public record GlazeMachineUsageResponseDTO(
    long machineId,
    String machineName,
    double usageTime
) {}
