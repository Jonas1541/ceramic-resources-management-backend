package com.jonasdurau.ceramicmanagement.product.transaction.validation;

import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.product.transaction.employeeusage.ProductTransactionEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

public class ProductTransactionEmployeeUsageValidator implements EmployeeDeletionValidator {

    private final ProductTransactionEmployeeUsageRepository repository;

    public ProductTransactionEmployeeUsageValidator(ProductTransactionEmployeeUsageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeId) {
        if(repository.existsByEmployeeId(employeeId)) {
            throw new ResourceDeletionException("Não é possível deletar o funcionário de id " + employeeId + " pois ele possui transações de produto associadas.");
        }
    }
    
}
