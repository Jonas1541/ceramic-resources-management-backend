package com.jonasdurau.ceramicmanagement.bisquefiring.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiringRepository;
import com.jonasdurau.ceramicmanagement.kiln.validation.KilnDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class BisqueFiringKilnValidator implements KilnDeletionValidator {

    private final BisqueFiringRepository repository;

    public BisqueFiringKilnValidator(BisqueFiringRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long kilnId) {
        if(repository.existsByKilnId(kilnId)) {
            throw new ResourceDeletionException("Não é possível deletar o forno pois ele possui queimas de biscoito vinculadas.");
        }
    }
    
}
