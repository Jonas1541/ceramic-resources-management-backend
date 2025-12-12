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

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategory;
import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.glaze.GlazeRepository;
import com.jonasdurau.ceramicmanagement.glaze.GlazeService;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeListDTO;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeResponseDTO;
import com.jonasdurau.ceramicmanagement.glaze.employeeusage.GlazeEmployeeUsage;
import com.jonasdurau.ceramicmanagement.glaze.employeeusage.GlazeEmployeeUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsage;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.GlazeMachineUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.machineusage.dto.GlazeMachineUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsage;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.GlazeResourceUsageRepository;
import com.jonasdurau.ceramicmanagement.glaze.resourceusage.dto.GlazeResourceUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.validation.GlazeDeletionValidator;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class GlazeServiceTest {

    @Mock
    private GlazeRepository glazeRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GlazeEmployeeUsageRepository glazeEmployeeUsageRepository;

    @Mock
    private GlazeResourceUsageRepository glazeResourceUsageRepository;

    @Mock
    private GlazeMachineUsageRepository glazeMachineUsageRepository;

    @Mock
    private GlazeDeletionValidator glazeDeletionValidator;

    private GlazeService glazeService;

    private Glaze glaze;
    private GlazeRequestDTO requestDTO;
    private Long testId;
    private Resource electricityResource;
    private Employee employee;

    @BeforeEach
    void setUp() {
        testId = 1L;

        this.glazeService = new GlazeService(
            glazeRepository,
            resourceRepository,
            machineRepository,
            employeeRepository,
            glazeResourceUsageRepository,
            glazeMachineUsageRepository,
            glazeEmployeeUsageRepository,
            List.of(glazeDeletionValidator)
        );

        electricityResource = new Resource();
        electricityResource.setId(1L);
        electricityResource.setCategory(ResourceCategory.ELECTRICITY);
        electricityResource.setUnitValue(new BigDecimal("0.50"));

        EmployeeCategory category = new EmployeeCategory();
        category.setId(1L);
        category.setName("Pintor");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("João");
        employee.setCostPerHour(new BigDecimal("20.00"));
        employee.setCategory(category);

        glaze = new Glaze();
        glaze.setId(testId);
        glaze.setColor("Azul");
        glaze.setCreatedAt(Instant.now());
        glaze.setUpdatedAt(Instant.now());
        glaze.setUnitCost(new BigDecimal("50.00"));

        GlazeEmployeeUsage employeeUsage = new GlazeEmployeeUsage();
        employeeUsage.setEmployee(employee);
        employeeUsage.setGlaze(glaze);
        employeeUsage.setUsageTime(2.0);
        glaze.getEmployeeUsages().add(employeeUsage);

        requestDTO = new GlazeRequestDTO(
            "Azul",
            List.of(new GlazeResourceUsageRequestDTO(1L, 2.0)),
            List.of(new GlazeMachineUsageRequestDTO(1L, 5.0)),
            List.of(new EmployeeUsageRequestDTO(2.0, 1L))
        );
    }

    @Test
    void findAll_ShouldReturnListOfGlazes() {
        when(glazeRepository.findAll()).thenReturn(List.of(glaze));
        List<GlazeListDTO> result = glazeService.findAll();
        assertEquals(1, result.size());
        assertEquals(testId, result.getFirst().id());
        verify(glazeRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnGlaze() {
        when(glazeRepository.findById(testId)).thenReturn(Optional.of(glaze));
        GlazeResponseDTO result = glazeService.findById(testId);
        assertEquals(testId, result.id());
        assertFalse(result.employeeUsages().isEmpty());
        verify(glazeRepository).findById(testId);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(glazeRepository.findById(testId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> glazeService.findById(testId));
        verify(glazeRepository).findById(testId);
    }

    @Test
    void create_WithValidData_ShouldReturnGlaze() {
        Resource mockResource = new Resource();
        mockResource.setUnitValue(new BigDecimal("2.00"));

        Machine mockMachine = new Machine();
        mockMachine.setId(1L);
        mockMachine.setPower(1500.0);
        
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(mockResource));
        when(machineRepository.findById(1L)).thenReturn(Optional.of(mockMachine));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));
        when(glazeRepository.save(any())).thenAnswer(invocation -> {
            Glaze saved = invocation.getArgument(0);
            saved.setId(testId);
            return saved;
        });
    
        GlazeResponseDTO result = glazeService.create(requestDTO);
    
        assertEquals(testId, result.id());
        assertNotNull(result.unitCost());
        assertFalse(result.employeeUsages().isEmpty());
        verify(glazeRepository).save(any());
    }

    @Test
    void create_WithMissingEmployee_ShouldThrowException() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(new Resource()));
        when(machineRepository.findById(1L)).thenReturn(Optional.of(new Machine()));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeService.create(requestDTO));
        verify(glazeRepository, never()).save(any());
    }

    @Test
    void update_WithValidData_ShouldUpdateGlaze() {
        // Arrange
        Resource mockResource = new Resource();
        mockResource.setId(1L);
        mockResource.setUnitValue(new BigDecimal("2.00"));

        Machine mockMachine = new Machine();
        mockMachine.setId(1L);
        mockMachine.setPower(1500.0);
        
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(mockResource));
        when(machineRepository.findById(1L)).thenReturn(Optional.of(mockMachine));

        when(glazeRepository.findById(testId)).thenReturn(Optional.of(glaze));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));
        when(glazeRepository.save(any())).thenReturn(glaze);
        
        // Act
        GlazeResponseDTO result = glazeService.update(testId, requestDTO);
    
        // Assert
        assertEquals(testId, result.id());
        assertEquals(1, result.employeeUsages().size());
        verify(glazeRepository).save(any());
    }
    
    @Test
    void update_WhenEmployeeNotFound_ShouldThrowException() {
        GlazeRequestDTO invalidRequest = new GlazeRequestDTO("Azul", List.of(), List.of(), List.of(new EmployeeUsageRequestDTO(1.0, 999L)));
        when(glazeRepository.findById(testId)).thenReturn(Optional.of(glaze));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeService.update(testId, invalidRequest));
        verify(glazeRepository, never()).save(any());
    }

    @Test
    void delete_WhenValidationPasses_ShouldDeleteGlaze() {
        // Arrange
        when(glazeRepository.findById(testId)).thenReturn(Optional.of(glaze));
        
        // O mock do validador não faz nada (void), simulando sucesso

        // Act
        glazeService.delete(testId);

        // Assert
        verify(glazeDeletionValidator).validate(testId); // Verifica se o validador foi chamado
        verify(glazeRepository).delete(glaze);
    }

    @Test
    void delete_WhenValidatorThrowsException_ShouldAbortDeletion() {
        // Arrange
        when(glazeRepository.findById(testId)).thenReturn(Optional.of(glaze));
        
        // Simula que o validador encontrou uma transação e proibiu a deleção
        doThrow(new ResourceDeletionException("Não é possível deletar..."))
            .when(glazeDeletionValidator).validate(testId);

        // Act & Assert
        assertThrows(ResourceDeletionException.class, () -> glazeService.delete(testId));
        
        verify(glazeDeletionValidator).validate(testId);
        verify(glazeRepository, never()).delete(any());
    }

    @Test
    void yearlyReport_WhenGlazeExists_ShouldReturnReport() {
        when(glazeRepository.findById(testId)).thenReturn(Optional.of(glaze));
        List<YearReportDTO> reports = glazeService.yearlyReport(testId);
        assertTrue(reports.isEmpty());
        verify(glazeRepository).findById(testId);
    }

    @Test
    void recalculateGlazesByResource_ShouldUpdateGlazes() {
        GlazeResourceUsage usage = new GlazeResourceUsage();
        usage.setGlaze(glaze);
        when(glazeResourceUsageRepository.findByResourceId(testId)).thenReturn(List.of(usage));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));

        glazeService.recalculateGlazesByResource(testId);

        verify(glazeRepository).save(glaze);
    }

    @Test
    void recalculateGlazesByMachine_ShouldUpdateGlazes() {
        GlazeMachineUsage usage = new GlazeMachineUsage();
        usage.setGlaze(glaze);
        when(glazeMachineUsageRepository.findByMachineId(testId)).thenReturn(List.of(usage));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));

        glazeService.recalculateGlazesByMachine(testId);

        verify(glazeRepository).save(glaze);
    }

    @Test
    void recalculateGlazesByEmployee_ShouldUpdateGlazes() {
        GlazeEmployeeUsage usage = new GlazeEmployeeUsage();
        usage.setGlaze(glaze);
        when(glazeEmployeeUsageRepository.findByEmployeeId(1L)).thenReturn(List.of(usage));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricityResource));

        glazeService.recalculateGlazesByEmployee(1L);

        verify(glazeRepository).save(glaze);
    }
}