package com.jonasdurau.ceramicmanagement.kiln;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiringRepository;
import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiringRepository;
import com.jonasdurau.ceramicmanagement.kiln.dto.KilnRequestDTO;
import com.jonasdurau.ceramicmanagement.kiln.dto.KilnResponseDTO;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.MonthReportDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class KilnService implements IndependentCrudService<KilnResponseDTO, KilnRequestDTO, KilnResponseDTO, Long> {

    @Autowired
    private KilnRepository kilnRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private BisqueFiringRepository bisqueFiringRepository;

    @Autowired
    private GlazeFiringRepository glazeFiringRepository;

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<KilnResponseDTO> findAll() {
        List<Kiln> list = kilnRepository.findAll();
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public KilnResponseDTO findById(Long id) {
        Kiln entity = kilnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forno não encontrado. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public KilnResponseDTO create(KilnRequestDTO dto) {
        Kiln entity = new Kiln();
        entity.setName(dto.name());
        entity.setGasConsumptionPerHour(dto.gasConsumptionPerHour());
        for (Long machineId : dto.machines()) {
            Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada. Id: " + machineId));
            entity.getMachines().add(machine);
        }
        entity = kilnRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public KilnResponseDTO update(Long id, KilnRequestDTO dto) {
        Kiln entity = kilnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forno não encontrado. id: " + id));
        entity.setName(dto.name());
        entity.setGasConsumptionPerHour(dto.gasConsumptionPerHour());
        List<Machine> oldList = new ArrayList<>(entity.getMachines());
        List<Machine> newList = dto.machines().stream().map(machineId -> {
            Machine machine = machineRepository.findById(machineId)
                    .orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada. Id: " + machineId));
            return machine;
        }).collect(Collectors.toList());
        Set<Long> oldIds = oldList.stream().map(Machine::getId).collect(Collectors.toSet());
        Set<Long> newIds = newList.stream().map(Machine::getId).collect(Collectors.toSet());
        List<Machine> toRemove = oldList.stream().filter(machine -> !newIds.contains(machine.getId())).collect(Collectors.toList());
        List<Machine> toAdd = newList.stream().filter(machine -> !oldIds.contains(machine.getId())).collect(Collectors.toList());
        entity.getMachines().removeAll(toRemove);
        entity.getMachines().addAll(toAdd);
        entity = kilnRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        Kiln entity = kilnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forno não encontrado. Id: " + id));
        boolean hasBisqueFiring = bisqueFiringRepository.existsByKilnId(id);
        boolean hasGlazeFiring = glazeFiringRepository.existsByKilnId(id);
        if (hasBisqueFiring || hasGlazeFiring) {
            throw new ResourceDeletionException("O forno não pode ser deletado pois possui queimas associadas.");
        }
        kilnRepository.delete(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<YearReportDTO> yearlyReport(Long kilnId) {
        Kiln kiln = kilnRepository.findById(kilnId)
                .orElseThrow(() -> new ResourceNotFoundException("Kiln not found: " + kilnId));
        ZoneId zone = ZoneId.systemDefault();
        Stream<AbstractMap.SimpleEntry<Instant, BigDecimal>> bisqueStream = kiln.getBisqueFirings().stream()
                .map(bisque -> new AbstractMap.SimpleEntry<>(bisque.getCreatedAt(), bisque.getCostAtTime()));
        Stream<AbstractMap.SimpleEntry<Instant, BigDecimal>> glazeStream = kiln.getGlazeFirings().stream()
                .map(glaze -> new AbstractMap.SimpleEntry<>(glaze.getCreatedAt(), glaze.getCostAtTime()));
        Stream<AbstractMap.SimpleEntry<Instant, BigDecimal>> combinedStream = Stream.concat(bisqueStream, glazeStream);
        Map<Integer, Map<Month, List<AbstractMap.SimpleEntry<Instant, BigDecimal>>>> mapYearMonth = combinedStream
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().atZone(zone).getYear(),
                        Collectors.groupingBy(
                                entry -> entry.getKey().atZone(zone).getMonth())));
        List<YearReportDTO> yearReports = new ArrayList<>();
        for (Map.Entry<Integer, Map<Month, List<AbstractMap.SimpleEntry<Instant, BigDecimal>>>> yearEntry : mapYearMonth
                .entrySet()) {
            int year = yearEntry.getKey();
            Map<Month, List<AbstractMap.SimpleEntry<Instant, BigDecimal>>> monthMap = yearEntry.getValue();
            YearReportDTO yearReport = new YearReportDTO(year);
            double totalIncomingQtyYear = 0.0;
            BigDecimal totalIncomingCostYear = BigDecimal.ZERO;
            for (Month m : Month.values()) {
                List<AbstractMap.SimpleEntry<Instant, BigDecimal>> monthEntries = monthMap.getOrDefault(m,
                        Collections.emptyList());
                double incomingQty = monthEntries.size();
                BigDecimal incomingCost = monthEntries.stream()
                        .map(entry -> entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalIncomingQtyYear += incomingQty;
                totalIncomingCostYear = totalIncomingCostYear.add(incomingCost);
                MonthReportDTO monthDto = new MonthReportDTO();
                monthDto.setMonthName(m.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                monthDto.setIncomingQty(incomingQty);
                monthDto.setIncomingCost(incomingCost);
                monthDto.setOutgoingQty(0.0);
                monthDto.setOutgoingProfit(BigDecimal.ZERO);
                yearReport.getMonths().add(monthDto);
            }
            yearReport.setTotalIncomingQty(totalIncomingQtyYear);
            yearReport.setTotalIncomingCost(totalIncomingCostYear);
            yearReport.setTotalOutgoingQty(0.0);
            yearReport.setTotalOutgoingProfit(BigDecimal.ZERO);
            yearReports.add(yearReport);
        }
        yearReports.sort((a, b) -> b.getYear() - a.getYear());
        return yearReports;
    }

    private KilnResponseDTO entityToResponseDTO(Kiln entity) {
        List<MachineResponseDTO> machineDTOs = new ArrayList<>();
        for (Machine machine : entity.getMachines()) {
            MachineResponseDTO machineDTO = new MachineResponseDTO(
                    machine.getId(),
                    machine.getCreatedAt(),
                    machine.getUpdatedAt(),
                    machine.getName(),
                    machine.getPower());
            machineDTOs.add(machineDTO);
        }
        return new KilnResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getPower(),
            entity.getGasConsumptionPerHour(),
            machineDTOs
        );
    }
}
