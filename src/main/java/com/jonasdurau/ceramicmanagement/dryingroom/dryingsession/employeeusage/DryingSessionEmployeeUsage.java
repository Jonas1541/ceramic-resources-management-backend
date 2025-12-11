package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.employeeusage;

import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSession;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEmployeeUsage;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_drying_session_employee_usage")
public class DryingSessionEmployeeUsage extends BaseEmployeeUsage {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "drying_session_id")
    private DryingSession dryingSession;

    public DryingSessionEmployeeUsage() {
    }

    public DryingSessionEmployeeUsage(Long id, double usageTime, Employee employee, DryingSession dryingSession) {
        super(id, usageTime, employee);
        this.dryingSession = dryingSession;
    }

    public DryingSession getDryingSession() {
        return dryingSession;
    }

    public void setDryingSession(DryingSession dryingSession) {
        this.dryingSession = dryingSession;
    }
}
