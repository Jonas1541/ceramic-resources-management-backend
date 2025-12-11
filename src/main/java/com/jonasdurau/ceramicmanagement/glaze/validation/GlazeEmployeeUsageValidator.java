package com.jonasdurau.ceramicmanagement.glaze.validation;

import org.springframework.stereotype.Component;
import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.glaze.employeeusage.GlazeEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class GlazeEmployeeUsageValidator implements EmployeeDeletionValidator {

    private final GlazeEmployeeUsageRepository repository;

    public GlazeEmployeeUsageValidator(GlazeEmployeeUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if (repository.existsByEmployeeId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o funcionário de id " + employeeId + " pois ele possui glasuras associadas.");
        }
    }
}