package com.jonasdurau.ceramicmanagement.employee.category;

import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_employee_category")
public class EmployeeCategory extends BaseEntity {

    private String name;

    @ManyToMany(mappedBy = "categories")
    private List<Employee> employees = new ArrayList<>();

    public EmployeeCategory() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
}
