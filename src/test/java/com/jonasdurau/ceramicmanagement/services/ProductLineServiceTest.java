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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.product.ProductRepository;
import com.jonasdurau.ceramicmanagement.product.line.ProductLine;
import com.jonasdurau.ceramicmanagement.product.line.ProductLineRepository;
import com.jonasdurau.ceramicmanagement.product.line.ProductLineService;
import com.jonasdurau.ceramicmanagement.product.line.dto.ProductLineRequestDTO;
import com.jonasdurau.ceramicmanagement.product.line.dto.ProductLineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductLineServiceTest {

    @Mock
    private ProductLineRepository productLineRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductLineService productLineService;

    private ProductLine productLine;
    private ProductLineRequestDTO requestDTO;
    private Long lineId;

    @BeforeEach
    void setUp() {
        lineId = 1L;
        
        productLine = new ProductLine();
        productLine.setId(lineId);
        productLine.setName("Coleção Verão");
        productLine.setCreatedAt(Instant.now());
        productLine.setUpdatedAt(Instant.now());

        requestDTO = new ProductLineRequestDTO("Coleção Inverno");
    }

    @Test
    void findAll_ShouldReturnAllProductLines() {
        when(productLineRepository.findAll()).thenReturn(List.of(productLine));

        List<ProductLineResponseDTO> result = productLineService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(lineId, result.getFirst().id());
    }

    @Test
    void findById_WhenExists_ShouldReturnProductLine() {
        when(productLineRepository.findById(lineId)).thenReturn(Optional.of(productLine));

        ProductLineResponseDTO result = productLineService.findById(lineId);

        assertEquals(lineId, result.id());
        assertEquals("Coleção Verão", result.name());
        assertEquals(0, result.productQuantity());
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(productLineRepository.findById(lineId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productLineService.findById(lineId));
    }

    @Test
    void create_WhenValidData_ShouldCreateProductLine() {
        when(productLineRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(productLineRepository.save(any())).thenAnswer(invocation -> {
            ProductLine saved = invocation.getArgument(0);
            saved.setId(lineId);
            return saved;
        });

        ProductLineResponseDTO result = productLineService.create(requestDTO);

        assertNotNull(result);
        assertEquals(requestDTO.name(), result.name());
        assertEquals(0, result.productQuantity());
        verify(productLineRepository).save(any());
    }

    @Test
    void create_WhenDuplicateName_ShouldThrowException() {
        when(productLineRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> productLineService.create(requestDTO));
        verify(productLineRepository, never()).save(any());
    }

    @Test
    void update_WhenValidData_ShouldUpdateProductLine() {
        when(productLineRepository.findById(lineId)).thenReturn(Optional.of(productLine));
        when(productLineRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(productLineRepository.save(any())).thenReturn(productLine);

        ProductLineResponseDTO result = productLineService.update(lineId, requestDTO);

        assertEquals(requestDTO.name(), result.name());
        verify(productLineRepository).save(any());
    }

    @Test
    void update_WhenSameName_ShouldUpdateWithoutCheck() {
        requestDTO = new ProductLineRequestDTO("Coleção Verão");
        when(productLineRepository.findById(lineId)).thenReturn(Optional.of(productLine));
        when(productLineRepository.save(any())).thenReturn(productLine);

        ProductLineResponseDTO result = productLineService.update(lineId, requestDTO);

        assertEquals(productLine.getName(), result.name());
        verify(productLineRepository, never()).existsByName(any());
    }

    @Test
    void update_WhenNewNameExists_ShouldThrowException() {
        when(productLineRepository.findById(lineId)).thenReturn(Optional.of(productLine));
        when(productLineRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> productLineService.update(lineId, requestDTO));
        verify(productLineRepository, never()).save(any());
    }

    @Test
    void delete_WhenNoProducts_ShouldDelete() {
        when(productLineRepository.findById(lineId)).thenReturn(Optional.of(productLine));
        when(productRepository.existsByLineId(lineId)).thenReturn(false);

        productLineService.delete(lineId);

        verify(productLineRepository).delete(productLine);
    }

    @Test
    void delete_WhenHasProducts_ShouldThrowException() {
        when(productLineRepository.findById(lineId)).thenReturn(Optional.of(productLine));
        when(productRepository.existsByLineId(lineId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> productLineService.delete(lineId));
        verify(productLineRepository, never()).delete(any());
    }
}