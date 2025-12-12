package com.jonasdurau.ceramicmanagement.batch.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.batch.machineusage.BatchMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.machine.validation.MachineDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class BatchMachineValidator implements MachineDeletionValidator {

    private final BatchMachineUsageRepository repository;

    public BatchMachineValidator(BatchMachineUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long machineId) {
        if(repository.existsByMachineId(machineId)) {
            throw new ResourceDeletionException("Não é possível deletar a máquina pois ela possui bateladas associadas.");
        }
    }
    
}
