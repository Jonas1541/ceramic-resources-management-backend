package com.jonasdurau.ceramicmanagement.resource.transaction.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransactionRepository;
import com.jonasdurau.ceramicmanagement.resource.validation.ResourceDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class ResourceTransactionValidator implements ResourceDeletionValidator {

    private final ResourceTransactionRepository repository;

    public ResourceTransactionValidator(ResourceTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long resourceId) {
        if(repository.existsByResourceId(resourceId)) {
            throw new ResourceDeletionException("Não é possível deletar o recurso pois ele possui transações associadas.");
        }
    }
    
}
