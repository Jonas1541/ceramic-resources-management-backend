package com.jonasdurau.ceramicmanagement.glaze.machineusage.dto;

import jakarta.validation.constraints.Positive;

public record GlazeMachineUsageRequestDTO(
    @Positive(message = "O ID da máquina deve ser positivo.")
    long machineId,
    @Positive(message = "O tempo de uso deve ser maior que 0.")
    double usageTime
) {}
