package com.jonasdurau.ceramicmanagement.product.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.product.ProductRepository;
import com.jonasdurau.ceramicmanagement.product.line.validation.ProductLineDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class ProductLineValidator implements ProductLineDeletionValidator {

    private final ProductRepository repository;

    public ProductLineValidator(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long productLineId) {
        if(repository.existsByLineId(productLineId)) {
            throw new ResourceDeletionException("Não é possível deletar essa linha de produto pois ela possui produtos associados.");
        }
    }
    
}
