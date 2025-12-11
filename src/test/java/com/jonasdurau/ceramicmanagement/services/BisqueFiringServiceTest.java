package com.jonasdurau.ceramicmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.product.line.ProductLine;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransactionRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;
import com.jonasdurau.ceramicmanagement.product.type.ProductType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiring;
import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiringRepository;
import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiringService;
import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BisqueFiringRequestDTO;
import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BisqueFiringResponseDTO;
import com.jonasdurau.ceramicmanagement.bisquefiring.employeeusage.BisqueFiringEmployeeUsage;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.employee.EmployeeRepository;
import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategory;
import com.jonasdurau.ceramicmanagement.kiln.Kiln;
import com.jonasdurau.ceramicmanagement.kiln.KilnRepository;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.resource.ResourceRepository;
import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.shared.dto.EmployeeUsageRequestDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.FiringListDTO;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceDeletionException;
import com.jonasdurau.ceramicmanagement.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class BisqueFiringServiceTest {

    @Mock
    private BisqueFiringRepository firingRepository;

    @Mock
    private KilnRepository kilnRepository;

    @Mock
    private ProductTransactionRepository productTransactionRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private BisqueFiringService bisqueFiringService;

    private Kiln kiln;
    private BisqueFiring firing;
    private ProductTransaction biscuit;
    private Employee employee;
    private Resource electricity;
    private Resource gas;
    private Long kilnId = 1L;
    private Long firingId = 1L;
    private Long biscuitId = 1L;
    private Long employeeId = 1L;

    @BeforeEach
    void setUp() {

        this.bisqueFiringService = new BisqueFiringService(
            firingRepository,
            kilnRepository,
            productTransactionRepository,
            resourceRepository,
            employeeRepository
        );
        
        kiln = new Kiln();
        kiln.setId(kilnId);
        kiln.setName("Forno Principal");
        kiln.setGasConsumptionPerHour(2.5);

        Machine kilnMachine = new Machine();
        kilnMachine.setPower(10.0);
        kiln.getMachines().add(kilnMachine);

        EmployeeCategory category = new EmployeeCategory();
        category.setName("Operador de Forno");

        employee = new Employee();
        employee.setId(employeeId);
        employee.setName("José");
        employee.setCostPerHour(new BigDecimal("25.00"));
        employee.setCategory(category);

        firing = new BisqueFiring();
        firing.setId(firingId);
        firing.setTemperature(1000.0);
        firing.setBurnTime(8.0);
        firing.setCoolingTime(4.0);
        firing.setKiln(kiln);

        ProductType type = new ProductType();
        type.setName("Vaso");
        ProductLine line = new ProductLine();
        line.setName("Coleção Verão");
        Product product = new Product();
        product.setName("Vaso Decorativo");
        product.setPrice(new BigDecimal("150.00"));
        product.setType(type);
        product.setLine(line);

        biscuit = new ProductTransaction();
        biscuit.setId(biscuitId);
        biscuit.setState(ProductState.GREENWARE);
        biscuit.setProduct(product);

        electricity = new Resource();
        electricity.setCategory(ResourceCategory.ELECTRICITY);
        electricity.setUnitValue(new BigDecimal("0.50"));

        gas = new Resource();
        gas.setCategory(ResourceCategory.GAS);
        gas.setUnitValue(new BigDecimal("3.00"));
    }
    
    @Test
    void findById_WhenExists_ShouldReturnFiring() {
        BisqueFiringEmployeeUsage employeeUsage = new BisqueFiringEmployeeUsage();
        employeeUsage.setEmployee(employee);
        employeeUsage.setUsageTime(3.0);
        firing.getEmployeeUsages().add(employeeUsage);

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));
        
        BisqueFiringResponseDTO result = bisqueFiringService.findById(kilnId, firingId);
        
        assertEquals(firingId, result.id());
        assertFalse(result.employeeUsages().isEmpty());
        verify(firingRepository).findByIdAndKilnId(firingId, kilnId);
    }

    @Test
    void create_WithValidData_ShouldCreateFiring() {
        BisqueFiringRequestDTO dto = new BisqueFiringRequestDTO(
            1000.0, 8.0, 4.0, 
            List.of(biscuitId),
            List.of(new EmployeeUsageRequestDTO(3.0, employeeId))
        );

        when(kilnRepository.findById(kilnId)).thenReturn(Optional.of(kiln));
        when(productTransactionRepository.findById(biscuitId)).thenReturn(Optional.of(biscuit));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricity));
        when(resourceRepository.findByCategory(ResourceCategory.GAS)).thenReturn(Optional.of(gas));
        when(firingRepository.save(any(BisqueFiring.class))).thenReturn(firing);

        BisqueFiringResponseDTO result = bisqueFiringService.create(kilnId, dto);

        assertNotNull(result);
        assertEquals(ProductState.BISCUIT, biscuit.getState());
        assertFalse(result.employeeUsages().isEmpty());
        
        BigDecimal expectedCost = new BigDecimal("179.40");
        BigDecimal actualCost = result.cost();
        String errorMessage = "O custo calculado está incorreto. Esperado: " + expectedCost + ", mas foi: " + actualCost;
        
        assertEquals(0, expectedCost.compareTo(actualCost), errorMessage);
        
        verify(firingRepository, times(2)).save(any(BisqueFiring.class));
    }
    
    @Test
    void findAllByParentId_WhenKilnExists_ShouldReturnList() {
        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByKilnId(kilnId)).thenReturn(List.of(firing));
        List<FiringListDTO> result = bisqueFiringService.findAllByParentId(kilnId);
        assertEquals(1, result.size());
        assertEquals(firingId, result.getFirst().id());
        verify(firingRepository).findByKilnId(kilnId);
    }

    @Test
    void create_WithMissingEmployee_ShouldThrowException() {
        BisqueFiringRequestDTO dto = new BisqueFiringRequestDTO(1000.0, 8.0, 4.0, List.of(biscuitId), List.of(new EmployeeUsageRequestDTO(3.0, 999L)));
        when(kilnRepository.findById(kilnId)).thenReturn(Optional.of(kiln));
        when(firingRepository.save(any(BisqueFiring.class))).thenReturn(new BisqueFiring());
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bisqueFiringService.create(kilnId, dto));
    }

    @Test
    void update_WhenValid_ShouldUpdateFiringAndBiscuits() {
        BisqueFiringEmployeeUsage initialUsage = new BisqueFiringEmployeeUsage();
        initialUsage.setEmployee(employee);
        initialUsage.setUsageTime(3.0);
        firing.getEmployeeUsages().add(initialUsage);
        
        BisqueFiringRequestDTO dto = new BisqueFiringRequestDTO(
            1100.0, 9.0, 5.0, 
            List.of(biscuitId),
            List.of(new EmployeeUsageRequestDTO(4.0, employeeId))
        );

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));
        when(productTransactionRepository.findById(biscuitId)).thenReturn(Optional.of(biscuit));
        when(resourceRepository.findByCategory(ResourceCategory.ELECTRICITY)).thenReturn(Optional.of(electricity));
        when(resourceRepository.findByCategory(ResourceCategory.GAS)).thenReturn(Optional.of(gas));
        when(firingRepository.save(any())).thenReturn(firing);

        BisqueFiringResponseDTO result = bisqueFiringService.update(kilnId, firingId, dto);

        assertEquals(1100.0, result.temperature());
        assertEquals(4.0, firing.getEmployeeUsages().getFirst().getUsageTime());
        verify(firingRepository).save(any());
    }
    
    @Test
    void update_WhenEmployeeNotFound_ShouldThrowException() {
        BisqueFiringRequestDTO dto = new BisqueFiringRequestDTO(1100.0, 9.0, 5.0, List.of(biscuitId), List.of(new EmployeeUsageRequestDTO(4.0, 999L)));
        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bisqueFiringService.update(kilnId, firingId, dto));
    }

    @Test
    void delete_WhenValid_ShouldDeleteFiring() {
        biscuit.setState(ProductState.BISCUIT);
        firing.getBiscuits().add(biscuit);

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));

        bisqueFiringService.delete(kilnId, firingId);

        assertEquals(ProductState.GREENWARE, biscuit.getState());
        verify(firingRepository).delete(firing);
    }

    @Test
    void delete_WhenBiscuitIsGlazed_ShouldThrowException() {
        biscuit.setState(ProductState.GLAZED);
        firing.getBiscuits().add(biscuit);

        when(kilnRepository.existsById(kilnId)).thenReturn(true);
        when(firingRepository.findByIdAndKilnId(firingId, kilnId)).thenReturn(Optional.of(firing));

        assertThrows(ResourceDeletionException.class, () -> bisqueFiringService.delete(kilnId, firingId));
    }
}