package com.jonasdurau.ceramicmanagement.batch.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.batch.resourceusage.BatchResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.resource.validation.ResourceDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class BatchResourceUsageValidator implements ResourceDeletionValidator {

    private final BatchResourceUsageRepository repository;

    public BatchResourceUsageValidator(BatchResourceUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long resourceId) {
        if(repository.existsByResourceId(resourceId)) {
            throw new ResourceDeletionException("Não é possível deletar o recurso pois ele possui bateladas associadas.");
        }
    }
    
}
