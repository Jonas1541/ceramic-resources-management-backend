package com.jonasdurau.ceramicmanagement.employee;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategory;
import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategoryRepository;
import com.jonasdurau.ceramicmanagement.employee.category.dto.EmployeeCategoryResponseDTO;
import com.jonasdurau.ceramicmanagement.employee.dto.EmployeeRequestDTO;
import com.jonasdurau.ceramicmanagement.employee.dto.EmployeeResponseDTO;
import com.jonasdurau.ceramicmanagement.employee.validation.EmployeeDeletionValidator;
import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class EmployeeService
        implements IndependentCrudService<EmployeeResponseDTO, EmployeeRequestDTO, EmployeeResponseDTO, Long> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeCategoryRepository employeeCategoryRepository;
    private final GlazeService glazeService;
    private final List<EmployeeDeletionValidator> deletionValidators;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, EmployeeCategoryRepository employeeCategoryRepository,
            GlazeService glazeService, List<EmployeeDeletionValidator> deletionValidators) {
        this.employeeRepository = employeeRepository;
        this.employeeCategoryRepository = employeeCategoryRepository;
        this.glazeService = glazeService;
        this.deletionValidators = deletionValidators;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<EmployeeResponseDTO> findAll() {
        List<Employee> list = employeeRepository.findAll();
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public EmployeeResponseDTO findById(Long id) {
        Employee entity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public EmployeeResponseDTO create(EmployeeRequestDTO dto) {
        List<EmployeeCategory> categories = employeeCategoryRepository.findAllById(dto.categoryIds());
        if (categories.size() != dto.categoryIds().size()) {
            // Optional: identify which IDs are missing for better error message
            throw new ResourceNotFoundException("Uma ou mais categorias não encontradas.");
        }

        Employee entity = new Employee();
        entity.setName(dto.name());
        entity.setCategories(categories);
        entity.setCostPerHour(dto.costPerHour());
        entity = employeeRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO dto) {
        Employee entity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id:" + id));

        List<EmployeeCategory> categories = employeeCategoryRepository.findAllById(dto.categoryIds());
        if (categories.size() != dto.categoryIds().size()) {
            throw new ResourceNotFoundException("Uma ou mais categorias não encontradas.");
        }

        entity.setName(dto.name());
        entity.setCategories(categories);
        entity.setCostPerHour(dto.costPerHour());
        entity = employeeRepository.save(entity);
        glazeService.recalculateGlazesByEmployee(id);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        Employee entity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + id));
        deletionValidators.forEach(validator -> validator.validate(id));
        employeeRepository.delete(entity);
    }

    private EmployeeResponseDTO entityToResponseDTO(Employee entity) {
        List<EmployeeCategoryResponseDTO> categories = entity.getCategories().stream()
                .map(cat -> new EmployeeCategoryResponseDTO(
                        cat.getId(),
                        cat.getCreatedAt(),
                        cat.getUpdatedAt(),
                        cat.getName()))
                .toList();

        return new EmployeeResponseDTO(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getName(),
                categories,
                entity.getCostPerHour());
    }
}
