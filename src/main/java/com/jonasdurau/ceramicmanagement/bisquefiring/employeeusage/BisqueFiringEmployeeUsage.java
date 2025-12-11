package com.jonasdurau.ceramicmanagement.bisquefiring.employeeusage;

import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiring;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEmployeeUsage;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_bisque_firing_employee_usage")
public class BisqueFiringEmployeeUsage extends BaseEmployeeUsage {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "bisque_firing_id")
    private BisqueFiring bisqueFiring;

    public BisqueFiringEmployeeUsage() {
    }

    public BisqueFiringEmployeeUsage(Long id, double usageTime, Employee employee, BisqueFiring bisqueFiring) {
        super(id, usageTime, employee);
        this.bisqueFiring = bisqueFiring;
    }

    public BisqueFiring getBisqueFiring() {
        return bisqueFiring;
    }

    public void setBisqueFiring(BisqueFiring bisqueFiring) {
        this.bisqueFiring = bisqueFiring;
    }
}
