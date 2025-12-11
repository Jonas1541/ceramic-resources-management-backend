package com.jonasdurau.ceramicmanagement.kiln.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record KilnRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,
    @Positive(message = "O consumo de gás por hora deve ser positivo")
    double gasConsumptionPerHour,
    List<@Positive(message = "Os id das máquinas devem ser positivos") Long> machines
) {
    public KilnRequestDTO {
        if (machines == null) {
            machines = new ArrayList<>();
        }
    }
}
