package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.product.type.ProductType;
import com.jonasdurau.ceramicmanagement.product.type.ProductTypeRepository;
import com.jonasdurau.ceramicmanagement.product.type.ProductTypeService;
import com.jonasdurau.ceramicmanagement.product.type.dto.ProductTypeRequestDTO;
import com.jonasdurau.ceramicmanagement.product.type.dto.ProductTypeResponseDTO;
import com.jonasdurau.ceramicmanagement.product.type.validation.ProductTypeDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductTypeServiceTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductTypeDeletionValidator deletionValidator;

    private ProductTypeService productTypeService;

    private ProductType productType;
    private ProductTypeRequestDTO requestDTO;
    private Long typeId;

    @BeforeEach
    void setUp() {
        typeId = 1L;

        productTypeService = new ProductTypeService(
            productTypeRepository,
            List.of(deletionValidator)
        );

        productType = new ProductType();
        productType.setId(typeId);
        productType.setName("Vaso Decorativo");
        productType.setCreatedAt(Instant.now());
        productType.setUpdatedAt(Instant.now());

        requestDTO = new ProductTypeRequestDTO("Prato Artesanal");
    }

    @Test
    void findAll_ShouldReturnAllProductTypes() {
        when(productTypeRepository.findAll()).thenReturn(List.of(productType));

        List<ProductTypeResponseDTO> result = productTypeService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(typeId, result.getFirst().id());
    }

    @Test
    void findById_WhenExists_ShouldReturnProductType() {
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.of(productType));

        ProductTypeResponseDTO result = productTypeService.findById(typeId);

        assertEquals(typeId, result.id());
        assertEquals("Vaso Decorativo", result.name());
        assertEquals(0, result.productQuantity());
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productTypeService.findById(typeId));
    }

    @Test
    void create_WhenValidData_ShouldCreateProductType() {
        when(productTypeRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(productTypeRepository.save(any(ProductType.class))).thenAnswer(invocation -> {
            ProductType saved = invocation.getArgument(0);
            saved.setId(typeId);
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            return saved;
        });

        ProductTypeResponseDTO result = productTypeService.create(requestDTO);

        assertNotNull(result);
        assertEquals(requestDTO.name(), result.name());
        assertEquals(0, result.productQuantity());
        verify(productTypeRepository).save(any(ProductType.class));
    }

    @Test
    void create_WhenDuplicateName_ShouldThrowException() {
        when(productTypeRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> productTypeService.create(requestDTO));
        verify(productTypeRepository, never()).save(any());
    }

    @Test
    void update_WhenValidData_ShouldUpdateProductType() {
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.of(productType));
        when(productTypeRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(productType);

        ProductTypeResponseDTO result = productTypeService.update(typeId, requestDTO);

        assertEquals(requestDTO.name(), result.name());
        verify(productTypeRepository).save(any(ProductType.class));
    }

    @Test
    void update_WhenSameName_ShouldUpdateWithoutCheck() {
        requestDTO = new ProductTypeRequestDTO("Vaso Decorativo");
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.of(productType));
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(productType);

        ProductTypeResponseDTO result = productTypeService.update(typeId, requestDTO);

        assertEquals(productType.getName(), result.name());
        // Garante que não chamou verificação de duplicidade
        verify(productTypeRepository, never()).existsByName(any());
    }

    @Test
    void update_WhenNewNameExists_ShouldThrowException() {
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.of(productType));
        when(productTypeRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> productTypeService.update(typeId, requestDTO));
        verify(productTypeRepository, never()).save(any());
    }

    @Test
    void delete_WhenValidationPasses_ShouldDelete() {
        // Arrange
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.of(productType));
        // Validator mockado não faz nada (sucesso)

        // Act
        productTypeService.delete(typeId);

        // Assert
        verify(deletionValidator).validate(typeId);
        verify(productTypeRepository).delete(productType);
    }

    @Test
    void delete_WhenValidatorThrowsException_ShouldAbortDeletion() {
        // Arrange
        when(productTypeRepository.findById(typeId)).thenReturn(Optional.of(productType));
        
        doThrow(new ResourceDeletionException("Tipo possui produtos associados"))
            .when(deletionValidator).validate(typeId);

        // Act & Assert
        assertThrows(ResourceDeletionException.class, () -> productTypeService.delete(typeId));
        
        verify(deletionValidator).validate(typeId);
        verify(productTypeRepository, never()).delete(any());
    }
}