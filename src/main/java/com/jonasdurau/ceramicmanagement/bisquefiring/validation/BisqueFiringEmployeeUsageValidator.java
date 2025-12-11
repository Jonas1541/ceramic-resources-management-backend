package com.jonasdurau.ceramicmanagement.bisquefiring.validation;

import com.jonasdurau.ceramicmanagement.bisquefiring.employeeusage.BisqueFiringEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

public class BisqueFiringEmployeeUsageValidator implements EmployeeDeletionValidator {

    private final BisqueFiringEmployeeUsageRepository repository;

    public BisqueFiringEmployeeUsageValidator(BisqueFiringEmployeeUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if(repository.existsByEmployeeId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o funcionário de id " + employeeId + " pois ele possui queimas de biscoito associadas.");
        }
    }
    
}
