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

import com.jonasdurau.ceramicmanagement.batch.resourceusage.BatchResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.ResourceService;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceListDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceRequestDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceResponseDTO;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransactionRepository;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceTransactionRepository transactionRepository;

    @Mock
    private BatchResourceUsageRepository batchResourceUsageRepository;

    @Mock
    private GlazeResourceUsageRepository glazeResourceUsageRepository;

    @Mock
    private GlazeService glazeService;

    @InjectMocks
    private ResourceService resourceService;

    private Resource resource;
    private ResourceRequestDTO requestDTO;
    private Long testId;

    @BeforeEach
    void setUp() {
        testId = 1L;

        resource = new Resource();
        resource.setId(testId);
        resource.setName("Água");
        resource.setCategory(ResourceCategory.WATER);
        resource.setUnitValue(new BigDecimal("0.05"));
        resource.setCreatedAt(Instant.now());
        resource.setUpdatedAt(Instant.now());

        requestDTO = new ResourceRequestDTO(
            "Água",
            ResourceCategory.WATER,
            new BigDecimal("0.05")
        );
    }

    @Test
    void findAll_ShouldReturnListOfResources() {
        when(resourceRepository.findAll()).thenReturn(List.of(resource));

        List<ResourceListDTO> result = resourceService.findAll();

        assertEquals(1, result.size());
        assertEquals(testId, result.getFirst().id());
        verify(resourceRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnResource() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));

        ResourceResponseDTO result = resourceService.findById(testId);

        assertEquals(testId, result.id());
        assertEquals("Água", result.name());
        verify(resourceRepository).findById(testId);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.findById(testId));
        verify(resourceRepository).findById(testId);
    }

    @Test
    void create_WithValidData_ShouldReturnResource() {
        when(resourceRepository.existsByName(any())).thenReturn(false);
        when(resourceRepository.existsByCategory(any())).thenReturn(false);
        when(resourceRepository.save(any())).thenAnswer(invocation -> {
            Resource saved = invocation.getArgument(0);
            saved.setId(testId);
            return saved;
        });

        ResourceResponseDTO result = resourceService.create(requestDTO);

        assertEquals(testId, result.id());
        verify(resourceRepository).save(any());
    }

    @Test
    void create_WithDuplicateName_ShouldThrowBusinessException() {
        when(resourceRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> resourceService.create(requestDTO));
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void create_WithDuplicateUniqueCategory_ShouldThrowBusinessException() {
        when(resourceRepository.existsByCategory(requestDTO.category())).thenReturn(true);

        assertThrows(BusinessException.class, () -> resourceService.create(requestDTO));
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void update_WithValidData_ShouldUpdateResource() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        when(resourceRepository.existsByCategoryAndIdNot(any(), any())).thenReturn(false);
        when(resourceRepository.save(any())).thenReturn(resource);

        ResourceResponseDTO result = resourceService.update(testId, requestDTO);

        assertEquals(testId, result.id());
        verify(glazeService).recalculateGlazesByResource(testId);
    }

    @Test
    void update_WithDuplicateName_ShouldThrowBusinessException() {
        ResourceRequestDTO updateDTO = new ResourceRequestDTO(
            "Água Nova",
            ResourceCategory.WATER,
            new BigDecimal("0.05")
        );
    
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        when(resourceRepository.existsByName(updateDTO.name())).thenReturn(true);
        when(resourceRepository.existsByCategoryAndIdNot(any(), any())).thenReturn(false);
    
        assertThrows(BusinessException.class, () -> resourceService.update(testId, updateDTO));
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void delete_WhenNoDependencies_ShouldDeleteResource() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        when(transactionRepository.existsByResourceId(testId)).thenReturn(false);
        when(batchResourceUsageRepository.existsByResourceId(testId)).thenReturn(false);
        when(glazeResourceUsageRepository.existsByResourceId(testId)).thenReturn(false);

        resourceService.delete(testId);

        verify(resourceRepository).delete(resource);
    }

    @Test
    void delete_WhenHasTransactions_ShouldThrowException() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        when(transactionRepository.existsByResourceId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> resourceService.delete(testId));
        verify(resourceRepository, never()).delete(any());
    }

    @Test
    void delete_WhenHasBatchUsages_ShouldThrowException() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        when(batchResourceUsageRepository.existsByResourceId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> resourceService.delete(testId));
        verify(resourceRepository, never()).delete(any());
    }

    @Test
    void delete_WhenHasGlazeUsages_ShouldThrowException() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        when(glazeResourceUsageRepository.existsByResourceId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> resourceService.delete(testId));
        verify(resourceRepository, never()).delete(any());
    }

    @Test
    void yearlyReport_WhenResourceExists_ShouldReturnReport() {
        ResourceTransaction tx = new ResourceTransaction();
        tx.setType(TransactionType.INCOMING);
        tx.setQuantity(100.0);
        tx.setCreatedAt(Instant.now());
        resource.getTransactions().add(tx);

        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));

        List<YearReportDTO> reports = resourceService.yearlyReport(testId);

        assertFalse(reports.isEmpty());
        verify(resourceRepository).findById(testId);
    }

    @Test
    void yearlyReport_WhenResourceNotExists_ShouldThrowException() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.yearlyReport(testId));
        verify(resourceRepository).findById(testId);
    }
}