package com.jonasdurau.ceramicmanagement.glazefiring.employeeusage;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiring;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEmployeeUsage;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_glaze_firing_employee_usage")
public class GlazeFiringEmployeeUsage extends BaseEmployeeUsage {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "glaze_firing_id")
    private GlazeFiring glazeFiring;

    public GlazeFiringEmployeeUsage() {
    }

    public GlazeFiringEmployeeUsage(Long id, double usageTime, Employee employee, GlazeFiring glazeFiring) {
        super(id, usageTime, employee);
        this.glazeFiring = glazeFiring;
    }

    public GlazeFiring getGlazeFiring() {
        return glazeFiring;
    }

    public void setGlazeFiring(GlazeFiring glazeFiring) {
        this.glazeFiring = glazeFiring;
    }
}
