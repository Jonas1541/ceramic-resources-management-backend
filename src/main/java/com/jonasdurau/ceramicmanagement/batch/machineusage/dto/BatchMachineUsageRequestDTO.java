package com.jonasdurau.ceramicmanagement.batch.machineusage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BatchMachineUsageRequestDTO(
    @NotNull(message = "O ID da máquina não pode ser nulo.")
    @Positive(message = "O ID da máquina deve ser positivo.")
    Long machineId,

    @Positive(message = "O tempo de uso deve ser maior que 0.")
    double usageTime
) {}
