package com.jonasdurau.ceramicmanagement.product.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.product.ProductRepository;
import com.jonasdurau.ceramicmanagement.product.type.validation.ProductTypeDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class ProductTypeValidator implements ProductTypeDeletionValidator {

    private final ProductRepository repository;

    public ProductTypeValidator(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long productTypeId) {
        if(repository.existsByTypeId(productTypeId)) {
            throw new ResourceDeletionException("Não é possível deletar o tipo de produto pois há produtos associados.");
        }
    }
    
}
