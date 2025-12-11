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

import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransactionRepository;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransactionService;
import com.jonasdurau.ceramicmanagement.resource.transaction.dto.ResourceTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.resource.transaction.dto.ResourceTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ResourceTransactionServiceTest {

    @Mock
    private ResourceTransactionRepository transactionRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceTransactionService transactionService;

    private Resource resource;
    private ResourceTransaction transaction;
    private Long resourceId;
    private Long transactionId;
    private ResourceTransactionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        resourceId = 1L;
        transactionId = 1L;
        
        resource = new Resource();
        resource.setId(resourceId);
        resource.setName("Argila Branca");
        resource.setUnitValue(new BigDecimal("2.50"));

        transaction = new ResourceTransaction();
        transaction.setId(transactionId);
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setQuantity(100.0);
        transaction.setResource(resource);
        transaction.setCostAtTime(new BigDecimal("250.00"));

        requestDTO = new ResourceTransactionRequestDTO(
            TransactionType.INCOMING,
            150.0
        );
    }

    @Test
    void findAllByParentId_WhenResourceExists_ShouldReturnTransactions() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByResource(resource)).thenReturn(List.of(transaction));

        List<ResourceTransactionResponseDTO> result = transactionService.findAllByParentId(resourceId);

        assertFalse(result.isEmpty());
        assertEquals(transactionId, result.getFirst().id());
        verify(transactionRepository).findByResource(resource);
    }

    @Test
    void findAllByParentId_WhenResourceNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.findAllByParentId(resourceId));
    }

    @Test
    void findById_WhenTransactionExists_ShouldReturnTransaction() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByIdAndResource(transactionId, resource)).thenReturn(Optional.of(transaction));

        ResourceTransactionResponseDTO result = transactionService.findById(resourceId, transactionId);

        assertEquals(transactionId, result.id());
        verify(transactionRepository).findByIdAndResource(transactionId, resource);
    }

    @Test
    void findById_WhenTransactionNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByIdAndResource(transactionId, resource)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.findById(resourceId, transactionId));
    }

    @Test
    void create_WhenValidData_ShouldCreateTransaction() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        
        when(transactionRepository.save(any())).thenReturn(transaction);

        ResourceTransactionResponseDTO result = transactionService.create(resourceId, requestDTO);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("250.00").compareTo(result.cost()));
        verify(transactionRepository).save(any());
    }

    @Test
    void create_WhenResourceNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.create(resourceId, requestDTO));
    }

    @Test
    void create_WhenUnitValueNotSet_ShouldThrowException() {
        resource.setUnitValue(null);
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        assertThrows(ResourceNotFoundException.class, () -> transactionService.create(resourceId, requestDTO));
    }

    @Test
    void update_WhenValidData_ShouldUpdateTransaction() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByIdAndResource(transactionId, resource)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);
    
        ResourceTransactionResponseDTO result = transactionService.update(resourceId, transactionId, requestDTO);
    
        assertEquals(150.0, result.quantity());
        verify(transactionRepository).save(any());
    }
    
    @Test
    void update_WhenResourceNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> transactionService.update(resourceId, transactionId, requestDTO));
        verify(transactionRepository, never()).save(any());
    }
    
    @Test
    void update_WhenTransactionNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByIdAndResource(transactionId, resource)).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> transactionService.update(resourceId, transactionId, requestDTO));
        verify(transactionRepository, never()).save(any());
    }
    
    @Test
    void delete_WhenExists_ShouldDeleteTransaction() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByIdAndResource(transactionId, resource)).thenReturn(Optional.of(transaction));
    
        transactionService.delete(resourceId, transactionId);
    
        verify(transactionRepository).delete(transaction);
    }
    
    @Test
    void delete_WhenResourceNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> transactionService.delete(resourceId, transactionId));
        verify(transactionRepository, never()).delete(any());
    }
    
    @Test
    void delete_WhenTransactionNotFound_ShouldThrowException() {
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(transactionRepository.findByIdAndResource(transactionId, resource)).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> transactionService.delete(resourceId, transactionId));
        verify(transactionRepository, never()).delete(any());
    }
}