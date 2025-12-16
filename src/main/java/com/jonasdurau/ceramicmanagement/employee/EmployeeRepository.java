package com.jonasdurau.ceramicmanagement.employee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{

    boolean existsByCategoryId(Long employeeCategoryId);
}
