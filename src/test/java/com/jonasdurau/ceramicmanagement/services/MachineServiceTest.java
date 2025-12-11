package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.batch.machineusage.BatchMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoomRepository;
import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.machine.MachineService;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineRequestDTO;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class MachineServiceTest {
    
    @Mock
    private MachineRepository machineRepository;

    @Mock
    private BatchMachineUsageRepository batchMachineUsageRepository;

    @Mock
    private GlazeMachineUsageRepository glazeMachineUsageRepository;

    @Mock
    private DryingRoomRepository dryingRoomRepository;

    @Mock
    private GlazeService glazeService;

    @InjectMocks
    private MachineService machineService;

    private Machine machine;
    private MachineRequestDTO requestDTO;
    private Long testId;

    @BeforeEach
    void setUp() {
        testId = 1L;

        machine = new Machine();
        machine.setId(testId);
        machine.setCreatedAt(Instant.now());
        machine.setUpdatedAt(Instant.now());
        machine.setName("Test Machine");
        machine.setPower(100.0);

        requestDTO = new MachineRequestDTO("Test Machine", 100.0);
    }

    @Test
    void findAll_WhenThereAreMachines_ShouldReturnListOfMachines() {
        when(machineRepository.findAll()).thenReturn(List.of(machine));

        List<MachineResponseDTO> responseList = machineService.findAll();
        MachineResponseDTO machineDTO = responseList.getFirst();

        assertEquals(responseList.size(), 1);
        assertDTOMatchesEntity(machineDTO, machine);
        verify(machineRepository).findAll();
    }

    @Test
    void findAll_WhenThereAreNotMachines_ShouldReturnEmptyList() {
        when(machineRepository.findAll()).thenReturn(List.of());

        List<MachineResponseDTO> responseDTO = machineService.findAll();

        assertTrue(responseDTO.isEmpty());
        verify(machineRepository).findAll();
    }

    @Test
    void findById_WhenMachineExists_ShouldReturnMachine() {
        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));

        MachineResponseDTO responseDTO = machineService.findById(testId);

        assertDTOMatchesEntity(responseDTO, machine);
        verify(machineRepository).findById(testId);
    }

    @Test
    void findById_WhenMachineDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(machineRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> machineService.findById(testId));

        verify(machineRepository).findById(testId);
    }

    @Test
    void create_WhenNameExists_ShouldThrowBusinessException() {
        when(machineRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> machineService.create(requestDTO));

        verify(machineRepository).existsByName(requestDTO.name());
        verify(machineRepository, never()).save(any());
    }

    @Test
    void create_WhenNameNotExists_ShouldReturnMachine() {
        when(machineRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(machineRepository.save(any(Machine.class))).thenReturn(machine);

        MachineResponseDTO responseDTO = machineService.create(requestDTO);

        assertDTOMatchesEntity(responseDTO, machine);

        verify(machineRepository).existsByName(requestDTO.name());
        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void update_WhenMachineNotExists_ShouldThrowResourceNotFoundException() {
        when(machineRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> machineService.update(testId, requestDTO));

        verify(machineRepository).findById(testId);
        verify(machineRepository, never()).existsByName(anyString());
        verify(machineRepository, never()).save(any());
        verify(glazeService, never()).recalculateGlazesByMachine(testId);
    }

    @Test
    void update_WhenMachineExistsAndNameExists_ShouldThrowBusinessException() {
        MachineRequestDTO dtoWithDifferentName = new MachineRequestDTO("Different Name", 100.0);

        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));
        when(machineRepository.existsByName(dtoWithDifferentName.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> machineService.update(testId, dtoWithDifferentName));

        verify(machineRepository).findById(testId);
        verify(machineRepository).existsByName(dtoWithDifferentName.name());
        verify(machineRepository, never()).save(any());
        verify(glazeService, never()).recalculateGlazesByMachine(testId);
    }

    @Test
    void update_WhenMachineExistsAndNameNotExists_ShouldReturnMachine() {
        MachineRequestDTO dtoWithDifferentName = new MachineRequestDTO("Different Name", 100.0);
        Machine updatedMachine = new Machine();
        updatedMachine.setId(machine.getId());
        updatedMachine.setCreatedAt(machine.getCreatedAt());
        updatedMachine.setUpdatedAt(Instant.now().plusSeconds(10));
        updatedMachine.setName(dtoWithDifferentName.name());
        updatedMachine.setPower(dtoWithDifferentName.power());

        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));
        when(machineRepository.existsByName(dtoWithDifferentName.name())).thenReturn(false);
        when(machineRepository.save(any(Machine.class))).thenReturn(updatedMachine);

        MachineResponseDTO responseDTO = machineService.update(testId, dtoWithDifferentName);

        assertDTOMatchesEntity(responseDTO, updatedMachine);

        verify(machineRepository).findById(testId);
        verify(machineRepository).existsByName(dtoWithDifferentName.name());
        verify(machineRepository).save(any(Machine.class));
        verify(glazeService).recalculateGlazesByMachine(testId);
    }

    @Test
    void delete_WhenMachineNotExists_ShouldThrowResourceNotFoundException() {
        when(machineRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> machineService.delete(testId));

        verify(machineRepository).findById(testId);
        verify(batchMachineUsageRepository, never()).existsByMachineId(testId);
        verify(glazeMachineUsageRepository, never()).existsByMachineId(testId);
        verify(dryingRoomRepository, never()).existsByMachinesId(testId);
        verify(machineRepository, never()).delete(any());
    }

    @Test
    void delete_WhenMachineHasBatchUsages_ShouldThrowResourceDeletionException() {
        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));
        when(batchMachineUsageRepository.existsByMachineId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> machineService.delete(testId));

        verify(machineRepository, never()).delete(any());
    }

    @Test
    void delete_WhenMachineHasGlazeUsages_ShouldThrowResourceDeletionException() {
        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));
        when(glazeMachineUsageRepository.existsByMachineId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> machineService.delete(testId));

        verify(machineRepository, never()).delete(any());
    }

    @Test
    void delete_WhenMachineHasDryingRoomUsages_ShouldThrowResourceDeletionException() {
        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));
        when(dryingRoomRepository.existsByMachinesId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> machineService.delete(testId));

        verify(machineRepository, never()).delete(any());
    }

    @Test
    void delete_WhenMachineHasNoDependencies_ShouldDelete() {
        when(machineRepository.findById(testId)).thenReturn(Optional.of(machine));
        when(batchMachineUsageRepository.existsByMachineId(testId)).thenReturn(false);
        when(glazeMachineUsageRepository.existsByMachineId(testId)).thenReturn(false);
        when(dryingRoomRepository.existsByMachinesId(testId)).thenReturn(false);

        machineService.delete(testId);

        verify(batchMachineUsageRepository).existsByMachineId(testId);
        verify(glazeMachineUsageRepository).existsByMachineId(testId);
        verify(dryingRoomRepository).existsByMachinesId(testId);
        verify(machineRepository).delete(any());
    }

    private void assertDTOMatchesEntity(MachineResponseDTO dto, Machine entity) {
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getCreatedAt(), dto.createdAt());
        assertEquals(entity.getUpdatedAt(), dto.updatedAt());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getPower(), dto.power());
    }
}
