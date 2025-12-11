package com.jonasdurau.ceramicmanagement.shared.persistence;

import java.math.BigDecimal;

import com.jonasdurau.ceramicmanagement.employee.Employee;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEmployeeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double usageTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public BaseEmployeeUsage() {
    }

    public BaseEmployeeUsage(Long id, double usageTime, Employee employee) {
        this.id = id;
        this.usageTime = usageTime;
        this.employee = employee;
    }

    public BigDecimal getCost() {
        return BigDecimal.valueOf(usageTime).multiply(employee.getCostPerHour());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(double usageTime) {
        this.usageTime = usageTime;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseEmployeeUsage other = (BaseEmployeeUsage) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
