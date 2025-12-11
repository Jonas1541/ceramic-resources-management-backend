package com.jonasdurau.ceramicmanagement.glaze.employeeusage;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEmployeeUsage;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_glaze_employee_usage")
public class GlazeEmployeeUsage extends BaseEmployeeUsage {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "glaze_id")
    private Glaze glaze;

    public GlazeEmployeeUsage() {
    }

    public GlazeEmployeeUsage(Long id, double usageTime, Employee employee, Glaze glaze) {
        super(id, usageTime, employee);
        this.glaze = glaze;
    }

    public Glaze getGlaze() {
        return glaze;
    }

    public void setGlaze(Glaze glaze) {
        this.glaze = glaze;
    }
}
