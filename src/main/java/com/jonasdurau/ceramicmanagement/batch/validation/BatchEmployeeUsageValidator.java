package com.jonasdurau.ceramicmanagement.batch.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.batch.employeeusage.BatchEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class BatchEmployeeUsageValidator implements EmployeeDeletionValidator {

    private final BatchEmployeeUsageRepository repository;

    public BatchEmployeeUsageValidator(BatchEmployeeUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if (repository.existsByEmployeeId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o funcionário de id " + employeeId + " pois ele possui bateladas associadas.");
        }
    }
}
