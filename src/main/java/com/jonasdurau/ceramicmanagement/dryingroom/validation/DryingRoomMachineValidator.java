package com.jonasdurau.ceramicmanagement.dryingroom.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoomRepository;
import com.jonasdurau.ceramicmanagement.machine.validation.MachineDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class DryingRoomMachineValidator implements MachineDeletionValidator {

    private final DryingRoomRepository repository;

    public DryingRoomMachineValidator(DryingRoomRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long machineId) {
        if(repository.existsByMachinesId(machineId)) {
            throw new ResourceDeletionException("Não é possível deletar a máquina pois ela possui estufas associadas.");
        }
    }
    
}
