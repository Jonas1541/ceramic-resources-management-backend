package com.jonasdurau.ceramicmanagement.glaze.transaction.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransactionRepository;
import com.jonasdurau.ceramicmanagement.glaze.validation.GlazeDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class GlazeTransactionValidator implements GlazeDeletionValidator {

    private final GlazeTransactionRepository repository;

    public GlazeTransactionValidator(GlazeTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long glazeId) {
        if(repository.existsByGlazeId(glazeId)) {
            throw new ResourceDeletionException("Não é possível deletar a glasura de id " + glazeId + "pois ela possui transações associadas.");
        }
    }
    
}
