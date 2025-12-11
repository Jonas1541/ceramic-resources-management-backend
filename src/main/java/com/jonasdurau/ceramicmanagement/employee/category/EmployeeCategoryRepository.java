package com.jonasdurau.ceramicmanagement.employee.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeCategoryRepository extends JpaRepository<EmployeeCategory, Long>{

    boolean existsByName(String name);
}
