package com.jonasdurau.ceramicmanagement.glazefiring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransaction;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransactionService;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlazeFiringRequestDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlazeFiringResponseDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlostRequestDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlostResponseDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.employeeusage.GlazeFiringEmployeeUsage;
import com.jonasdurau.ceramicmanagement.kiln.Kiln;
import com.jonasdurau.ceramicmanagement.kiln.KilnRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransactionRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.FiringListDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentCrudService;

@Service
public class GlazeFiringService implements DependentCrudService<FiringListDTO, GlazeFiringRequestDTO, GlazeFiringResponseDTO, Long> {
    
    @Autowired
    private GlazeFiringRepository firingRepository;

    @Autowired
    private KilnRepository kilnRepository;

    @Autowired
    private ProductTransactionRepository productTransactionRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private GlazeTransactionService glazeTransactionService;

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<FiringListDTO> findAllByParentId(Long kilnId) {
        if(!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId);
        }
        List<GlazeFiring> list = firingRepository.findByKilnId(kilnId);
        return list.stream()
            .map(firing -> new FiringListDTO(
                firing.getId(),
                firing.getCreatedAt(),
                firing.getUpdatedAt(),
                firing.getTemperature(),
                firing.getBurnTime(),
                firing.getCoolingTime(),
                firing.getGasConsumption(),
                firing.getKiln().getName(),
                firing.getCostAtTime()
            )).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public GlazeFiringResponseDTO findById(Long kilnId, Long firingId) {
        if(!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId);
        }
        GlazeFiring entity = firingRepository.findByIdAndKilnId(firingId, kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Queima não encontrada. Id: " + firingId));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeFiringResponseDTO create(Long kilnId, GlazeFiringRequestDTO dto) {
        GlazeFiring entity = new GlazeFiring();
        entity.setTemperature(dto.temperature());
        entity.setBurnTime(dto.burnTime());
        entity.setCoolingTime(dto.coolingTime());
        Kiln kiln = kilnRepository.findById(kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId));
        entity.setKiln(kiln);
        
        // Salva a entidade para gerar o ID antes de associar
        entity = firingRepository.save(entity);

        // Adiciona funcionários
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            Employee employee = employeeRepository.findById(euDTO.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + euDTO.employeeId()));
            GlazeFiringEmployeeUsage eu = new GlazeFiringEmployeeUsage();
            eu.setGlazeFiring(entity);
            eu.setEmployee(employee);
            eu.setUsageTime(euDTO.usageTime());
            entity.getEmployeeUsages().add(eu);
        }

        // Adiciona produtos
        for(GlostRequestDTO glostDTO : dto.glosts()) {
            ProductTransaction glost = productTransactionRepository.findById(glostDTO.productTransactionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + glostDTO.productTransactionId()));
            if(glost.getGlazeFiring() != null  && !glost.getGlazeFiring().getId().equals(entity.getId())) {
                throw new ResourceNotFoundException("Produto já passou por uma 2° queima. Id: " + glost.getId());
            }
            glost.setGlazeFiring(entity);
            glost.setState(ProductState.GLAZED);
            if (glostDTO.glazeId() != null) {
                GlazeTransaction glazeTransaction = glazeTransactionService.createEntity(glostDTO.glazeId(), glost);
                glost.setGlazeTransaction(glazeTransaction);
            }
            entity.getGlosts().add(glost);
        }

        entity.setCostAtTime(calculateCostAtTime(entity));
        entity = firingRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeFiringResponseDTO update(Long kilnId, Long firingId, GlazeFiringRequestDTO dto) {
        if (!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId);
        }
        GlazeFiring entity = firingRepository.findByIdAndKilnId(firingId, kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Queima não encontrada. Id: " + firingId));
        
        entity.setTemperature(dto.temperature());
        entity.setBurnTime(dto.burnTime());
        entity.setCoolingTime(dto.coolingTime());

        // Atualiza funcionários
        Map<Long, GlazeFiringEmployeeUsage> existingEmployeeUsages = entity.getEmployeeUsages().stream()
            .collect(Collectors.toMap(eu -> eu.getEmployee().getId(), eu -> eu));
        Set<Long> updatedEmployeeIds = new HashSet<>();
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            GlazeFiringEmployeeUsage existingEu = existingEmployeeUsages.get(euDTO.employeeId());
            if (existingEu != null) {
                existingEu.setUsageTime(euDTO.usageTime());
                updatedEmployeeIds.add(euDTO.employeeId());
            } else {
                Employee employee = employeeRepository.findById(euDTO.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + euDTO.employeeId()));
                GlazeFiringEmployeeUsage newEu = new GlazeFiringEmployeeUsage();
                newEu.setGlazeFiring(entity);
                newEu.setEmployee(employee);
                newEu.setUsageTime(euDTO.usageTime());
                entity.getEmployeeUsages().add(newEu);
                updatedEmployeeIds.add(euDTO.employeeId());
            }
        }
        List<GlazeFiringEmployeeUsage> euToRemove = entity.getEmployeeUsages().stream()
            .filter(eu -> !updatedEmployeeIds.contains(eu.getEmployee().getId()))
            .collect(Collectors.toList());
        entity.getEmployeeUsages().removeAll(euToRemove);

        // Atualiza produtos
        List<ProductTransaction> oldList = new ArrayList<>(entity.getGlosts());
        List<ProductTransaction> newList = dto.glosts().stream()
                .map(glostDTO -> {
                    ProductTransaction glost = productTransactionRepository.findById(glostDTO.productTransactionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + glostDTO.productTransactionId()));
                    if (glost.getGlazeFiring() != null && !glost.getGlazeFiring().getId().equals(entity.getId())) {
                        throw new ResourceNotFoundException("Produto já passou por uma 2° queima. Id: " + glost.getId());
                    }
                    if (glostDTO.glazeId() != null) {
                        GlazeTransaction glazeTransaction = glazeTransactionService.createEntity(glostDTO.glazeId(), glost);
                        glost.setGlazeTransaction(glazeTransaction);
                    } else {
                        glost.setGlazeTransaction(null);
                    }
                    glost.setGlazeFiring(entity);
                    glost.setState(ProductState.GLAZED);
                    return glost;
                }).collect(Collectors.toList());

        Set<Long> oldIds = oldList.stream().map(ProductTransaction::getId).collect(Collectors.toSet());
        Set<Long> newIds = newList.stream().map(ProductTransaction::getId).collect(Collectors.toSet());
        List<ProductTransaction> toRemove = oldList.stream().filter(glost -> !newIds.contains(glost.getId())).collect(Collectors.toList());
        toRemove.forEach(glost -> {
            glost.setGlazeFiring(null);
            glost.setState(ProductState.BISCUIT);
            glost.setGlazeTransaction(null);
            productTransactionRepository.save(glost);
        });
        entity.getGlosts().removeAll(toRemove);

        List<ProductTransaction> toAdd = newList.stream().filter(glost -> !oldIds.contains(glost.getId())).collect(Collectors.toList());
        toAdd.forEach(glost -> productTransactionRepository.save(glost));
        entity.getGlosts().addAll(toAdd);
        
        entity.setCostAtTime(calculateCostAtTime(entity));
        GlazeFiring updatedEntity = firingRepository.save(entity);
        return entityToResponseDTO(updatedEntity);
    }
    
    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long kilnId, Long firingId) {
        if(!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. id: " + kilnId);
        }
        GlazeFiring entity = firingRepository.findByIdAndKilnId(firingId, kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Queima não encontrada. Id: " + firingId));
        entity.getGlosts().forEach(glost -> {
            glost.setGlazeFiring(null);
            glost.setState(ProductState.BISCUIT);
            glost.setGlazeTransaction(null);
            productTransactionRepository.save(glost);
        });
        firingRepository.delete(entity);
    }

    private GlazeFiringResponseDTO entityToResponseDTO(GlazeFiring entity) {
        List<GlostResponseDTO> glostDTOs = entity.getGlosts().stream().map(glost -> {
            Long productId = glost.getProduct().getId();
            Long productTxId = glost.getId();
            String unitName = glost.getUnitName();
            String productName = glost.getProduct().getName();
            String glazeColor = "sem glasura";
            Double quantity = 0.0;
            if (glost.getGlazeTransaction() != null) {
                glazeColor = glost.getGlazeTransaction().getGlaze().getColor();
                quantity = glost.getGlazeTransaction().getQuantity();
            }
            return new GlostResponseDTO(productId, productTxId, unitName, productName, glazeColor, quantity);
        }).collect(Collectors.toList());

        List<EmployeeUsageResponseDTO> employeeUsageDTOs = entity.getEmployeeUsages().stream()
            .map(eu -> new EmployeeUsageResponseDTO(
                eu.getEmployee().getId(),
                eu.getEmployee().getName(),
                eu.getUsageTime(),
                eu.getCost()
            ))
            .collect(Collectors.toList());

        return new GlazeFiringResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getTemperature(),
            entity.getBurnTime(),
            entity.getCoolingTime(),
            entity.getGasConsumption(),
            entity.getKiln().getName(),
            glostDTOs,
            employeeUsageDTOs,
            entity.calculateEmployeeTotalCost(),
            entity.getCostAtTime()
        );
    }

    private BigDecimal calculateCostAtTime(GlazeFiring entity) {
        Resource electricity = resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso ELECTRICITY não cadastrada!"));
        Resource gas = resourceRepository.findByCategory(ResourceCategory.GAS)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso GAS não cadastrado!"));
        
        BigDecimal gasCost = gas.getUnitValue()
                .multiply(BigDecimal.valueOf(entity.getGasConsumption()));

        BigDecimal electricCost = electricity.getUnitValue()
                .multiply(BigDecimal.valueOf(entity.getEnergyConsumption()));
        
        BigDecimal employeeCost = entity.getEmployeeUsages().stream()
            .map(usage -> usage.getEmployee().getCostPerHour()
                .multiply(BigDecimal.valueOf(usage.getUsageTime())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return gasCost.add(electricCost).add(employeeCost)
                .setScale(2, RoundingMode.HALF_UP);
    }
}