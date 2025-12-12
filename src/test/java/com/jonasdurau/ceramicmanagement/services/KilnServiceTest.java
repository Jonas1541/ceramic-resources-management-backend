package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.kiln.Kiln;
import com.jonasdurau.ceramicmanagement.kiln.KilnRepository;
import com.jonasdurau.ceramicmanagement.kiln.KilnService;
import com.jonasdurau.ceramicmanagement.kiln.dto.KilnRequestDTO;
import com.jonasdurau.ceramicmanagement.kiln.dto.KilnResponseDTO;
import com.jonasdurau.ceramicmanagement.kiln.validation.KilnDeletionValidator;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.machine.MachineRepository;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class KilnServiceTest {

    @Mock
    private KilnRepository kilnRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private KilnDeletionValidator kilnDeletionValidator;

    private KilnService kilnService;

    private Kiln kiln;
    private KilnRequestDTO requestDTO;
    private Long testId;
    private Machine machine1;
    private Machine machine2;

    @BeforeEach
    void setUp() {
        testId = 1L;

        this.kilnService = new KilnService(
            kilnRepository, 
            machineRepository, 
            List.of(kilnDeletionValidator)
        );

        machine1 = new Machine();
        machine1.setId(1L);
        machine1.setName("Máquina A");

        machine2 = new Machine();
        machine2.setId(2L);
        machine2.setName("Máquina B");
        
        kiln = new Kiln();
        kiln.setId(testId);
        kiln.setName("Forno Principal");
        kiln.setCreatedAt(Instant.now());
        kiln.setUpdatedAt(Instant.now());
        List<Long> machineRequestDTOs = new ArrayList<>();
        machineRequestDTOs.add(1L);
        machineRequestDTOs.add(2L);
        requestDTO = new KilnRequestDTO("Forno Principal", 10, machineRequestDTOs);
    }

    @Test
    void findAll_ShouldReturnListOfKilns() {
        when(kilnRepository.findAll()).thenReturn(List.of(kiln));

        List<KilnResponseDTO> result = kilnService.findAll();

        assertEquals(1, result.size());
        assertEquals(testId, result.getFirst().id());
        verify(kilnRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnKiln() {
        when(kilnRepository.findById(testId)).thenReturn(Optional.of(kiln));

        KilnResponseDTO result = kilnService.findById(testId);

        assertEquals(testId, result.id());
        assertEquals("Forno Principal", result.name());
        verify(kilnRepository).findById(testId);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(kilnRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kilnService.findById(testId));
        verify(kilnRepository).findById(testId);
    }

    @Test
    void create_WithValidData_ShouldReturnKiln() {
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine1));
        when(machineRepository.findById(2L)).thenReturn(Optional.of(machine2));
        when(kilnRepository.save(any(Kiln.class))).thenAnswer(invocation -> {
            Kiln saved = invocation.getArgument(0);
            saved.setId(testId);
            return saved;
        });

        KilnResponseDTO result = kilnService.create(requestDTO);

        assertEquals(testId, result.id());
        verify(kilnRepository).save(any(Kiln.class));
    }

    @Test
    void update_WithValidData_ShouldUpdateKiln() {
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine1));
        when(machineRepository.findById(2L)).thenReturn(Optional.of(machine2));
        when(kilnRepository.findById(testId)).thenReturn(Optional.of(kiln));
        when(kilnRepository.save(any(Kiln.class))).thenReturn(kiln);

        KilnResponseDTO result = kilnService.update(testId, requestDTO);

        assertEquals(testId, result.id());
        assertEquals("Forno Principal", result.name());
        verify(kilnRepository).save(any(Kiln.class));
    }

    @Test
    void update_WhenKilnNotFound_ShouldThrowException() {
        when(kilnRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kilnService.update(testId, requestDTO));
        verify(kilnRepository, never()).save(any());
    }

    @Test
    void delete_WhenValidationPasses_ShouldDeleteKiln() {
        // Arrange
        when(kilnRepository.findById(testId)).thenReturn(Optional.of(kiln));
        // Validador não faz nada (sucesso)

        // Act
        kilnService.delete(testId);

        // Assert
        verify(kilnDeletionValidator).validate(testId);
        verify(kilnRepository).delete(kiln);
    }

    @Test
    void delete_WhenValidatorThrowsException_ShouldAbortDeletion() {
        // Arrange
        when(kilnRepository.findById(testId)).thenReturn(Optional.of(kiln));
        
        // Simula que o validador (seja ele de Bisque ou Glaze) bloqueou
        doThrow(new ResourceDeletionException("Forno possui queimas associadas"))
            .when(kilnDeletionValidator).validate(testId);

        // Act & Assert
        assertThrows(ResourceDeletionException.class, () -> kilnService.delete(testId));
        
        verify(kilnDeletionValidator).validate(testId);
        verify(kilnRepository, never()).delete(any());
    }

    @Test
    void yearlyReport_WhenKilnExists_ShouldReturnReport() {
        when(kilnRepository.findById(testId)).thenReturn(Optional.of(kiln));

        List<YearReportDTO> reports = kilnService.yearlyReport(testId);

        assertTrue(reports.isEmpty());
        verify(kilnRepository).findById(testId);
    }

    @Test
    void yearlyReport_WhenKilnNotExists_ShouldThrowException() {
        when(kilnRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kilnService.yearlyReport(testId));
        verify(kilnRepository).findById(testId);
    }
}