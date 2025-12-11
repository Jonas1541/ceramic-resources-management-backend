package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.validation;

import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.employeeusage.DryingSessionEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

public class DryingSessionEmployeeUsageValidator implements EmployeeDeletionValidator {

    private final DryingSessionEmployeeUsageRepository repository;

    public DryingSessionEmployeeUsageValidator(DryingSessionEmployeeUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if(repository.existsByEmployeeId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o funcionário de id " + employeeId + " pois ele possui usos de estufa associados.");
        }
    }
    
}
