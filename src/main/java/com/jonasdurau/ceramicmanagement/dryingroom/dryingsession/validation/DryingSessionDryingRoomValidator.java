package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.validation;

import org.springframework.stereotype.Component;
import com.jonasdurau.ceramicmanagement.dryingroom.validation.DryingRoomDeletionValidator;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSessionRepository;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class DryingSessionDryingRoomValidator implements DryingRoomDeletionValidator {

    private final DryingSessionRepository repository;

    public DryingSessionDryingRoomValidator(DryingSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long dryingRoomId) {
        if (repository.existsByDryingRoomId(dryingRoomId)) {
            throw new ResourceDeletionException("A estufa não pode ser deletada pois ela possui usos registrados.");
        }
    }
}