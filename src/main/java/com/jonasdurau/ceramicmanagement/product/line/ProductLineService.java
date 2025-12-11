package com.jonasdurau.ceramicmanagement.product.line;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.product.ProductRepository;
import com.jonasdurau.ceramicmanagement.product.line.dto.ProductLineRequestDTO;
import com.jonasdurau.ceramicmanagement.product.line.dto.ProductLineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class ProductLineService implements IndependentCrudService<ProductLineResponseDTO, ProductLineRequestDTO, ProductLineResponseDTO, Long> {
    
    @Autowired
    private ProductLineRepository productLineRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<ProductLineResponseDTO> findAll() {
        List<ProductLine> list = productLineRepository.findAll();
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public ProductLineResponseDTO findById(Long id) {
        ProductLine entity = productLineRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Linha de produto não encontrada. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ProductLineResponseDTO create(ProductLineRequestDTO dto) {
        if(productLineRepository.existsByName(dto.name())) {
            throw new BusinessException("O nome " + dto.name() + " já existe.");
        }
        ProductLine entity = new ProductLine();
        entity.setName(dto.name());
        entity = productLineRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ProductLineResponseDTO update(Long id, ProductLineRequestDTO dto) {
        ProductLine entity = productLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Linha de produto não encontrada. id: " + id));
        String oldName = entity.getName();
        String newName = dto.name();
        if(!newName.equals(oldName) && productLineRepository.existsByName(newName)) {
            throw new BusinessException("O nome " + newName + " já existe.");
        }
        entity.setName(newName);
        entity = productLineRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        ProductLine entity = productLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Linha de produto não encontrada. Id " + id));
        boolean hasProducts = productRepository.existsByLineId(id);
        if(hasProducts) {
            throw new ResourceDeletionException("Não é possível deletar a linha de produtos de Id " + id + " pois ela tem produtos associados.");
        }
        productLineRepository.delete(entity);
    }

    private ProductLineResponseDTO entityToResponseDTO(ProductLine entity) {
        ProductLineResponseDTO dto = new ProductLineResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getProductQuantity()
        );
        return dto;
    }
}
