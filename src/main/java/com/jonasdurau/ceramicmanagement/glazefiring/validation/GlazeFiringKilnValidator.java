package com.jonasdurau.ceramicmanagement.glazefiring.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiringRepository;
import com.jonasdurau.ceramicmanagement.kiln.validation.KilnDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class GlazeFiringKilnValidator implements KilnDeletionValidator {

    private final GlazeFiringRepository repository;

    public GlazeFiringKilnValidator(GlazeFiringRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if(repository.existsByKilnId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o forno pois ele possui queimas de glasura associadas.");
        }
    }
    
}
