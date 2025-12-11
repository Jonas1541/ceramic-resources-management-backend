package com.jonasdurau.ceramicmanagement.batch.employeeusage;

import com.jonasdurau.ceramicmanagement.batch.Batch;
import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEmployeeUsage;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_batch_employee_usage")
public class BatchEmployeeUsage extends BaseEmployeeUsage {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    public BatchEmployeeUsage() {
    }

    public BatchEmployeeUsage(Long id, double usageTime, Employee employee, Batch batch) {
        super(id, usageTime, employee);
        this.batch = batch;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}
