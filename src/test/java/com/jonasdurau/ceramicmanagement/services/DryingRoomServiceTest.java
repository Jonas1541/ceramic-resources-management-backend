package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoom;
import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoomRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoomService;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSession;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSessionRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomListDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomRequestDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomResponseDTO;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.BusinessException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DryingRoomServiceTest {

    @Mock
    private DryingRoomRepository dryingRoomRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private DryingSessionRepository dryingSessionRepository;

    private DryingRoomService dryingRoomService;

    private DryingRoom dryingRoom;
    private DryingRoomRequestDTO requestDTO;
    private Long testId;
    private Machine machine;

    @BeforeEach
    void setUp() {
        testId = 1L;
        
        this.dryingRoomService = new DryingRoomService(
            dryingRoomRepository,
            machineRepository,
            dryingSessionRepository
        );

        machine = new Machine();
        machine.setId(1L);
        machine.setName("Máquina 1");
        machine.setPower(1500.0);

        dryingRoom = new DryingRoom();
        dryingRoom.setId(testId);
        dryingRoom.setName("Estufa A");
        dryingRoom.setGasConsumptionPerHour(2.5);
        dryingRoom.setCreatedAt(Instant.now());
        dryingRoom.setUpdatedAt(Instant.now());
        dryingRoom.getMachines().add(machine);

        requestDTO = new DryingRoomRequestDTO(
            "Estufa A",
            3.0,
            List.of(1L)
        );
    }

    @Test
    void findAll_ShouldReturnListOfDryingRooms() {
        when(dryingRoomRepository.findAll()).thenReturn(List.of(dryingRoom));

        List<DryingRoomListDTO> result = dryingRoomService.findAll();

        assertEquals(1, result.size());
        assertEquals(testId, result.getFirst().id());
        verify(dryingRoomRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnDryingRoom() {
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));

        DryingRoomResponseDTO result = dryingRoomService.findById(testId);

        assertEquals(testId, result.id());
        verify(dryingRoomRepository).findById(testId);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dryingRoomService.findById(testId));
        verify(dryingRoomRepository).findById(testId);
    }

    @Test
    void create_WithValidData_ShouldReturnDryingRoom() {
        when(dryingRoomRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(dryingRoomRepository.save(any())).thenReturn(dryingRoom);

        DryingRoomResponseDTO result = dryingRoomService.create(requestDTO);

        assertNotNull(result);
        assertEquals("Estufa A", result.name());
        verify(dryingRoomRepository).save(any());
    }

    @Test
    void create_WithDuplicateName_ShouldThrowException() {
        when(dryingRoomRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThrows(BusinessException.class, () -> dryingRoomService.create(requestDTO));
        verify(dryingRoomRepository, never()).save(any());
    }

    @Test
    void create_WithInvalidMachine_ShouldThrowException() {
        when(dryingRoomRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(machineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dryingRoomService.create(requestDTO));
        verify(dryingRoomRepository, never()).save(any());
    }

    @Test
    void update_WithValidData_ShouldUpdateDryingRoom() {
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(dryingRoomRepository.save(any())).thenReturn(dryingRoom);

        DryingRoomResponseDTO result = dryingRoomService.update(testId, requestDTO);

        assertEquals(testId, result.id());
        verify(dryingRoomRepository).save(any());
    }

    @Test
    void update_WithDuplicateName_ShouldThrowException() {

        DryingRoomRequestDTO requestDTO = new DryingRoomRequestDTO(
            "Estufa B",
            3.0,
            List.of(1L)
        );
    
        DryingRoom existing = new DryingRoom();
        existing.setId(2L);
        existing.setName("Estufa B");

        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));
        when(dryingRoomRepository.existsByName("Estufa B")).thenReturn(true);
    
        assertThrows(BusinessException.class, () -> dryingRoomService.update(testId, requestDTO));
        verify(dryingRoomRepository, never()).save(any());
    }

    @Test
    void delete_WhenNoSessions_ShouldDeleteDryingRoom() {
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));
        when(dryingSessionRepository.existsByDryingRoomId(testId)).thenReturn(false);

        dryingRoomService.delete(testId);

        verify(dryingRoomRepository).delete(dryingRoom);
    }

    @Test
    void delete_WhenHasSessions_ShouldThrowException() {
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));
        when(dryingSessionRepository.existsByDryingRoomId(testId)).thenReturn(true);

        assertThrows(ResourceDeletionException.class, () -> dryingRoomService.delete(testId));
        verify(dryingRoomRepository, never()).delete(any());
    }

    @Test
    void yearlyReport_WithSessions_ShouldGenerateReport() {
        DryingSession session = new DryingSession();
        session.setCreatedAt(Instant.now());
        session.setCostAtTime(new BigDecimal("100.00"));
        
        dryingRoom.getSessions().add(session);
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));

        List<YearReportDTO> reports = dryingRoomService.yearlyReport(testId);

        assertFalse(reports.isEmpty());
        verify(dryingRoomRepository).findById(testId);
    }

    @Test
    void yearlyReport_WithNoSessions_ShouldReturnEmpty() {
        when(dryingRoomRepository.findById(testId)).thenReturn(Optional.of(dryingRoom));

        List<YearReportDTO> reports = dryingRoomService.yearlyReport(testId);

        assertTrue(reports.isEmpty());
    }
}