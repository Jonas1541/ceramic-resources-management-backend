package com.jonasdurau.ceramicmanagement.kiln.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.kiln.KilnRepository;
import com.jonasdurau.ceramicmanagement.machine.validation.MachineDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class KilnMachineValidator implements MachineDeletionValidator {

    private final KilnRepository repository;

    public KilnMachineValidator(KilnRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long machineId) {
        if(repository.existsByMachinesId(machineId)) {
            throw new ResourceDeletionException("Não é possível deletar a máquina pois ela possui fornos associados.");
        }
    }
    
}
