package com.jonasdurau.ceramicmanagement.employee;

import java.math.BigDecimal;

import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategory;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_employee")
public class Employee extends BaseEntity {
    
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_category_id")
    private EmployeeCategory category;
    
    private BigDecimal costPerHour;

    public Employee() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmployeeCategory getCategory() {
        return category;
    }

    public void setCategory(EmployeeCategory category) {
        this.category = category;
    }

    public BigDecimal getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(BigDecimal costPerHour) {
        this.costPerHour = costPerHour;
    }
}
