package com.jonasdurau.ceramicmanagement.dryingroom.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;

public record DryingRoomResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    double gasConsumptionPerHour,
    List<MachineResponseDTO> machines
) {
    public DryingRoomResponseDTO {
        if (machines == null) {
            machines = new ArrayList<>();
        }
    }
}
