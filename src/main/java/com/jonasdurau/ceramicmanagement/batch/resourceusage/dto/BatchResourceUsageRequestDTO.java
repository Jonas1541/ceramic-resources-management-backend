package com.jonasdurau.ceramicmanagement.batch.resourceusage.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record BatchResourceUsageRequestDTO(
    @NotNull(message = "O ID do recurso não pode ser nulo.")
    @Positive(message = "O ID do recurso deve ser positivo.")
    Long resourceId,

    @PositiveOrZero(message = "A quantidade inicial não pode ser negativa.")
    double initialQuantity,

    @DecimalMin(value = "0.0", message = "A umidade não pode ser negativa.")
    @DecimalMax(value = "1.0", message = "A umidade não pode exceder 1.0.")
    double umidity,
    
    @PositiveOrZero(message = "A quantidade adicionada não pode ser negativa.")
    double addedQuantity
) {}
