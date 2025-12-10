package com.jonasdurau.ceramicmanagement.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.controllers.exceptions.BusinessException;
import com.jonasdurau.ceramicmanagement.controllers.exceptions.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.controllers.exceptions.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.dtos.request.EmployeeCategoryRequestDTO;
import com.jonasdurau.ceramicmanagement.dtos.response.EmployeeCategoryResponseDTO;
import com.jonasdurau.ceramicmanagement.entities.EmployeeCategory;
import com.jonasdurau.ceramicmanagement.repositories.EmployeeCategoryRepository;
import com.jonasdurau.ceramicmanagement.repositories.EmployeeRepository;

@Service
public class EmployeeCategoryService implements IndependentCrudService<EmployeeCategoryResponseDTO, EmployeeCategoryRequestDTO, EmployeeCategoryResponseDTO, Long>{

    private final EmployeeCategoryRepository employeeCategoryRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeCategoryService(EmployeeCategoryRepository employeeCategoryRepository, EmployeeRepository employeeRepository) {
        this.employeeCategoryRepository = employeeCategoryRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<EmployeeCategoryResponseDTO> findAll() {
        List<EmployeeCategory> list = employeeCategoryRepository.findAll();
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public EmployeeCategoryResponseDTO findById(Long id) {
        EmployeeCategory entity = employeeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria de funcionário não encontrada. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public EmployeeCategoryResponseDTO create(EmployeeCategoryRequestDTO dto) {
        if(employeeCategoryRepository.existsByName(dto.name())) {
            throw new BusinessException("Já existe uma categoria de funcionário com o nome: " + dto.name());
        }
        EmployeeCategory entity = new EmployeeCategory();
        entity.setName(dto.name());
        entity = employeeCategoryRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public EmployeeCategoryResponseDTO update(Long id, EmployeeCategoryRequestDTO dto) {
        EmployeeCategory entity = employeeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria de funcionário não encontrada. Id: " + id));
        String oldName = entity.getName();
        String newName = dto.name();
        if(!newName.equals(oldName) && employeeCategoryRepository.existsByName(newName)) {
            throw new BusinessException("O nome " + newName + " já existe.");
        }
        entity.setName(newName);
        entity = employeeCategoryRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        EmployeeCategory entity = employeeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria de funcionário não encontrada. Id: " + id));
        boolean hasEmployees = employeeRepository.existsByCategory(entity);
        if(hasEmployees) {
            throw new ResourceDeletionException("Não é possível deletar a categoria de funcionário pois ela possui funcionários associados.");
        }
        employeeCategoryRepository.delete(entity);
    }
    
    private EmployeeCategoryResponseDTO entityToResponseDTO(EmployeeCategory entity) {
        return new EmployeeCategoryResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName()
        );
    }
}
