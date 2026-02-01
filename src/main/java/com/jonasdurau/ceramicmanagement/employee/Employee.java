package com.jonasdurau.ceramicmanagement.employee;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.employee.category.EmployeeCategory;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_employee")
public class Employee extends BaseEntity {

    private String name;

    @ManyToMany
    @JoinTable(name = "tb_employee_categories", joinColumns = @JoinColumn(name = "employee_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<EmployeeCategory> categories = new ArrayList<>();

    private BigDecimal costPerHour;

    public Employee() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EmployeeCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<EmployeeCategory> categories) {
        this.categories = categories;
    }

    public BigDecimal getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(BigDecimal costPerHour) {
        this.costPerHour = costPerHour;
    }
}
