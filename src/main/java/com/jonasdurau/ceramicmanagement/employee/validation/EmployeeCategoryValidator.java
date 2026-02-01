package com.jonasdurau.ceramicmanagement.employee.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.employee.category.validation.EmployeeCategoryDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class EmployeeCategoryValidator implements EmployeeCategoryDeletionValidator {

    private final EmployeeRepository repository;

    public EmployeeCategoryValidator(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long employeeCategoryId) {
        if (repository.existsByCategoriesId(employeeCategoryId)) {
            throw new ResourceDeletionException(
                    "Não é possível deletar a categoria de funcionário pois ela possui funcionários associados.");
        }
    }

}
