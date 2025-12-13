package com.jonasdurau.ceramicmanagement.product;

import java.math.BigDecimal;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.AbstractMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.product.dto.ProductRequestDTO;
import com.jonasdurau.ceramicmanagement.product.dto.ProductResponseDTO;
import com.jonasdurau.ceramicmanagement.product.line.ProductLine;
import com.jonasdurau.ceramicmanagement.product.line.ProductLineRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.product.type.ProductType;
import com.jonasdurau.ceramicmanagement.product.type.ProductTypeRepository;
import com.jonasdurau.ceramicmanagement.product.validation.ProductDeletionValidator;
import com.jonasdurau.ceramicmanagement.shared.dto.MonthReportDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class ProductService implements IndependentCrudService<ProductResponseDTO, ProductRequestDTO, ProductResponseDTO, Long> {
    
    private final ProductRepository productRepository;
    private final ProductTypeRepository typeRepository;
    private final ProductLineRepository lineRepository;
    private final List<ProductDeletionValidator> deletionValidators;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductTypeRepository typeRepository,
            ProductLineRepository lineRepository, List<ProductDeletionValidator> deletionValidators) {
        this.productRepository = productRepository;
        this.typeRepository = typeRepository;
        this.lineRepository = lineRepository;
        this.deletionValidators = deletionValidators;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<ProductResponseDTO> findAll() {
        List<Product> list = productRepository.findAll();
        return list.stream().map(this::entityToDTO).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public ProductResponseDTO findById(Long id) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + id));
        return entityToDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product entity = new Product();
        entity.setName(dto.name());
        entity.setPrice(dto.price());
        entity.setHeight(dto.height());
        entity.setLength(dto.length());
        entity.setWidth(dto.width());
        entity.setglazeQuantityPerUnit(dto.glazeQuantityPerUnit());
        entity.setWeight(dto.weight());
        ProductType type = typeRepository.findById(dto.typeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo não encontrado. Id: " + dto.typeId()));
        ProductLine line = lineRepository.findById(dto.lineId())
                .orElseThrow(() -> new ResourceNotFoundException("Linha não encontrada. Id: " + dto.lineId()));
        entity.setType(type);
        entity.setLine(line);
        entity = productRepository.save(entity);
        return entityToDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + id));
        entity.setName(dto.name());
        entity.setPrice(dto.price());
        entity.setHeight(dto.height());
        entity.setLength(dto.length());
        entity.setWidth(dto.width());
        entity.setglazeQuantityPerUnit(dto.glazeQuantityPerUnit());
        entity.setWeight(dto.weight());
        ProductType type = typeRepository.findById(dto.typeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo não encontrado. Id: " + dto.typeId()));
        ProductLine line = lineRepository.findById(dto.lineId())
                .orElseThrow(() -> new ResourceNotFoundException("Linha não encontrada. Id: " + dto.lineId()));
        entity.setType(type);
        entity.setLine(line);
        entity = productRepository.save(entity);
        return entityToDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. Id: " + id));
        deletionValidators.forEach(validator -> validator.validate(id));
        productRepository.delete(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<YearReportDTO> yearlyReport(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        List<ProductTransaction> txs = product.getTransactions();
        ZoneId zone = ZoneId.systemDefault();
        Map<Integer, Map<Month, List<ProductTransaction>>> mapYearMonth = txs.stream()
                .map(t -> new AbstractMap.SimpleEntry<>(t, t.getCreatedAt().atZone(zone)))
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getYear(),
                        Collectors.groupingBy(
                                entry -> entry.getValue().getMonth(),
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList()))));
        List<YearReportDTO> yearReports = new ArrayList<>();
        for (Map.Entry<Integer, Map<Month, List<ProductTransaction>>> yearEntry : mapYearMonth.entrySet()) {
            int year = yearEntry.getKey();
            Map<Month, List<ProductTransaction>> mapMonth = yearEntry.getValue();
            YearReportDTO yearReport = new YearReportDTO(year);
            double totalIncomingQtyYear = 0.0;
            double totalOutgoingQtyYear = 0.0;
            BigDecimal totalIncomingCostYear = BigDecimal.ZERO;
            BigDecimal totalProfitYear = BigDecimal.ZERO;
            for (Month m : Month.values()) {
                List<ProductTransaction> monthTx = mapMonth.getOrDefault(m, Collections.emptyList());
                double incomingQty = 0.0;
                double outgoingQty = 0.0;
                BigDecimal incomingCost = BigDecimal.ZERO;
                BigDecimal profit = BigDecimal.ZERO;
                for (ProductTransaction t : monthTx) {
                    incomingQty += 1;
                    incomingCost = incomingCost.add(t.getTotalCost());
                    if (t.getOutgoingReason() != null) {
                        outgoingQty += 1;
                        profit = profit.add(t.getProfit());
                    }
                }
                totalIncomingQtyYear += incomingQty;
                totalOutgoingQtyYear += outgoingQty;
                totalIncomingCostYear = totalIncomingCostYear.add(incomingCost);
                totalProfitYear = totalProfitYear.add(profit);
                MonthReportDTO monthDto = new MonthReportDTO();
                monthDto.setMonthName(m.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                monthDto.setIncomingQty(incomingQty);
                monthDto.setIncomingCost(incomingCost);
                monthDto.setOutgoingQty(outgoingQty);
                monthDto.setOutgoingProfit(profit);
                yearReport.getMonths().add(monthDto);
            }
            yearReport.setTotalIncomingQty(totalIncomingQtyYear);
            yearReport.setTotalIncomingCost(totalIncomingCostYear);
            yearReport.setTotalOutgoingQty(totalOutgoingQtyYear);
            yearReport.setTotalOutgoingProfit(totalProfitYear);
            yearReports.add(yearReport);
        }
        yearReports.sort((a, b) -> b.getYear() - a.getYear());
        return yearReports;
    }

    private ProductResponseDTO entityToDTO(Product entity) {
        return new ProductResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getPrice(),
            entity.getHeight(),
            entity.getLength(),
            entity.getWidth(),
            entity.getglazeQuantityPerUnit(),
            entity.getWeight(),
            entity.getType().getName(),
            entity.getLine().getName(),
            entity.getProductStock()
        );
    }
}
