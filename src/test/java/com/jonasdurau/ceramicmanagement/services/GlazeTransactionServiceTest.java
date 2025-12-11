package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.glaze.GlazeRepository;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsage;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsage;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransaction;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransactionRepository;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransactionService;
import com.jonasdurau.ceramicmanagement.glaze.transaction.dto.GlazeTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.transaction.dto.GlazeTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class GlazeTransactionServiceTest {

    @Mock
    private GlazeTransactionRepository glazeTransactionRepository;

    @Mock
    private GlazeRepository glazeRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private GlazeTransactionService glazeTransactionService;

    private Glaze glaze;
    private GlazeTransaction transaction;
    private GlazeTransactionRequestDTO requestDTO;
    private Product product;
    private ProductTransaction productTransaction;
    private Long glazeId;
    private Long transactionId;
    private Long productTransactionId = 100L;
    private Resource electricityResource;
    private Resource materialResource;
    private Machine machine;

    @BeforeEach
    void setUp() {
        glazeId = 1L;
        transactionId = 1L;

        materialResource = new Resource();
        materialResource.setId(1L);
        materialResource.setUnitValue(new BigDecimal("2.00"));

        machine = new Machine();
        machine.setId(1L);
        machine.setPower(1.5);

        electricityResource = new Resource();
        electricityResource.setId(2L);
        electricityResource.setCategory(ResourceCategory.ELECTRICITY);
        electricityResource.setUnitValue(new BigDecimal("0.50"));

        glaze = new Glaze();
        glaze.setId(glazeId);
        glaze.setColor("Azul");
        
        GlazeResourceUsage resourceUsage = new GlazeResourceUsage();
        resourceUsage.setResource(materialResource);
        resourceUsage.setQuantity(0.5);
        glaze.getResourceUsages().add(resourceUsage);

        GlazeMachineUsage machineUsage = new GlazeMachineUsage();
        machineUsage.setMachine(machine);

        glaze.getMachineUsages().add(machineUsage);

        transaction = new GlazeTransaction();
        transaction.setId(transactionId);
        transaction.setType(TransactionType.INCOMING);
        transaction.setQuantity(150.0);
        
        BigDecimal resourceCost = new BigDecimal("150.00"); // 150.0 * 0.5 * 2.00
        BigDecimal machineCost = new BigDecimal("112.50"); // 150.0 * 1.5 * 0.50
        BigDecimal totalCost = new BigDecimal("262.50");
        
        transaction.setResourceTotalCostAtTime(resourceCost);
        transaction.setMachineEnergyConsumptionCostAtTime(machineCost);
        transaction.setGlazeFinalCostAtTime(totalCost);
        transaction.setGlaze(glaze);

        product = new Product();
        product.setId(1L);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(null);
        product.setName("product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setHeight(1.1);
        product.setLength(1.1);
        product.setWidth(1.1);
        product.setglazeQuantityPerUnit(2.2);

        productTransaction = new ProductTransaction();
        productTransaction.setId(productTransactionId);
        productTransaction.setProduct(product);
        transaction.setProductTransaction(productTransaction);

        glaze.getTransactions().add(transaction);

        requestDTO = new GlazeTransactionRequestDTO(
            150.0,
            TransactionType.INCOMING
        );
    }

    @Test
    void findAllByParentId_WhenGlazeExists_ShouldReturnTransactions() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));

        List<GlazeTransactionResponseDTO> result = glazeTransactionService.findAllByParentId(glazeId);

        assertFalse(result.isEmpty());
        assertEquals(transactionId, result.getFirst().id());

        assertEquals(productTransactionId, result.getFirst().productTxId());
    }

    @Test
    void findAllByParentId_WhenGlazeNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.findAllByParentId(glazeId));
    }

    @Test
    void findById_WhenExists_ShouldReturnTransaction() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));

        GlazeTransactionResponseDTO result = glazeTransactionService.findById(glazeId, transactionId);

        assertEquals(transactionId, result.id());

        assertEquals(productTransactionId, result.productTxId());
    }
    
    @Test
    void entityToResponseDTO_WhenProductTransactionIsNull_ShouldReturnNullProductTxId() {
        transaction.setProductTransaction(null);
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));
        
        GlazeTransactionResponseDTO result = glazeTransactionService.findById(glazeId, transactionId);
        
        assertNull(result.productTxId());
    }

    @Test
    void findById_WhenGlazeNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.findById(glazeId, transactionId));
    }

    @Test
    void findById_WhenTransactionNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.findById(glazeId, 999L));
    }

    @Test
    void create_WhenValidData_ShouldCreateTransaction() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY))
            .thenReturn(Optional.of(electricityResource));
        when(glazeTransactionRepository.save(any())).thenReturn(transaction);

        GlazeTransactionResponseDTO result = glazeTransactionService.create(glazeId, requestDTO);

        assertNotNull(result);
        verify(glazeTransactionRepository).save(any());
    }

    @Test
    void create_WhenGlazeNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.create(glazeId, requestDTO));
    }

    @Test
    void create_WhenElectricityResourceMissing_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.create(glazeId, requestDTO));
    }

    @Test
    void update_WhenValidData_ShouldUpdateTransaction() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));
        when(glazeTransactionRepository.save(any())).thenReturn(transaction);

        GlazeTransactionResponseDTO result = glazeTransactionService.update(glazeId, transactionId, requestDTO);

        assertEquals(150.0, result.quantity());
        verify(glazeTransactionRepository).save(any());
    }

    @Test
    void update_WhenGlazeNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.update(glazeId, transactionId, requestDTO));
    }

    @Test
    void update_WhenTransactionNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.update(glazeId, 999L, requestDTO));
    }

    @Test
    void delete_WhenExists_ShouldDeleteTransaction() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));

        glazeTransactionService.delete(glazeId, transactionId);

        verify(glazeTransactionRepository).delete(any());
    }

    @Test
    void delete_WhenGlazeNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.delete(glazeId, transactionId));
    }

    @Test
    void delete_WhenTransactionNotFound_ShouldThrowException() {
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));

        assertThrows(ResourceNotFoundException.class, () -> glazeTransactionService.delete(glazeId, 999L));
    }

    @Test
    void createEntity_WhenValidData_ShouldCreateTransaction() {

        GlazeTransaction mockTransaction = new GlazeTransaction();
        mockTransaction.setType(TransactionType.OUTGOING);
        mockTransaction.setQuantity(150.0);
        mockTransaction.setProductTransaction(productTransaction);
    
        when(glazeRepository.findById(glazeId)).thenReturn(Optional.of(glaze));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));
        when(glazeTransactionRepository.save(any(GlazeTransaction.class))).thenReturn(mockTransaction);

        GlazeTransaction result = glazeTransactionService.createEntity(glazeId, productTransaction);
    
        assertNotNull(result);
        assertEquals(TransactionType.OUTGOING, result.getType());
        assertEquals(150.0, result.getQuantity());

        assertEquals(productTransaction, result.getProductTransaction());
    }
}