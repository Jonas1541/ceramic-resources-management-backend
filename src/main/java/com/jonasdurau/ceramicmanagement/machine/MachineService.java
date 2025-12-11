package com.jonasdurau.ceramicmanagement.machine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.batch.machineusage.BatchMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoomRepository;
import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineRequestDTO;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentCrudService;

@Service
public class MachineService implements IndependentCrudService<MachineResponseDTO, MachineRequestDTO, MachineResponseDTO, Long>{
    
    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private BatchMachineUsageRepository batchMachineUsageRepository;

    @Autowired
    private GlazeMachineUsageRepository glazeMachineUsageRepository;

    @Autowired
    private DryingRoomRepository dryingRoomRepository;

    @Autowired
    private GlazeService glazeService;

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<MachineResponseDTO> findAll() {
        List<Machine> list = machineRepository.findAll();
        return list.stream().map(this::entityToResponseDTO).toList();
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public MachineResponseDTO findById(Long id) {
        Machine entity = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada. Id: " + id));
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public MachineResponseDTO create(MachineRequestDTO dto) {
        if(machineRepository.existsByName(dto.name())) {
            throw new BusinessException("O nome '" + dto.name() + "' já existe.");
        }
        Machine entity = new Machine();
        entity.setName(dto.name());
        entity.setPower(dto.power());
        entity = machineRepository.save(entity);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public MachineResponseDTO update(Long id, MachineRequestDTO dto) {
        Machine entity = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada. Id: " + id));
        String newName = dto.name();
        String oldName = entity.getName();
        if (!oldName.equals(newName) && machineRepository.existsByName(newName)) {
            throw new BusinessException("O nome '" + newName + "' já existe.");
        }
        entity.setName(newName);
        entity.setPower(dto.power());
        entity = machineRepository.save(entity);
        glazeService.recalculateGlazesByMachine(id);
        return entityToResponseDTO(entity);
    }

    @Override
    @Transactional(transactionManager = "tenantTransactionManager")
    public void delete(Long id) {
        Machine entity = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada. Id: " + id));
        boolean hasBatchUsages = batchMachineUsageRepository.existsByMachineId(id);
        boolean hasGlazeUsages = glazeMachineUsageRepository.existsByMachineId(id);
        boolean hasDryingRooms = dryingRoomRepository.existsByMachinesId(id);
        if (hasBatchUsages) {
            throw new ResourceDeletionException("Não é possível deletar a máquina com id " + id + " pois ela tem bateladas associadas.");
        }
        if (hasGlazeUsages) {
            throw new ResourceDeletionException("Não é possível deletar a máquina com id " + id + " pois ela tem glasuras associadas.");
        }
        if (hasDryingRooms) {
            throw new ResourceDeletionException("Não é possível deletar a máquina com id " + id + "pois ela tem estufas associadas.");
        }
        machineRepository.delete(entity);
    }

    private MachineResponseDTO entityToResponseDTO(Machine entity) {
        return new MachineResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getPower()
        );
    }
}
