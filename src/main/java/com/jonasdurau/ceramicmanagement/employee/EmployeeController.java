package com.jonasdurau.ceramicmanagement.employee;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.employee.dto.EmployeeRequestDTO;
import com.jonasdurau.ceramicmanagement.employee.dto.EmployeeResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController extends IndependentController<EmployeeResponseDTO, EmployeeRequestDTO, EmployeeResponseDTO, Long, EmployeeService>{
}
