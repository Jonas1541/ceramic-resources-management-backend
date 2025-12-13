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

import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.product.ProductRepository;
import com.jonasdurau.ceramicmanagement.product.ProductService;
import com.jonasdurau.ceramicmanagement.product.dto.ProductRequestDTO;
import com.jonasdurau.ceramicmanagement.product.dto.ProductResponseDTO;
import com.jonasdurau.ceramicmanagement.product.line.ProductLine;
import com.jonasdurau.ceramicmanagement.product.line.ProductLineRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductOutgoingReason;
import com.jonasdurau.ceramicmanagement.product.type.ProductType;
import com.jonasdurau.ceramicmanagement.product.type.ProductTypeRepository;
import com.jonasdurau.ceramicmanagement.product.validation.ProductDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductTypeRepository typeRepository;

    @Mock
    private ProductLineRepository lineRepository;

    @Mock
    private ProductDeletionValidator productDeletionValidator;

    private ProductService productService;

    private Product product;
    private ProductRequestDTO requestDTO;
    private Long testId;
    private ProductType productType;
    private ProductLine productLine;

    @BeforeEach
    void setUp() {
        testId = 1L;

        this.productService = new ProductService(
            productRepository,
            typeRepository,
            lineRepository,
            List.of(productDeletionValidator)
        );

        productType = new ProductType();
        productType.setId(1L);
        productType.setName("Vaso");

        productLine = new ProductLine();
        productLine.setId(1L);
        productLine.setName("Coleção Verão");

        product = new Product();
        product.setId(testId);
        product.setName("Vaso Decorativo");
        product.setPrice(new BigDecimal("150.00"));
        product.setHeight(30.0);
        product.setLength(20.0);
        product.setWidth(20.0);
        product.setType(productType);
        product.setLine(productLine);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());

        requestDTO = new ProductRequestDTO(
            "Vaso Decorativo",
            new BigDecimal("150.00"),
            30.0,
            20.0,
            20.0,
            2.2,
            1.5,
            1L,
            1L
        );
    }

    @Test
    void findAll_ShouldReturnListOfProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDTO> result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals(testId, result.getFirst().id());
        verify(productRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(testId)).thenReturn(Optional.of(product));

        ProductResponseDTO result = productService.findById(testId);

        assertEquals(testId, result.id());
        assertEquals("Vaso Decorativo", result.name());
        verify(productRepository).findById(testId);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(testId));
        verify(productRepository).findById(testId);
    }

    @Test
    void create_WithValidData_ShouldReturnProduct() {
        when(typeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(lineRepository.findById(1L)).thenReturn(Optional.of(productLine));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(testId);
            return savedProduct;
        });

        ProductResponseDTO result = productService.create(requestDTO);

        assertEquals(testId, result.id());
        assertEquals("Vaso Decorativo", result.name());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_WithInvalidType_ShouldThrowException() {
        when(typeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(requestDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WithInvalidLine_ShouldThrowException() {
        when(typeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(lineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(requestDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_WithValidData_ShouldUpdateProduct() {
        when(productRepository.findById(testId)).thenReturn(Optional.of(product));
        when(typeRepository.findById(1L)).thenReturn(Optional.of(productType));
        when(lineRepository.findById(1L)).thenReturn(Optional.of(productLine));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO result = productService.update(testId, requestDTO);

        assertEquals(testId, result.id());
        assertEquals("Vaso Decorativo", result.name());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.update(testId, requestDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_WhenValidationPasses_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.findById(testId)).thenReturn(Optional.of(product));
        
        // Mock do validador não faz nada (sucesso)

        // Act
        productService.delete(testId);

        // Assert
        verify(productDeletionValidator).validate(testId);
        verify(productRepository).delete(product);
    }

    @Test
    void delete_WhenValidatorThrowsException_ShouldAbortDeletion() {
        // Arrange
        when(productRepository.findById(testId)).thenReturn(Optional.of(product));
        
        // Simula o validador encontrando transações ou outro bloqueio
        doThrow(new ResourceDeletionException("Produto tem transações associadas"))
            .when(productDeletionValidator).validate(testId);

        // Act & Assert
        assertThrows(ResourceDeletionException.class, () -> productService.delete(testId));
        
        verify(productDeletionValidator).validate(testId);
        verify(productRepository, never()).delete(any());
    }

    @Test
    void yearlyReport_WhenProductExists_ShouldReturnReport() {
        ProductTransaction tx = new ProductTransaction();
        tx.setProduct(product);
        tx.setCreatedAt(Instant.now());
        tx.setOutgoingReason(ProductOutgoingReason.SOLD);
        tx.setCost(new BigDecimal("50.00")); 
        product.getTransactions().add(tx);

        when(productRepository.findById(testId)).thenReturn(Optional.of(product));

        List<YearReportDTO> reports = productService.yearlyReport(testId);

        assertFalse(reports.isEmpty());
        verify(productRepository).findById(testId);
    }

    @Test
    void yearlyReport_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.yearlyReport(testId));
        verify(productRepository).findById(testId);
    }
}