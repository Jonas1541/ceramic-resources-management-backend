package com.jonasdurau.ceramicmanagement.batch.machineusage.dto;

public record BatchMachineUsageResponseDTO(
    Long machineId,
    String name,
    double usageTime,
    double energyConsumption
) {}
