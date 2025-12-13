package com.jonasdurau.ceramicmanagement.product.transaction.validation;

import org.springframework.stereotype.Component;

import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransactionRepository;
import com.jonasdurau.ceramicmanagement.product.validation.ProductDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;

@Component
public class ProductTransactionValidator implements ProductDeletionValidator {

    private final ProductTransactionRepository repository;

    public ProductTransactionValidator(ProductTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(Long productId) {
        if(repository.existsByProductId(productId)) {
            throw new ResourceDeletionException("Não é possível deletar o produto pois ele possui transações associadas.");
        }
    }
    
}
