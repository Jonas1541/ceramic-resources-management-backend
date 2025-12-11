package com.jonasdurau.ceramicmanagement.kiln.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;

public record KilnResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    double power,
    double gasConsumptionPerHour,
    List<MachineResponseDTO> machines
) {
    public KilnResponseDTO {
        if (machines == null) {
            machines = new ArrayList<>();
        }
    }
}