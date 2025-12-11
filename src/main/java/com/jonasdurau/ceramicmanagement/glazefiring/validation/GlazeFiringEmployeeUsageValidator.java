package com.jonasdurau.ceramicmanagement.glazefiring.validation;

import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.glazefiring.employeeusage.GlazeFiringEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

public class GlazeFiringEmployeeUsageValidator implements EmployeeDeletionValidator {

    private final GlazeFiringEmployeeUsageRepository repository;

    public GlazeFiringEmployeeUsageValidator(GlazeFiringEmployeeUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if(repository.existsByEmployeeId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o funcionário de id " + employeeId + " pois ele possui queimas de glasura associadas.");
        }
    }
    
}
