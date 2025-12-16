package com.jonasdurau.ceramicmanagement.glaze.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.resource.validation.ResourceDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class GlazeResourceUsageValidator implements ResourceDeletionValidator {

    private final GlazeResourceUsageRepository repository;

    public GlazeResourceUsageValidator(GlazeResourceUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long resourceId) {
        if(repository.existsByResourceId(resourceId)) {
            throw new ResourceDeletionException("Não é possível deletar o recurso pois ele possui glasuras associadas.");
        }
    }
    
}
