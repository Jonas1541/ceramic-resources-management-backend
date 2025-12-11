package com.jonasdurau.ceramicmanagement.employee.category;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.employee.category.dto.EmployeeCategoryRequestDTO;
import com.jonasdurau.ceramicmanagement.employee.category.dto.EmployeeCategoryResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/employee-categories")
public class EmployeeCategoryController extends IndependentController<EmployeeCategoryResponseDTO, EmployeeCategoryRequestDTO, EmployeeCategoryResponseDTO, Long, EmployeeCategoryService>{
}
