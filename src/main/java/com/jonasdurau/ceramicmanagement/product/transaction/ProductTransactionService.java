package com.jonasdurau.ceramicmanagement.product.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.batch.BatchRepository;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.product.ProductRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.dto.ProductTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.product.transaction.dto.ProductTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.product.transaction.employeeusage.ProductTransactionEmployeeUsage;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductOutgoingReason;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@Service
public class ProductTransactionService {
    
    @Autowired
    private ProductTransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<ProductTransactionResponseDTO> findAllByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Produto não encontrado. Id: " + productId));
        List<ProductTransaction> list = transactionRepository.findByProduct(product);
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<ProductTransactionResponseDTO> findAllByState(ProductState state) {
        List<ProductTransaction> list = transactionRepository.findByState(state);
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public ProductTransactionResponseDTO findById(Long productId, Long transactionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));
        ProductTransaction transaction = transactionRepository.findByIdAndProduct(transactionId, product)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada. Id: " + transactionId));
        return entityToResponseDTO(transaction);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public List<ProductTransactionResponseDTO> create(Long productId, int quantity, ProductTransactionRequestDTO dto) {
        if (!batchRepository.anyExists()) {
            throw new BusinessException("Não é possível criar uma transação de produto, pois não há nenhuma batelada cadastrada para a base de cálculo de custo.");
        }
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));

        List<ProductTransactionEmployeeUsage> preparedEmployeeUsages = new ArrayList<>();
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            double individualUsageTime = euDTO.usageTime() / quantity;

            Employee employee = employeeRepository.findById(euDTO.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado. Id: " + euDTO.employeeId()));

            ProductTransactionEmployeeUsage employeeUsage = new ProductTransactionEmployeeUsage();
            employeeUsage.setEmployee(employee);
            employeeUsage.setUsageTime(individualUsageTime);
            preparedEmployeeUsages.add(employeeUsage);
        }

        List<ProductTransaction> savedTransactions = new ArrayList<>();

        long currentCounter = product.getUnitCounter();

        for (int i = 0; i < quantity; i++) {
            currentCounter++;
            ProductTransaction transaction = new ProductTransaction();
            transaction.setState(ProductState.GREENWARE);
            transaction.setProduct(product);

            transaction.setUnitName("Unidade " + currentCounter);

            List<ProductTransactionEmployeeUsage> usagesForThisTransaction = new ArrayList<>();
            for (ProductTransactionEmployeeUsage preparedUsage : preparedEmployeeUsages) {
                ProductTransactionEmployeeUsage newUsage = new ProductTransactionEmployeeUsage();
                newUsage.setEmployee(preparedUsage.getEmployee());
                newUsage.setUsageTime(preparedUsage.getUsageTime());
                newUsage.setProductTransaction(transaction);
                usagesForThisTransaction.add(newUsage);
            }
            transaction.getEmployeeUsages().addAll(usagesForThisTransaction);
            transaction.setCost(calculateProductTransactionCost(product.getWeight(), transaction));
            savedTransactions.add(transaction);
        }

        product.setUnitCounter(currentCounter);
        productRepository.save(product);

        List<ProductTransaction> finalTransactions = transactionRepository.saveAll(savedTransactions);

        return finalTransactions.stream().map(this::entityToResponseDTO).toList();
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long productId, Long transactionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));
        ProductTransaction entity = transactionRepository.findByIdAndProduct(transactionId, product)
                .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + transactionId));
        if(entity.getBisqueFiring() != null && entity.getGlazeFiring() == null) {
            throw new ResourceDeletionException("A transação do produto não pode ser deletada pois está em uma 1° queima.");
        }
        if(entity.getGlazeFiring() != null) {
            throw new ResourceDeletionException("A transação do produto não pode ser deletada pois está em uma 2° queima.");
        }
        transactionRepository.delete(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public ProductTransactionResponseDTO outgoing(Long productId, Long transactionId, ProductOutgoingReason outgoingReason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));
        ProductTransaction entity = transactionRepository.findByIdAndProduct(transactionId, product)
                .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + transactionId));
        entity.setOutgoingReason(outgoingReason);
        entity.setOutgoingAt(Instant.now());
        entity = transactionRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public List<ProductTransactionResponseDTO> outgoingByQuantity(Long productId, int quantity, ProductState state, ProductOutgoingReason outgoingReason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));
        List<ProductTransaction> availableTransactions = transactionRepository
                .findByProductAndStateAndOutgoingReasonIsNullOrderByCreatedAtAsc(product, state, PageRequest.of(0, quantity));
        if (availableTransactions.size() < quantity) {
            throw new ResourceNotFoundException("Quantidade solicitada maior que o estoque disponível.");
        }
        Instant now = Instant.now();
        for (ProductTransaction tx : availableTransactions) {
            tx.setOutgoingReason(outgoingReason);
            tx.setOutgoingAt(now);
        }
        List<ProductTransaction> saved = transactionRepository.saveAll(availableTransactions);
        return saved.stream().map(this::entityToResponseDTO).toList();
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public ProductTransactionResponseDTO cancelOutgoing(Long productId, Long transactionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));
        ProductTransaction entity = transactionRepository.findByIdAndProduct(transactionId, product)
                .orElseThrow(() -> new ResourceNotFoundException("Transação de produto não encontrada. Id: " + transactionId));
        entity.setOutgoingReason(null);
        entity.setOutgoingAt(null);
        entity = transactionRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public List<ProductTransactionResponseDTO> cancelOutgoingByQuantity(Long productId, int quantity, ProductState state) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + productId));
        List<ProductTransaction> availableTransactions = transactionRepository
                .findByProductAndStateAndOutgoingReasonIsNotNullOrderByCreatedAtAsc(product, state, PageRequest.of(0, quantity));
        if (availableTransactions.size() < quantity) {
            throw new ResourceNotFoundException("Quantidade solicitada maior que o estoque disponível.");
        }
        for (ProductTransaction tx : availableTransactions) {
            tx.setOutgoingReason(null);
            tx.setOutgoingAt(null);
        }
        List<ProductTransaction> saved = transactionRepository.saveAll(availableTransactions);
        return saved.stream().map(this::entityToResponseDTO).toList();
    }

    private ProductTransactionResponseDTO entityToResponseDTO(ProductTransaction entity) {
        String glazeColor = "sem glasura";
        double glazeQuantity = 0.0;
        Long bisqueFiringId = null;
        Long glazeFiringId = null;

        if (entity.getGlazeTransaction() != null) {
            glazeColor = entity.getGlazeTransaction().getGlaze().getColor();
            glazeQuantity = entity.getGlazeTransaction().getQuantity();
        }
        if (entity.getBisqueFiring() != null) {
            bisqueFiringId = entity.getBisqueFiring().getId();
        }
        if (entity.getGlazeFiring() != null) {
            glazeFiringId = entity.getGlazeFiring().getId();
        }

        List<EmployeeUsageResponseDTO> employeeUsagesDTO = entity.getEmployeeUsages().stream()
                .map(this::employeeUsageToDTO)
                .toList();

        return new ProductTransactionResponseDTO(
                entity.getId(),
                entity.getUnitName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getOutgoingAt(),
                entity.getState(),
                entity.getOutgoingReason(),
                entity.getProduct().getName(),
                bisqueFiringId,
                glazeFiringId,
                glazeColor,
                glazeQuantity,
                employeeUsagesDTO,
                entity.getTotalEmployeeCost(),
                calculateBatchCost(entity.getProduct().getWeight()),
                entity.getBisqueFiringCost(),
                entity.getGlazeFiringCost(),
                entity.getGlazeTransactionCost(),
                entity.getTotalCost(),
                entity.getProfit());
    }

    private EmployeeUsageResponseDTO employeeUsageToDTO(ProductTransactionEmployeeUsage usage) {
        return new EmployeeUsageResponseDTO(
                usage.getEmployee().getId(),
                usage.getEmployee().getName(),
                usage.getUsageTime(),
                usage.getCost());
    }

    private BigDecimal calculateBatchCost(double transactionWeight) {
        Double totalWeight = batchRepository.getTotalWeight();
        BigDecimal totalCost = batchRepository.getTotalFinalCost();

        if (totalWeight == null || totalWeight == 0) {
            throw new IllegalStateException("Peso total do lote é nulo ou zero, impossível calcular o custo do material.");
        }

        BigDecimal transactionWeightBD = BigDecimal.valueOf(transactionWeight);

        // Fórmula: (custoTotalLote * pesoTransacao) / pesoTotalLote
        return totalCost
                .multiply(transactionWeightBD)
                .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateProductTransactionCost(double transactionWeight, ProductTransaction productTransaction) {
        BigDecimal materialCost = calculateBatchCost(transactionWeight);
        BigDecimal employeeCost = productTransaction.getTotalEmployeeCost();
        return materialCost.add(employeeCost);
    }

}
