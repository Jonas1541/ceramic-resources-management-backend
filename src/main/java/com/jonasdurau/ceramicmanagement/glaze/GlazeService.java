package com.jonasdurau.ceramicmanagement.glaze;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeListDTO;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeResponseDTO;
import com.jonasdurau.ceramicmanagement.glaze.employeeusage.GlazeEmployeeUsage;
import com.jonasdurau.ceramicmanagement.glaze.employeeusage.GlazeEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsage;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.dto.GlazeMachineUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.dto.GlazeMachineUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsage;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.dto.GlazeResourceUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.dto.GlazeResourceUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransaction;
import com.jonasdurau.ceramicmanagement.glaze.validation.GlazeDeletionValidator;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.MonthReportDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class GlazeService implements IndependentCrudService<GlazeListDTO, GlazeRequestDTO, GlazeResponseDTO, Long> {

    private final GlazeRepository glazeRepository;
    private final ResourceRepository resourceRepository;
    private final MachineRepository machineRepository;
    private final EmployeeRepository employeeRepository;
    private final GlazeResourceUsageRepository glazeResourceUsageRepository;
    private final GlazeMachineUsageRepository glazeMachineUsageRepository;
    private final GlazeEmployeeUsageRepository glazeEmployeeUsageRepository;
    private final List<GlazeDeletionValidator> deletionValidators;

    @Autowired
    public GlazeService(GlazeRepository glazeRepository, ResourceRepository resourceRepository,
            MachineRepository machineRepository, EmployeeRepository employeeRepository,
            GlazeResourceUsageRepository glazeResourceUsageRepository,
            GlazeMachineUsageRepository glazeMachineUsageRepository,
            GlazeEmployeeUsageRepository glazeEmployeeUsageRepository,
            List<GlazeDeletionValidator> deletionValidators) {
        this.glazeRepository = glazeRepository;
        this.resourceRepository = resourceRepository;
        this.machineRepository = machineRepository;
        this.employeeRepository = employeeRepository;
        this.glazeResourceUsageRepository = glazeResourceUsageRepository;
        this.glazeMachineUsageRepository = glazeMachineUsageRepository;
        this.glazeEmployeeUsageRepository = glazeEmployeeUsageRepository;
        this.deletionValidators = deletionValidators;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<GlazeListDTO> findAll() {
        List<Glaze> entities = glazeRepository.findAll();
        return entities.stream()
            .map(glaze -> new GlazeListDTO(
                glaze.getId(),
                glaze.getCreatedAt(),
                glaze.getUpdatedAt(),
                glaze.getColor(),
                glaze.getUnitCost(),
                glaze.getCurrentQuantity(),
                glaze.getCurrentQuantityPrice()
            ))
            .toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public GlazeResponseDTO findById(Long id) {
        Glaze glaze = glazeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze not found: " + id));
        return entityToResponseDTO(glaze);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeResponseDTO create(GlazeRequestDTO dto) {
        Glaze glaze = new Glaze();
        glaze.setColor(dto.color());

        // Resource Usages
        for (GlazeResourceUsageRequestDTO usageDTO : dto.resourceUsages()) {
            Resource resource = resourceRepository.findById(usageDTO.resourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + usageDTO.resourceId()));
            GlazeResourceUsage usage = new GlazeResourceUsage();
            usage.setGlaze(glaze);
            usage.setResource(resource);
            usage.setQuantity(usageDTO.quantity());
            glaze.getResourceUsages().add(usage);
        }
        
        // Machine Usages
        for (GlazeMachineUsageRequestDTO muDTO : dto.machineUsages()) {
            Machine machine = machineRepository.findById(muDTO.machineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + muDTO.machineId()));
            GlazeMachineUsage mu = new GlazeMachineUsage();
            mu.setGlaze(glaze);
            mu.setMachine(machine);
            mu.setUsageTime(muDTO.usageTime());
            glaze.getMachineUsages().add(mu);
        }

        // Employee Usages
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            Employee employee = employeeRepository.findById(euDTO.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + euDTO.employeeId()));
            GlazeEmployeeUsage eu = new GlazeEmployeeUsage();
            eu.setGlaze(glaze);
            eu.setEmployee(employee);
            eu.setUsageTime(euDTO.usageTime());
            glaze.getEmployeeUsages().add(eu);
        }

        computeUnitCost(glaze);
        glaze = glazeRepository.save(glaze);
        return entityToResponseDTO(glaze);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public GlazeResponseDTO update(Long id, GlazeRequestDTO dto) {
        Glaze glaze = glazeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Glaze not found: " + id));
        
        // Update Resource Usages
        Map<Long, GlazeResourceUsage> existingResourceUsages = glaze.getResourceUsages().stream()
            .collect(Collectors.toMap(r -> r.getResource().getId(), r -> r));
        for (GlazeResourceUsageRequestDTO usageDTO : dto.resourceUsages()) {
            GlazeResourceUsage existingUsage = existingResourceUsages.get(usageDTO.resourceId());
            if (existingUsage != null) {
                existingUsage.setQuantity(usageDTO.quantity());
                existingResourceUsages.remove(usageDTO.resourceId());
            } else {
                Resource resource = resourceRepository.findById(usageDTO.resourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + usageDTO.resourceId()));
                GlazeResourceUsage newUsage = new GlazeResourceUsage();
                newUsage.setGlaze(glaze);
                newUsage.setResource(resource);
                newUsage.setQuantity(usageDTO.quantity());
                glaze.getResourceUsages().add(newUsage);
            }
        }
        for (GlazeResourceUsage removedUsage : existingResourceUsages.values()) {
            glaze.getResourceUsages().remove(removedUsage);
        }
        
        // Update Machine Usages
        Map<Long, GlazeMachineUsage> existingMachineUsages = glaze.getMachineUsages().stream()
            .collect(Collectors.toMap(mu -> mu.getMachine().getId(), mu -> mu));
        Set<Long> updatedMachineIds = new HashSet<>();
        for (GlazeMachineUsageRequestDTO muDTO : dto.machineUsages()) {
            GlazeMachineUsage existingMu = existingMachineUsages.get(muDTO.machineId());
            if (existingMu != null) {
                existingMu.setUsageTime(muDTO.usageTime());
                updatedMachineIds.add(muDTO.machineId());
            } else {
                Machine machine = machineRepository.findById(muDTO.machineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + muDTO.machineId()));
                GlazeMachineUsage newMu = new GlazeMachineUsage();
                newMu.setGlaze(glaze);
                newMu.setMachine(machine);
                newMu.setUsageTime(muDTO.usageTime());
                glaze.getMachineUsages().add(newMu);
                updatedMachineIds.add(muDTO.machineId());
            }
        }
        List<GlazeMachineUsage> muToRemove = glaze.getMachineUsages().stream()
            .filter(mu -> !updatedMachineIds.contains(mu.getMachine().getId()))
            .collect(Collectors.toList());
        for (GlazeMachineUsage mu : muToRemove) {
            glaze.getMachineUsages().remove(mu);
        }
        
        // Update Employee Usages
        Map<Long, GlazeEmployeeUsage> existingEmployeeUsages = glaze.getEmployeeUsages().stream()
            .collect(Collectors.toMap(eu -> eu.getEmployee().getId(), eu -> eu));
        Set<Long> updatedEmployeeIds = new HashSet<>();
        for (EmployeeUsageRequestDTO euDTO : dto.employeeUsages()) {
            GlazeEmployeeUsage existingEu = existingEmployeeUsages.get(euDTO.employeeId());
            if (existingEu != null) {
                existingEu.setUsageTime(euDTO.usageTime());
                updatedEmployeeIds.add(euDTO.employeeId());
            } else {
                Employee employee = employeeRepository.findById(euDTO.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + euDTO.employeeId()));
                GlazeEmployeeUsage newEu = new GlazeEmployeeUsage();
                newEu.setGlaze(glaze);
                newEu.setEmployee(employee);
                newEu.setUsageTime(euDTO.usageTime());
                glaze.getEmployeeUsages().add(newEu);
                updatedEmployeeIds.add(euDTO.employeeId());
            }
        }
        List<GlazeEmployeeUsage> euToRemove = glaze.getEmployeeUsages().stream()
            .filter(eu -> !updatedEmployeeIds.contains(eu.getEmployee().getId()))
            .collect(Collectors.toList());
        for (GlazeEmployeeUsage eu : euToRemove) {
            glaze.getEmployeeUsages().remove(eu);
        }

        glaze.setColor(dto.color());
        computeUnitCost(glaze);
        glaze = glazeRepository.save(glaze);
        return entityToResponseDTO(glaze);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        Glaze glaze = glazeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Glasura não encontrada: " + id));
        deletionValidators.forEach(validator -> validator.validate(id));
        glazeRepository.delete(glaze);
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<YearReportDTO> yearlyReport(Long glazeId) {
        Glaze glaze = glazeRepository.findById(glazeId)
                .orElseThrow(() -> new ResourceNotFoundException("Glasura não encontrada: " + glazeId));
        List<GlazeTransaction> txs = glaze.getTransactions();
        ZoneId zone = ZoneId.systemDefault();
        Map<Integer, Map<Month, List<GlazeTransaction>>> mapYearMonth = txs.stream()
                .map(t -> new AbstractMap.SimpleEntry<>(t, t.getCreatedAt().atZone(zone)))
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getYear(),
                        Collectors.groupingBy(
                                entry -> entry.getValue().getMonth(),
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList()))));
        List<YearReportDTO> yearReports = new ArrayList<>();
        for (Map.Entry<Integer, Map<Month, List<GlazeTransaction>>> yearEntry : mapYearMonth.entrySet()) {
            int year = yearEntry.getKey();
            Map<Month, List<GlazeTransaction>> mapMonth = yearEntry.getValue();
            YearReportDTO yearReport = new YearReportDTO(year);
            double totalIncomingQtyYear = 0.0;
            double totalOutgoingQtyYear = 0.0;
            BigDecimal totalIncomingCostYear = BigDecimal.ZERO;
            BigDecimal totalOutgoingProfitYear = BigDecimal.ZERO;
            for (Month m : Month.values()) {
                List<GlazeTransaction> monthTx = mapMonth.getOrDefault(m, Collections.emptyList());
                double incomingQty = 0.0;
                double outgoingQty = 0.0;
                BigDecimal incomingCost = BigDecimal.ZERO;
                BigDecimal outgoingProfit = BigDecimal.ZERO;
                for (GlazeTransaction t : monthTx) {
                    if (t.getType() == TransactionType.INCOMING) {
                        incomingQty += t.getQuantity();
                        incomingCost = incomingCost.add(t.getGlazeFinalCostAtTime());
                    } else {
                        outgoingQty += t.getQuantity();
                    }
                }
                totalIncomingQtyYear += incomingQty;
                totalOutgoingQtyYear += outgoingQty;
                totalIncomingCostYear = totalIncomingCostYear.add(incomingCost);
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

    private GlazeResponseDTO entityToResponseDTO(Glaze entity) {
        List<GlazeResourceUsageResponseDTO> resourceUsageDTOs = entity.getResourceUsages().stream()
            .map(usage -> new GlazeResourceUsageResponseDTO(
                usage.getResource().getId(),
                usage.getResource().getName(),
                usage.getQuantity()
            ))
            .collect(Collectors.toList());
            
        List<GlazeMachineUsageResponseDTO> machineUsageDTOs = entity.getMachineUsages().stream()
            .map(mu -> new GlazeMachineUsageResponseDTO(
                mu.getMachine().getId(),
                mu.getMachine().getName(),
                mu.getUsageTime()
            ))
            .collect(Collectors.toList());

        List<EmployeeUsageResponseDTO> employeeUsageDTOs = entity.getEmployeeUsages().stream()
            .map(eu -> new EmployeeUsageResponseDTO(
                eu.getEmployee().getId(),
                eu.getEmployee().getName(),
                eu.getUsageTime(),
                eu.getCost()
            ))
            .collect(Collectors.toList());
            
        return new GlazeResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getColor(),
            resourceUsageDTOs,
            machineUsageDTOs,
            employeeUsageDTOs,
            entity.getTotalEmployeeCost(),
            entity.getUnitCost(),
            entity.getCurrentQuantity(),
            entity.getCurrentQuantityPrice()
        );
    }

    private void computeUnitCost(Glaze glaze) {
        BigDecimal resourceCost = BigDecimal.ZERO;
        for (GlazeResourceUsage usage : glaze.getResourceUsages()) {
            BigDecimal resourceUnitValue = usage.getResource().getUnitValue(); 
            BigDecimal subCost = resourceUnitValue.multiply(BigDecimal.valueOf(usage.getQuantity()));
            resourceCost = resourceCost.add(subCost);
        }
        BigDecimal machineCost = computeMachineCost(glaze);
        BigDecimal employeeCost = computeEmployeeCost(glaze);
        
        BigDecimal finalCost = resourceCost
            .add(machineCost)
            .add(employeeCost)
            .setScale(2, RoundingMode.HALF_UP);
            
        glaze.setUnitCost(finalCost);
    }

    private BigDecimal computeMachineCost(Glaze glaze) {
        Resource electricity = resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)
            .orElseThrow(() -> new BusinessException("ELECTRICITY resource not found"));
        BigDecimal total = BigDecimal.ZERO;
        for (GlazeMachineUsage mu : glaze.getMachineUsages()) {
            double kwh = mu.getEnergyConsumption();
            BigDecimal cost = electricity.getUnitValue().multiply(BigDecimal.valueOf(kwh));
            total = total.add(cost);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeEmployeeCost(Glaze glaze) {
        return glaze.getEmployeeUsages().stream()
            .map(usage -> {
                BigDecimal costPerHour = usage.getEmployee().getCostPerHour();
                return costPerHour.multiply(BigDecimal.valueOf(usage.getUsageTime()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void recalculateGlazesByResource(Long resourceId) {
        List<GlazeResourceUsage> usages = glazeResourceUsageRepository.findByResourceId(resourceId);
        List<Glaze> glazes = usages.stream()
            .map(GlazeResourceUsage::getGlaze)
            .distinct()
            .collect(Collectors.toList());
        for (Glaze glaze : glazes) {
            computeUnitCost(glaze);
            glazeRepository.save(glaze);
        }
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void recalculateGlazesByMachine(Long machineId) {
        List<GlazeMachineUsage> usages = glazeMachineUsageRepository.findByMachineId(machineId);
        List<Glaze> glazes = usages.stream()
                .map(GlazeMachineUsage::getGlaze)
                .distinct()
                .collect(Collectors.toList());
        for (Glaze g : glazes) {
            computeUnitCost(g);
            glazeRepository.save(g);
        }
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void recalculateGlazesByEmployee(Long employeeId) {
        List<GlazeEmployeeUsage> usages = glazeEmployeeUsageRepository.findByEmployeeId(employeeId);
        List<Glaze> glazes = usages.stream()
                .map(GlazeEmployeeUsage::getGlaze)
                .distinct()
                .collect(Collectors.toList());
        for (Glaze glaze : glazes) {
            computeUnitCost(glaze);
            glazeRepository.save(glaze);
        }
    }
}