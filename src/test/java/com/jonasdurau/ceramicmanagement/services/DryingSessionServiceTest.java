package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.jonasdurau.ceramicmanagement.machine.Machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoom;
import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoomRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSession;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSessionRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSessionService;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.dto.DryingSessionRequestDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.dto.DryingSessionResponseDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.employeeusage.DryingSessionEmployeeUsage;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DryingSessionServiceTest {

    @Mock
    private DryingSessionRepository sessionRepository;

    @Mock
    private DryingRoomRepository roomRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private DryingSessionService dryingSessionService;

    private DryingRoom dryingRoom;
    private DryingSession session;
    private Employee employee;
    private Resource electricity;
    private Resource gas;
    private Long roomId = 1L;
    private Long sessionId = 1L;
    private Long employeeId = 1L;

    @BeforeEach
    void setUp() {

        this.dryingSessionService = new DryingSessionService(
            sessionRepository,
            roomRepository,
            resourceRepository,
            employeeRepository
        );

        dryingRoom = new DryingRoom();
        dryingRoom.setId(roomId);
        dryingRoom.setName("Estufa Principal");
        dryingRoom.setGasConsumptionPerHour(2.5);

        Machine machine = new Machine();
        machine.setPower(2.0);
        dryingRoom.getMachines().add(machine);

        session = new DryingSession();
        session.setId(sessionId);
        session.setHours(8.0);
        session.setDryingRoom(dryingRoom);

        employee = new Employee();
        employee.setId(employeeId);
        employee.setName("Ana");
        employee.setCostPerHour(new BigDecimal("20.00"));

        electricity = new Resource();
        electricity.setCategory(ResourceCategory.ELECTRICITY);
        electricity.setUnitValue(new BigDecimal("0.75"));

        gas = new Resource();
        gas.setCategory(ResourceCategory.GAS);
        gas.setUnitValue(new BigDecimal("4.00"));
    }

    @Test
    void findAllByParentId_ShouldReturnSessionsList() {
        DryingSessionEmployeeUsage employeeUsage = new DryingSessionEmployeeUsage();
        employeeUsage.setEmployee(employee);
        session.getEmployeeUsages().add(employeeUsage);
        dryingRoom.getSessions().add(session);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(dryingRoom));

        List<DryingSessionResponseDTO> result = dryingSessionService.findAllByParentId(roomId);

        assertEquals(1, result.size());
        assertEquals(sessionId, result.getFirst().id());
        assertFalse(result.getFirst().employeeUsages().isEmpty());
    }

    @Test
    void findById_WhenValidIds_ShouldReturnSession() {
        DryingSessionEmployeeUsage employeeUsage = new DryingSessionEmployeeUsage();
        employeeUsage.setEmployee(employee);
        session.getEmployeeUsages().add(employeeUsage);

        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        DryingSessionResponseDTO result = dryingSessionService.findById(roomId, sessionId);

        assertEquals(sessionId, result.id());
        assertFalse(result.employeeUsages().isEmpty());
    }

    @Test
    void create_WithValidData_ShouldPersistSession() {
        DryingSessionRequestDTO dto = new DryingSessionRequestDTO(8.0, List.of(new EmployeeUsageRequestDTO(1.0, employeeId)));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(dryingRoom));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricity));
        when(resourceRepository.findByCategory(ResourceCategory.GAS)).thenReturn(Optional.of(gas));
        when(sessionRepository.save(any(DryingSession.class))).thenAnswer(invocation -> {
            DryingSession saved = invocation.getArgument(0);
            saved.setId(sessionId);
            return saved;
        });

        DryingSessionResponseDTO result = dryingSessionService.create(roomId, dto);
        
        // Custo = (8h * 2.5 * R$4) + (2kW * 0.74 * 8h * R$0.75) + (1h * R$20) = 80.00 + 8.88 + 20.00 = R$108.88
        assertEquals(0, new BigDecimal("108.88").compareTo(result.costAtTime()));
        assertFalse(result.employeeUsages().isEmpty());
    }

    @Test
    void create_WithMissingEmployee_ShouldThrowException() {
        DryingSessionRequestDTO dto = new DryingSessionRequestDTO(8.0, List.of(new EmployeeUsageRequestDTO(1.0, 999L)));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(dryingRoom));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dryingSessionService.create(roomId, dto));
    }

    @Test
    void update_WithValidData_ShouldUpdateSessionAndRecalculateCost() {
        DryingSessionEmployeeUsage initialUsage = new DryingSessionEmployeeUsage();
        initialUsage.setEmployee(employee);
        initialUsage.setUsageTime(1.0);
        session.getEmployeeUsages().add(initialUsage);
        DryingSessionRequestDTO dto = new DryingSessionRequestDTO(10.0, List.of(new EmployeeUsageRequestDTO(1.5, employeeId)));

        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricity));
        when(resourceRepository.findByCategory(ResourceCategory.GAS)).thenReturn(Optional.of(gas));
        when(sessionRepository.save(any())).thenReturn(session);

        DryingSessionResponseDTO result = dryingSessionService.update(roomId, sessionId, dto);

        // Custo = (10h * 2.5 * R$4) + (2kW * 0.74 * 10h * R$0.75) + (1.5h * R$20) = 100.00 + 11.10 + 30.00 = R$141.10
        assertEquals(0, new BigDecimal("141.10").compareTo(result.costAtTime()));
        assertEquals(10.0, result.hours());
        assertEquals(1.5, result.employeeUsages().getFirst().usageTime());
    }

    @Test
    void update_WhenEmployeeNotFound_ShouldThrowException() {
        DryingSessionRequestDTO dto = new DryingSessionRequestDTO(10.0, List.of(new EmployeeUsageRequestDTO(1.5, 999L)));
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dryingSessionService.update(roomId, sessionId, dto));
    }
    
    @Test
    void delete_WhenValidIds_ShouldRemoveSession() {
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        dryingSessionService.delete(roomId, sessionId);

        verify(sessionRepository).delete(session);
    }

    @Test
    void delete_WhenSessionNotExists_ShouldThrowException() {
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> dryingSessionService.delete(roomId, sessionId));
    }
}