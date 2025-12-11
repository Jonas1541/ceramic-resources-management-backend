package com.jonasdurau.ceramicmanagement.bisquefiring;

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

import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BiscuitResponseDTO;
import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BisqueFiringRequestDTO;
import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BisqueFiringResponseDTO;
import com.jonasdurau.ceramicmanagement.bisquefiring.employeeusage.BisqueFiringEmployeeUsage;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
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
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentCrudService;

@Service
public class BisqueFiringService implements DependentCrudService<FiringListDTO, BisqueFiringRequestDTO, BisqueFiringResponseDTO, Long> {

    private final BisqueFiringRepository firingRepository;
    private final KilnRepository kilnRepository;
    private final ProductTransactionRepository productTransactionRepository;
    private final ResourceRepository resourceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public BisqueFiringService(BisqueFiringRepository firingRepository, KilnRepository kilnRepository,
            ProductTransactionRepository productTransactionRepository, ResourceRepository resourceRepository,
            EmployeeRepository employeeRepository) {
        this.firingRepository = firingRepository;
        this.kilnRepository = kilnRepository;
        this.productTransactionRepository = productTransactionRepository;
        this.resourceRepository = resourceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<FiringListDTO> findAllByParentId(Long kilnId) {
        if (!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId);
        }
        List<BisqueFiring> list = firingRepository.findByKilnId(kilnId);
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
            ))
            .toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public BisqueFiringResponseDTO findById(Long kilnId, Long firingId) {
        if(!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId);
        }
        BisqueFiring entity = firingRepository.findByIdAndKilnId(firingId, kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Queima não encontrada. Id: " + firingId));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public BisqueFiringResponseDTO create(Long kilnId, BisqueFiringRequestDTO dto) {
        BisqueFiring entity = new BisqueFiring();
        entity.setTemperature(dto.temperature());
        entity.setBurnTime(dto.burnTime());
        entity.setCoolingTime(dto.coolingTime());
        Kiln kiln = kilnRepository.findById(kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId));
        entity.setKiln(kiln);
        
        // Salva a entidade primeiro para ter um ID para associar
        entity = firingRepository.save(entity);

        // Adiciona funcionários
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            Employee employee = employeeRepository.findById(euDTO.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + euDTO.employeeId()));
            BisqueFiringEmployeeUsage eu = new BisqueFiringEmployeeUsage();
            eu.setBisqueFiring(entity);
            eu.setEmployee(employee);
            eu.setUsageTime(euDTO.usageTime());
            entity.getEmployeeUsages().add(eu);
        }

        // Adiciona produtos
        for(long biscuitId : dto.biscuits()) {
            ProductTransaction biscuit = productTransactionRepository.findById(biscuitId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + biscuitId));
            if(biscuit.getBisqueFiring() != null  && !biscuit.getBisqueFiring().getId().equals(entity.getId())) {
                throw new BusinessException("Produto já passou por uma 1° queima. Id: " + biscuitId);
            }
            biscuit.setBisqueFiring(entity);
            biscuit.setState(ProductState.BISCUIT);
            entity.getBiscuits().add(biscuit);
        }

        entity.setCostAtTime(calculateCostAtTime(entity));
        entity = firingRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public BisqueFiringResponseDTO update(Long kilnId, Long firingId, BisqueFiringRequestDTO dto) {
        if (!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. Id: " + kilnId);
        }
        BisqueFiring entity = firingRepository.findByIdAndKilnId(firingId, kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Queima não encontrada. Id: " + firingId));
        
        entity.setTemperature(dto.temperature());
        entity.setBurnTime(dto.burnTime());
        entity.setCoolingTime(dto.coolingTime());

        // Atualiza funcionários
        Map<Long, BisqueFiringEmployeeUsage> existingEmployeeUsages = entity.getEmployeeUsages().stream()
            .collect(Collectors.toMap(eu -> eu.getEmployee().getId(), eu -> eu));
        Set<Long> updatedEmployeeIds = new HashSet<>();
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            BisqueFiringEmployeeUsage existingEu = existingEmployeeUsages.get(euDTO.employeeId());
            if (existingEu != null) {
                existingEu.setUsageTime(euDTO.usageTime());
                updatedEmployeeIds.add(euDTO.employeeId());
            } else {
                Employee employee = employeeRepository.findById(euDTO.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + euDTO.employeeId()));
                BisqueFiringEmployeeUsage newEu = new BisqueFiringEmployeeUsage();
                newEu.setBisqueFiring(entity);
                newEu.setEmployee(employee);
                newEu.setUsageTime(euDTO.usageTime());
                entity.getEmployeeUsages().add(newEu);
                updatedEmployeeIds.add(euDTO.employeeId());
            }
        }
        List<BisqueFiringEmployeeUsage> euToRemove = entity.getEmployeeUsages().stream()
            .filter(eu -> !updatedEmployeeIds.contains(eu.getEmployee().getId()))
            .collect(Collectors.toList());
        entity.getEmployeeUsages().removeAll(euToRemove);

        // Atualiza produtos
        List<ProductTransaction> oldList = new ArrayList<>(entity.getBiscuits());
        List<ProductTransaction> newList = dto.biscuits().stream()
                .map(id -> productTransactionRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + id)))
                .collect(Collectors.toList());

        Set<Long> oldIds = oldList.stream().map(ProductTransaction::getId).collect(Collectors.toSet());
        Set<Long> newIds = newList.stream().map(ProductTransaction::getId).collect(Collectors.toSet());
        List<ProductTransaction> toRemove = oldList.stream().filter(biscuit -> !newIds.contains(biscuit.getId())).collect(Collectors.toList());
        List<ProductTransaction> toAdd = newList.stream().filter(biscuit -> !oldIds.contains(biscuit.getId())).collect(Collectors.toList());
        
        toRemove.forEach(biscuit -> {
            if(biscuit.getState() == ProductState.GLAZED) {
                throw new ResourceDeletionException("A queima não pode ser apagada pois há um produto que já passou pela 2° queima. Id: " + biscuit.getId());
            }
            biscuit.setBisqueFiring(null);
            biscuit.setState(ProductState.GREENWARE);
            productTransactionRepository.save(biscuit);
        });
        entity.getBiscuits().removeAll(toRemove);
        
        toAdd.forEach(biscuit -> {
            if(biscuit.getBisqueFiring() != null && !biscuit.getBisqueFiring().getId().equals(entity.getId())) {
                throw new BusinessException("Produto já passou por uma 1° queima. Id: " + biscuit.getId());
            }
            biscuit.setBisqueFiring(entity);
            biscuit.setState(ProductState.BISCUIT);
            productTransactionRepository.save(biscuit);
        });
        entity.getBiscuits().addAll(toAdd);
        
        entity.setCostAtTime(calculateCostAtTime(entity));
        BisqueFiring updatedEntity = firingRepository.save(entity);
        return entityToResponseDTO(updatedEntity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long kilnId, Long firingId) {
        if(!kilnRepository.existsById(kilnId)) {
            throw new ResourceNotFoundException("Forno não encontrado. id: " + kilnId);
        }
        BisqueFiring entity = firingRepository.findByIdAndKilnId(firingId, kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Queima não encontrada. Id: " + firingId));
        entity.getBiscuits().forEach(biscuit -> {
            if (biscuit.getState() == ProductState.GLAZED) {
                throw new ResourceDeletionException("A queima não pode ser apagada pois há um produto que já passou pela 2° queima. Id: "+ biscuit.getId());
            }
            biscuit.setBisqueFiring(null);
            biscuit.setState(ProductState.GREENWARE);
            productTransactionRepository.save(biscuit);
        });
        firingRepository.delete(entity);
    }

    private BisqueFiringResponseDTO entityToResponseDTO(BisqueFiring entity) {
        // Mapeia Biscoitos
        List<BiscuitResponseDTO> biscuitDTOs = entity.getBiscuits().stream().map(biscuit -> {
            String productName = biscuit.getProduct().getName();
            return new BiscuitResponseDTO(biscuit.getId(), biscuit.getUnitName(), productName);
        }).collect(Collectors.toList());

        List<EmployeeUsageResponseDTO> employeeUsageDTOs = entity.getEmployeeUsages().stream()
            .map(eu -> new EmployeeUsageResponseDTO(
                eu.getEmployee().getId(),
                eu.getEmployee().getName(),
                eu.getUsageTime(),
                eu.getCost()
            ))
            .collect(Collectors.toList());

        return new BisqueFiringResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getTemperature(),
            entity.getBurnTime(),
            entity.getCoolingTime(),
            entity.getGasConsumption(),
            entity.getKiln().getName(),
            biscuitDTOs,
            employeeUsageDTOs,
            entity.calculateEmployeeTotalCost(),
            entity.getCostAtTime()
        );
    }

    private BigDecimal calculateCostAtTime(BisqueFiring entity) {
        Resource electricity = resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso ELECTRICITY não cadastrada!"));
        Resource gas = resourceRepository.findByCategory(ResourceCategory.GAS)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso GAS não cadastrada!"));
        
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