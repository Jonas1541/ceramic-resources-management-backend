package com.jonasdurau.ceramicmanagement.resource;

import com.jonasdurau.ceramicmanagement.batch.resourceusage.BatchResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceListDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceRequestDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceResponseDTO;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransactionRepository;
import com.jonasdurau.ceramicmanagement.shared.dto.MonthReportDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceService  implements IndependentCrudService<ResourceListDTO, ResourceRequestDTO, ResourceResponseDTO, Long>{

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceTransactionRepository transactionRepository;

    @Autowired
    private BatchResourceUsageRepository batchResourceUsageRepository;

    @Autowired
    private GlazeResourceUsageRepository glazeResourceUsageRepository;

    @Autowired
    private GlazeService glazeService;

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<ResourceListDTO> findAll() {
        List<Resource> list = resourceRepository.findAll();
        return list.stream()
                .map(r -> {
                    double currentQty = r.getCurrentQuantity();
                    BigDecimal currentPrice = r.getCurrentQuantityPrice();

                    return new ResourceListDTO(
                            r.getId(),
                            r.getName(),
                            r.getCategory().name(),
                            r.getUnitValue(),
                            currentQty,
                            currentPrice);
                })
                .toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public ResourceResponseDTO findById(Long id) {
        Resource entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ResourceResponseDTO create(ResourceRequestDTO dto) {
        if (resourceRepository.existsByName(dto.name())) {
            throw new BusinessException("O nome '" + dto.name() + "' já existe.");
        }
        if (isUniqueCategory(dto.category()) && resourceRepository.existsByCategory(dto.category())) {
            throw new BusinessException("Já existe um recurso com a categoria " + dto.category());
        }
        Resource entity = new Resource();
        entity.setName(dto.name());
        entity.setCategory(dto.category());
        entity.setUnitValue(dto.unitValue());
        entity = resourceRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public ResourceResponseDTO update(Long id, ResourceRequestDTO dto) {
        Resource entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado. Id: " + id));
        if (isUniqueCategory(dto.category()) && resourceRepository.existsByCategoryAndIdNot(dto.category(), id)) {
            throw new BusinessException("Já existe outro recurso com a categoria " + dto.category());
        }
        String newName = dto.name();
        String oldName = entity.getName();
        if (!oldName.equals(newName) && resourceRepository.existsByName(newName)) {
            throw new BusinessException("O nome '" + newName + "' já existe.");
        }
        entity.setName(newName);
        entity.setCategory(dto.category());
        entity.setUnitValue(dto.unitValue());
        entity = resourceRepository.save(entity);
        glazeService.recalculateGlazesByResource(id);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        Resource entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado. Id: " + id));
        boolean hasTransactions = transactionRepository.existsByResourceId(id);
        boolean hasBatchUsages = batchResourceUsageRepository.existsByResourceId(id);
        boolean hasGlazeUsages = glazeResourceUsageRepository.existsByResourceId(id);
        if (hasTransactions) {
            throw new ResourceDeletionException("Não é possível deletar o recurso com id " + id + " pois ele tem transações associadas.");
        }
        if (hasBatchUsages) {
            throw new ResourceDeletionException("Não é possível deletar o recurso com id " + id + " pois ele tem bateladas associadas.");
        }
        if (hasGlazeUsages) {
            throw new ResourceDeletionException("Não é possível deletar o recurso com id " + id + " pois ele tem glasuras associadas.");
        }
        resourceRepository.delete(entity);
    }

    private ResourceResponseDTO entityToResponseDTO(Resource entity) {
        return new ResourceResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getCategory(),
            entity.getUnitValue(),
            entity.getCurrentQuantity(),
            entity.getCurrentQuantityPrice()
        );
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public List<YearReportDTO> yearlyReport(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));
        List<ResourceTransaction> txs = resource.getTransactions();
        ZoneId zone = ZoneId.systemDefault();
        Map<Integer, Map<Month, List<ResourceTransaction>>> mapYearMonth = txs.stream()
                .map(t -> new AbstractMap.SimpleEntry<>(t, t.getCreatedAt().atZone(zone)))
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getYear(),
                        Collectors.groupingBy(
                                entry -> entry.getValue().getMonth(),
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList()))));
        List<YearReportDTO> yearReports = new ArrayList<>();
        for (Map.Entry<Integer, Map<Month, List<ResourceTransaction>>> yearEntry : mapYearMonth.entrySet()) {
            int year = yearEntry.getKey();
            Map<Month, List<ResourceTransaction>> mapMonth = yearEntry.getValue();
            YearReportDTO yearReport = new YearReportDTO(year);
            double totalIncomingQtyYear = 0.0;
            BigDecimal totalIncomingCostYear = BigDecimal.ZERO;
            double totalOutgoingQtyYear = 0.0;
            BigDecimal totalOutgoingProfitYear = BigDecimal.ZERO;
            for (Month m : Month.values()) {
                List<ResourceTransaction> monthTx = mapMonth.getOrDefault(m, Collections.emptyList());
                double incomingQty = 0.0;
                BigDecimal incomingCost = BigDecimal.ZERO;
                double outgoingQty = 0.0;
                BigDecimal outgoingProfit = BigDecimal.ZERO;
                for (ResourceTransaction t : monthTx) {
                    if (t.getType() == TransactionType.INCOMING) {
                        incomingQty += t.getQuantity();
                        BigDecimal cost = resource.getUnitValue().multiply(BigDecimal.valueOf(t.getQuantity()));
                        incomingCost = incomingCost.add(cost);
                    } else {
                        outgoingQty += t.getQuantity();
                    }
                }
                totalIncomingQtyYear += incomingQty;
                totalIncomingCostYear = totalIncomingCostYear.add(incomingCost);
                totalOutgoingQtyYear += outgoingQty;
                MonthReportDTO monthDto = new MonthReportDTO();
                monthDto.setMonthName(m.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                monthDto.setIncomingQty(incomingQty);
                monthDto.setIncomingCost(incomingCost);
                monthDto.setOutgoingQty(outgoingQty);
                monthDto.setOutgoingProfit(outgoingProfit);
                yearReport.getMonths().add(monthDto);
            }
            yearReport.setTotalIncomingQty(totalIncomingQtyYear);
            yearReport.setTotalIncomingCost(totalIncomingCostYear);
            yearReport.setTotalOutgoingQty(totalOutgoingQtyYear);
            yearReport.setTotalOutgoingProfit(totalOutgoingProfitYear);
            yearReports.add(yearReport);
        }
        yearReports.sort((a, b) -> b.getYear() - a.getYear());
        return yearReports;
    }

    private boolean isUniqueCategory(ResourceCategory category) {
        return category == ResourceCategory.ELECTRICITY || category == ResourceCategory.WATER || category == ResourceCategory.GAS;
    }
}
