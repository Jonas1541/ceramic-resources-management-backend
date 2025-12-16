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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.ResourceService;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceListDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceRequestDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceResponseDTO;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.resource.validation.ResourceDeletionValidator;
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
    private GlazeService glazeService;

    @Mock
    private ResourceDeletionValidator resourceDeletionValidator;

    private ResourceService resourceService;

    private Resource resource;
    private ResourceRequestDTO requestDTO;
    private Long testId;

    @BeforeEach
    void setUp() {
        testId = 1L;

        this.resourceService = new ResourceService(
            resourceRepository,
            glazeService,
            List.of(resourceDeletionValidator)
        );

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
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource saved = invocation.getArgument(0);
            saved.setId(testId);
            // Simula o JPA setando timestamps se necessário, ou mocka o retorno completo
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            return saved;
        });

        ResourceResponseDTO result = resourceService.create(requestDTO);

        assertEquals(testId, result.id());
        verify(resourceRepository).save(any(Resource.class));
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
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        ResourceResponseDTO result = resourceService.update(testId, requestDTO);

        assertEquals(testId, result.id());
        verify(glazeService).recalculateGlazesByResource(testId);
        verify(resourceRepository).save(any(Resource.class));
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
        // existsByCategoryAndIdNot não é chamado se category não mudar ou se name falhar antes? 
        // Na sua lógica, verifica Categoria primeiro, depois nome.
        // Como a categoria é a mesma (WATER) e id é o mesmo, a validação de categoria passa.
        // A validação de nome ocorre pois "Água" != "Água Nova".
    
        assertThrows(BusinessException.class, () -> resourceService.update(testId, updateDTO));
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void delete_WhenValidationPasses_ShouldDeleteResource() {
        // Arrange
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        // Validator mockado não faz nada (sucesso)

        // Act
        resourceService.delete(testId);

        // Assert
        verify(resourceDeletionValidator).validate(testId);
        verify(resourceRepository).delete(resource);
    }

    @Test
    void delete_WhenValidatorThrowsException_ShouldAbortDeletion() {
        // Arrange
        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));
        
        doThrow(new ResourceDeletionException("Recurso em uso"))
            .when(resourceDeletionValidator).validate(testId);

        // Act & Assert
        assertThrows(ResourceDeletionException.class, () -> resourceService.delete(testId));
        
        verify(resourceDeletionValidator).validate(testId);
        verify(resourceRepository, never()).delete(any());
    }

    @Test
    void yearlyReport_WhenResourceExists_ShouldReturnReport() {
        ResourceTransaction tx = new ResourceTransaction();
        tx.setType(TransactionType.INCOMING);
        tx.setQuantity(100.0);
        tx.setCreatedAt(Instant.now());
        
        // Adiciona à lista inicializada no setUp
        resource.getTransactions().add(tx);

        when(resourceRepository.findById(testId)).thenReturn(Optional.of(resource));

        List<YearReportDTO> reports = resourceService.yearlyReport(testId);

        assertFalse(reports.isEmpty());
        // Verifica se houve processamento (Incoming Qty deve ser 100)
        assertEquals(100.0, reports.get(0).getTotalIncomingQty());
        verify(resourceRepository).findById(testId);
    }

    @Test
    void yearlyReport_WhenResourceNotExists_ShouldThrowException() {
        when(resourceRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.yearlyReport(testId));
        verify(resourceRepository).findById(testId);
    }
}