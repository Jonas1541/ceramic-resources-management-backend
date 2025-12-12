package com.jonasdurau.ceramicmanagement.glaze.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.machine.validation.MachineDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class GlazeMachineValidator implements MachineDeletionValidator {

    private final GlazeMachineUsageRepository repository;

    public GlazeMachineValidator(GlazeMachineUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long machineId) {
        if(repository.existsByMachineId(machineId)) {
            throw new ResourceDeletionException("Não é possível deletar a máquina pois ela possui glasuras associadas.");
        }
    }
    
}
