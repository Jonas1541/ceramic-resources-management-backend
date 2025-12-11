package com.jonasdurau.ceramicmanagement.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategory;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{

    boolean existsByCategory(EmployeeCategory category);
}
