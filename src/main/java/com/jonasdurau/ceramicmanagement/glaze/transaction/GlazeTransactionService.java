package com.jonasdurau.ceramicmanagement.glaze.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.glaze.GlazeRepository;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsage;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsage;
import com.jonasdurau.ceramicmanagement.glaze.transaction.dto.GlazeTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.transaction.dto.GlazeTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentCrudService;

@Service
public class GlazeTransactionService implements DependentCrudService<GlazeTransactionResponseDTO, GlazeTransactionRequestDTO, GlazeTransactionResponseDTO, Long> {

    @Autowired
    private GlazeTransactionRepository glazeTransactionRepository;

    @Autowired
    private GlazeRepository glazeRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<GlazeTransactionResponseDTO> findAllByParentId(Long glazeId) {
        Glaze glaze = glazeRepository.findById(glazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze não encontrado. Id: " + glazeId));
        return glaze.getTransactions().stream()
            .map(this::entityToResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public GlazeTransactionResponseDTO findById(Long glazeId, Long transactionId) {
        Glaze glaze = glazeRepository.findById(glazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze não encontrado. Id: " + glazeId));
        GlazeTransaction transaction = glaze.getTransactions().stream()
            .filter(t -> t.getId().equals(transactionId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada. Id: " + transactionId));
        return entityToResponseDTO(transaction);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeTransactionResponseDTO create(Long glazeId, GlazeTransactionRequestDTO dto) {
        Glaze glaze = glazeRepository.findById(glazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze não encontrado. Id: " + glazeId));
        GlazeTransaction entity = new GlazeTransaction();
        entity.setType(dto.type());
        entity.setQuantity(dto.quantity());
        entity.setGlaze(glaze);
        BigDecimal resourceCost = computeResourceCostAtTime(glaze, dto.quantity());
        BigDecimal machineCost = computeMachineCostAtTime(glaze, dto.quantity());
        BigDecimal employeeCost = computeEmployeeCostAtTime(glaze, dto.quantity());
        BigDecimal finalCost = resourceCost.add(machineCost).add(employeeCost).setScale(2, RoundingMode.HALF_UP);
        entity.setResourceTotalCostAtTime(resourceCost);
        entity.setMachineEnergyConsumptionCostAtTime(machineCost);
        entity.setEmployeeTotalCostAtTime(employeeCost);
        entity.setGlazeFinalCostAtTime(finalCost);
        if (dto.type() == TransactionType.INCOMING) {
            createResourceTransactionsForGlazeTx(entity, glaze, dto.quantity());
        }
        entity = glazeTransactionRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeTransaction createEntity(Long glazeId, ProductTransaction productTransaction) {
        Glaze glaze = glazeRepository.findById(glazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze não encontrado. Id: " + glazeId));
        GlazeTransaction entity = new GlazeTransaction();
        entity.setType(TransactionType.OUTGOING);
        entity.setQuantity(productTransaction.getProduct().getglazeQuantityPerUnit());
        entity.setGlaze(glaze);
        entity.setProductTransaction(productTransaction);
        BigDecimal resourceCost = computeResourceCostAtTime(glaze, productTransaction.getProduct().getglazeQuantityPerUnit());
        BigDecimal machineCost = computeMachineCostAtTime(glaze, productTransaction.getProduct().getglazeQuantityPerUnit());
        BigDecimal employeeCost = computeEmployeeCostAtTime(glaze, productTransaction.getProduct().getglazeQuantityPerUnit());
        BigDecimal finalCost = resourceCost.add(machineCost).add(employeeCost).setScale(2, RoundingMode.HALF_UP);
        entity.setResourceTotalCostAtTime(resourceCost);
        entity.setMachineEnergyConsumptionCostAtTime(machineCost);
        entity.setEmployeeTotalCostAtTime(employeeCost);
        entity.setGlazeFinalCostAtTime(finalCost);
        entity = glazeTransactionRepository.save(entity);
        return entity;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeTransactionResponseDTO update(Long glazeId, Long transactionId, GlazeTransactionRequestDTO dto) {
        Glaze glaze = glazeRepository.findById(glazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze não encontrado. Id: " + glazeId));
        GlazeTransaction transaction = glaze.getTransactions().stream()
            .filter(t -> t.getId().equals(transactionId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada. Id: " + transactionId));
        transaction.setType(dto.type());
        transaction.setQuantity(dto.quantity());
        BigDecimal resourceCost = computeResourceCostAtTime(glaze, dto.quantity());
        BigDecimal machineCost = computeMachineCostAtTime(glaze, dto.quantity());
        BigDecimal employeeCost = computeEmployeeCostAtTime(glaze, dto.quantity());
        BigDecimal finalCost = resourceCost.add(machineCost).add(employeeCost).setScale(2, RoundingMode.HALF_UP);
        transaction.setResourceTotalCostAtTime(resourceCost);
        transaction.setMachineEnergyConsumptionCostAtTime(machineCost);
        transaction.setEmployeeTotalCostAtTime(employeeCost);
        transaction.setGlazeFinalCostAtTime(finalCost);
        transaction.getResourceTransactions().clear();
        if (dto.type() == TransactionType.INCOMING) {
            createResourceTransactionsForGlazeTx(transaction, glaze, dto.quantity());
        }
        transaction = glazeTransactionRepository.save(transaction);
        return entityToResponseDTO(transaction);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long glazeId, Long transactionId) {
        Glaze glaze = glazeRepository.findById(glazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze não encontrado. Id: " + glazeId));
        GlazeTransaction transaction = glaze.getTransactions().stream()
            .filter(t -> t.getId().equals(transactionId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada. Id: " + transactionId));
        glaze.getTransactions().remove(transaction);
        glazeTransactionRepository.delete(transaction);
    }

    private void createResourceTransactionsForGlazeTx(GlazeTransaction transaction, Glaze glaze, double quantity) {
        for (GlazeResourceUsage usage : glaze.getResourceUsages()) {
            double neededQty = usage.getQuantity() * quantity;
            ResourceTransaction resourceTx = new ResourceTransaction();
            resourceTx.setType(TransactionType.OUTGOING);
            resourceTx.setQuantity(neededQty);
            resourceTx.setResource(usage.getResource());
            resourceTx.setGlazeTransaction(transaction);
            BigDecimal costAtTime = usage.getResource().getUnitValue()
                .multiply(BigDecimal.valueOf(neededQty))
                .setScale(2, RoundingMode.HALF_UP);
            resourceTx.setCostAtTime(costAtTime);
            transaction.getResourceTransactions().add(resourceTx);
        }
    }

    private BigDecimal computeResourceCostAtTime(Glaze glaze, double transactionQty) {
        BigDecimal total = BigDecimal.ZERO;
        for (GlazeResourceUsage usage : glaze.getResourceUsages()) {
            double usageScaled = usage.getQuantity() * transactionQty;
            BigDecimal sub = usage.getResource().getUnitValue()
                .multiply(BigDecimal.valueOf(usageScaled));
            total = total.add(sub);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeMachineCostAtTime(Glaze glaze, double transactionQty) {
        BigDecimal total = BigDecimal.ZERO;
        Resource electricity = resourceRepository.findByCategory(
            com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory.ELECTRICITY)
            .orElseThrow(() -> new ResourceNotFoundException("Resource ELECTRICITY não cadastrado!"));
        for (GlazeMachineUsage mu : glaze.getMachineUsages()) {
            double baseEnergy = mu.getEnergyConsumption(); // p/ 1 kg
            double scaledEnergy = baseEnergy * transactionQty;
            BigDecimal electricityRate = electricity.getUnitValue(); 
            BigDecimal sub = electricityRate.multiply(BigDecimal.valueOf(scaledEnergy));
            total = total.add(sub);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeEmployeeCostAtTime(Glaze glaze, double transactionQty) {
        return glaze.getTotalEmployeeCost()
                .multiply(BigDecimal.valueOf(transactionQty))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private GlazeTransactionResponseDTO entityToResponseDTO(GlazeTransaction entity) {
        Long productTxId = null;
        if (entity.getProductTransaction() != null) {
            productTxId = entity.getProductTransaction().getId();
        }
        return new GlazeTransactionResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getQuantity(),
            entity.getType(),
            entity.getGlaze().getColor(),
            productTxId,
            entity.getResourceTotalCostAtTime(),
            entity.getMachineEnergyConsumptionCostAtTime(),
            entity.getEmployeeTotalCostAtTime(),
            entity.getGlazeFinalCostAtTime()
        );
    }
}
