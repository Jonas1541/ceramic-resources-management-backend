package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransaction;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransactionService;
import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiring;
import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiringRepository;
import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiringService;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlazeFiringRequestDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlazeFiringResponseDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlostRequestDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.employeeusage.GlazeFiringEmployeeUsage;
import com.jonasdurau.ceramicmanagement.kiln.Kiln;
import com.jonasdurau.ceramicmanagement.kiln.KilnRepository;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransactionRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.FiringListDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class GlazeFiringServiceTest {

    @Mock
    private GlazeFiringRepository firingRepository;
    @Mock
    private KilnRepository kilnRepository;
    @Mock
    private ProductTransactionRepository productTransactionRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private GlazeTransactionService glazeTransactionService;

    @InjectMocks
    private GlazeFiringService glazeFiringService;

    private Kiln kiln;
    private GlazeFiring firing;
    private ProductTransaction glost;
    private Employee employee;
    private Resource electricity;
    private Resource gas;
    private Glaze glaze;
    private Long kilnId = 1L;
    private Long firingId = 1L;
    private Long glostId = 1L;
    private Long glazeId = 1L;
    private Long employeeId = 1L;

    @BeforeEach
    void setUp() {
        kiln = new Kiln();
        kiln.setId(kilnId);
        kiln.setName("Forno de Esmalte");
        kiln.setGasConsumptionPerHour(3.0);
        Machine kilnMachine = new Machine();
        kilnMachine.setPower(10.0);
        kiln.getMachines().add(kilnMachine);

        firing = new GlazeFiring();
        firing.setId(firingId);
        firing.setTemperature(800.0);
        firing.setBurnTime(6.0);
        firing.setCoolingTime(3.0);
        firing.setKiln(kiln);

        Product product = new Product();
        product.setId(1L);
        product.setName("Vaso Esmaltado");

        glost = new ProductTransaction();
        glost.setId(glostId);
        glost.setState(ProductState.BISCUIT);
        glost.setProduct(product);

        employee = new Employee();
        employee.setId(employeeId);
        employee.setName("Maria");
        employee.setCostPerHour(new BigDecimal("30.00"));

        electricity = new Resource();
        electricity.setCategory(ResourceCategory.ELECTRICITY);
        electricity.setUnitValue(new BigDecimal("0.60"));

        gas = new Resource();
        gas.setCategory(ResourceCategory.GAS);
        gas.setUnitValue(new BigDecimal("3.50"));

        glaze = new Glaze();
        glaze.setId(glazeId);
        glaze.setColor("Azul Cobalto");
    }

    @Test
    void findAllByParentId_WhenKilnExists_ShouldReturnList() {
        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByKilnId(kilnId)).thenReturn(List.of(firing));

        List<FiringListDTO> result = glazeFiringService.findAllByParentId(kilnId);

        assertEquals(1, result.size());
        assertEquals(firingId, result.getFirst().id());
    }

    @Test
    void findById_WhenExists_ShouldReturnFiring() {
        GlazeFiringEmployeeUsage employeeUsage = new GlazeFiringEmployeeUsage();
        employeeUsage.setEmployee(employee);
        employeeUsage.setUsageTime(2.0);
        firing.getEmployeeUsages().add(employeeUsage);

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));

        GlazeFiringResponseDTO result = glazeFiringService.findById(kilnId, firingId);

        assertEquals(firingId, result.id());
        assertFalse(result.employeeUsages().isEmpty());
    }

    @Test
    void create_WithValidData_ShouldCreateFiring() {
        GlazeFiringRequestDTO dto = new GlazeFiringRequestDTO(
            800.0, 6.0, 3.0,
            List.of(new GlostRequestDTO(glostId, glazeId)),
            List.of(new EmployeeUsageRequestDTO(2.0, employeeId))
        );
        
        when(kilnRepository.findById(kilnId)).thenReturn(Optional.of(kiln));
        when(productTransactionRepository.findById(glostId)).thenReturn(Optional.of(glost));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricity));
        when(resourceRepository.findByCategory(ResourceCategory.GAS)).thenReturn(Optional.of(gas));
        when(firingRepository.save(any(GlazeFiring.class))).thenReturn(firing);

        GlazeFiringResponseDTO result = glazeFiringService.create(kilnId, dto);

        assertNotNull(result);
        assertFalse(result.employeeUsages().isEmpty());
        // Custo = (6h * 3.0 * R$3.5) + (10kW * 0.74 * 9h * R$0.6) + (2h * R$30) = 63.00 + 39.96 + 60.00 = R$162.96
        assertEquals(0, new BigDecimal("162.96").compareTo(result.cost()));
        verify(firingRepository, times(2)).save(any(GlazeFiring.class));
    }

    @Test
    void create_WithMissingEmployee_ShouldThrowException() {
        GlazeFiringRequestDTO dto = new GlazeFiringRequestDTO(800.0, 6.0, 3.0, List.of(new GlostRequestDTO(glostId, glazeId)), List.of(new EmployeeUsageRequestDTO(2.0, 999L)));
        when(kilnRepository.findById(kilnId)).thenReturn(Optional.of(kiln));
        when(firingRepository.save(any(GlazeFiring.class))).thenReturn(new GlazeFiring());
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeFiringService.create(kilnId, dto));
    }

    @Test
    void update_WithValidData_ShouldUpdateFiring() {
        // Arrange
        GlazeFiringEmployeeUsage initialUsage = new GlazeFiringEmployeeUsage();
        initialUsage.setEmployee(employee);
        initialUsage.setUsageTime(2.0);
        firing.getEmployeeUsages().add(initialUsage);
        firing.getGlosts().add(glost);

        GlazeFiringRequestDTO dto = new GlazeFiringRequestDTO(
            850.0, 7.0, 4.0,
            List.of(new GlostRequestDTO(glostId, glazeId)),
            List.of(new EmployeeUsageRequestDTO(2.5, employeeId))
        );

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));
        when(productTransactionRepository.findById(glostId)).thenReturn(Optional.of(glost));
        when(firingRepository.save(any(GlazeFiring.class))).thenReturn(firing);
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricity));
        when(resourceRepository.findByCategory(ResourceCategory.GAS)).thenReturn(Optional.of(gas));

        // Act
        GlazeFiringResponseDTO result = glazeFiringService.update(kilnId, firingId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(850.0, result.temperature());
        assertEquals(2.5, firing.getEmployeeUsages().getFirst().getUsageTime());
        verify(firingRepository).save(any(GlazeFiring.class));
    }
    
    @Test
    void update_WhenEmployeeNotFound_ShouldThrowException() {
        GlazeFiringRequestDTO dto = new GlazeFiringRequestDTO(850.0, 7.0, 4.0, List.of(), List.of(new EmployeeUsageRequestDTO(2.5, 999L)));
        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> glazeFiringService.update(kilnId, firingId, dto));
    }

    @Test
    void delete_ShouldResetGlostsAndTransactions() {
        GlazeTransaction glazeTx = new GlazeTransaction();
        glost.setGlazeTransaction(glazeTx);
        glost.setGlazeFiring(firing);
        firing.getGlosts().add(glost);

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));

        glazeFiringService.delete(kilnId, firingId);

        assertEquals(ProductState.BISCUIT, glost.getState());
        assertNull(glost.getGlazeTransaction());
        verify(firingRepository).delete(firing);
    }
}