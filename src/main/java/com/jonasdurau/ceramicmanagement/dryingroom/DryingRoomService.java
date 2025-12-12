package com.jonasdurau.ceramicmanagement.dryingroom;

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
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSession;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomListDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomRequestDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomResponseDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.validation.DryingRoomDeletionValidator;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.MonthReportDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class DryingRoomService implements IndependentCrudService<DryingRoomListDTO, DryingRoomRequestDTO, DryingRoomResponseDTO, Long> {

    private final DryingRoomRepository dryingRoomRepository;
    private final MachineRepository machineRepository;
    private final List<DryingRoomDeletionValidator> deletionValidators;

    @Autowired
    public DryingRoomService(DryingRoomRepository dryingRoomRepository, MachineRepository machineRepository,
            List<DryingRoomDeletionValidator> deletionValidators) {
        this.dryingRoomRepository = dryingRoomRepository;
        this.machineRepository = machineRepository;
        this.deletionValidators = deletionValidators;
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<DryingRoomListDTO> findAll() {
        List<DryingRoom> list = dryingRoomRepository.findAll();
        return list.stream().map(
            room -> new DryingRoomListDTO(
                room.getId(),
                room.getCreatedAt(),
                room.getUpdatedAt(),
                room.getName(),
                room.getGasConsumptionPerHour()
            ))
            .toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public DryingRoomResponseDTO findById(Long id) {
        DryingRoom entity = dryingRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estufa não encontrada. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public DryingRoomResponseDTO create(DryingRoomRequestDTO dto) {
        DryingRoom entity = new DryingRoom();
        if(dryingRoomRepository.existsByName(dto.name())) {
            throw new BusinessException("Esse nome já existe.");
        }
        entity.setName(dto.name());
        entity.setGasConsumptionPerHour(dto.gasConsumptionPerHour());
        for(Long id : dto.machines()) {
            Machine machine = machineRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada. Id: " + id));
            entity.getMachines().add(machine);
        }
        entity = dryingRoomRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public DryingRoomResponseDTO update(Long id, DryingRoomRequestDTO dto) {
        DryingRoom entity = dryingRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estufa não encontrada. Id: " + id));
        String oldName = entity.getName();
        String newName = dto.name();
        if(!oldName.equals(newName) && dryingRoomRepository.existsByName(newName)) {
            throw new BusinessException("Esse nome já existe.");
        }
        entity.setName(newName);
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
        entity = dryingRoomRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        DryingRoom entity = dryingRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estufa não encontrada. Id: " + id));
        deletionValidators.forEach(validator -> validator.validate(id));
        dryingRoomRepository.delete(entity);
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<YearReportDTO> yearlyReport(Long dryingRoomId) {
        DryingRoom dryingRoom = dryingRoomRepository.findById(dryingRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("DryingRoom not found: " + dryingRoomId));
        List<DryingSession> sessions = dryingRoom.getSessions();
        ZoneId zone = ZoneId.systemDefault();
        Map<Integer, Map<Month, List<DryingSession>>> mapYearMonth = sessions.stream()
                .map(s -> new AbstractMap.SimpleEntry<>(s, s.getCreatedAt().atZone(zone)))
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getYear(),
                        Collectors.groupingBy(
                                entry -> entry.getValue().getMonth(),
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList()))));
        List<YearReportDTO> yearReports = new ArrayList<>();
        for (Map.Entry<Integer, Map<Month, List<DryingSession>>> yearEntry : mapYearMonth.entrySet()) {
            int year = yearEntry.getKey();
            Map<Month, List<DryingSession>> monthMap = yearEntry.getValue();
            YearReportDTO yearReport = new YearReportDTO(year);
            double totalIncomingQtyYear = 0.0;
            BigDecimal totalIncomingCostYear = BigDecimal.ZERO;
            for (Month m : Month.values()) {
                List<DryingSession> monthSessions = monthMap.getOrDefault(m, Collections.emptyList());
                double incomingQty = monthSessions.size();
                BigDecimal incomingCost = monthSessions.stream()
                        .map(session -> session.getCostAtTime() != null ? session.getCostAtTime() : BigDecimal.ZERO)
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

    private DryingRoomResponseDTO entityToResponseDTO(DryingRoom entity) {
        List<MachineResponseDTO> machineDTOs = new ArrayList<>();
        for(Machine machine : entity.getMachines()) {
            MachineResponseDTO machineDTO = new MachineResponseDTO(
                machine.getId(),
                machine.getCreatedAt(),
                machine.getUpdatedAt(),
                machine.getName(),
                machine.getPower()
            );
            machineDTOs.add(machineDTO);
        }
        return new DryingRoomResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getGasConsumptionPerHour(),
            machineDTOs
        );
    }
}
