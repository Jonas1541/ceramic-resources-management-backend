package com.jonasdurau.ceramicmanagement.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.batch.employeeusage.BatchEmployeeUsage;
import com.jonasdurau.ceramicmanagement.batch.machineusage.BatchMachineUsage;
import com.jonasdurau.ceramicmanagement.batch.resourceusage.BatchResourceUsage;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_batch")
public class Batch extends BaseEntity {

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchResourceUsage> resourceUsages = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchMachineUsage> machineUsages = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchEmployeeUsage> employeeUsages = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTransaction> resourceTransactions = new ArrayList<>();

    private BigDecimal batchTotalWaterCostAtTime;
    private BigDecimal resourceTotalCostAtTime;
    private BigDecimal machinesEnergyConsumptionCostAtTime;
    private BigDecimal employeeTotalCostAtTime;
    private BigDecimal batchFinalCostAtTime;

    private double weight;

    public Batch() {
    }

    public BigDecimal calculateEmployeeTotalCost() {
        return employeeUsages.stream()
                .map(BatchEmployeeUsage::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getBatchTotalWater() {
        return resourceUsages.stream()
                .mapToDouble(BatchResourceUsage::getTotalWater)
                .sum();
    }

    public double calculateResourceTotalQuantity() {
        return resourceUsages.stream()
                .mapToDouble(BatchResourceUsage::getTotalQuantity)
                .sum();
    }

    public BigDecimal getResourceTotalCost() {
        return resourceUsages.stream()
                .map(BatchResourceUsage::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getMachinesEnergyConsumption() {
        return machineUsages.stream()
                .mapToDouble(BatchMachineUsage::getEnergyConsumption)
                .sum();
    }

    public List<BatchResourceUsage> getResourceUsages() {
        return resourceUsages;
    }

    public List<BatchMachineUsage> getMachineUsages() {
        return machineUsages;
    }

    public List<BatchEmployeeUsage> getEmployeeUsages() {
        return employeeUsages;
    }

    public BigDecimal getEmployeeTotalCostAtTime() {
        return employeeTotalCostAtTime;
    }

    public void setEmployeeTotalCostAtTime(BigDecimal employeeTotalCostAtTime) {
        this.employeeTotalCostAtTime = employeeTotalCostAtTime;
    }

    public List<ResourceTransaction> getResourceTransactions() {
        return resourceTransactions;
    }

    public BigDecimal getBatchTotalWaterCostAtTime() {
        return batchTotalWaterCostAtTime;
    }

    public void setBatchTotalWaterCostAtTime(BigDecimal batchTotalWaterCostAtTime) {
        this.batchTotalWaterCostAtTime = batchTotalWaterCostAtTime;
    }

    public BigDecimal getResourceTotalCostAtTime() {
        return resourceTotalCostAtTime;
    }

    public void setResourceTotalCostAtTime(BigDecimal resourceTotalCostAtTime) {
        this.resourceTotalCostAtTime = resourceTotalCostAtTime;
    }

    public BigDecimal getMachinesEnergyConsumptionCostAtTime() {
        return machinesEnergyConsumptionCostAtTime;
    }

    public void setMachinesEnergyConsumptionCostAtTime(BigDecimal machinesEnergyConsumptionCostAtTime) {
        this.machinesEnergyConsumptionCostAtTime = machinesEnergyConsumptionCostAtTime;
    }

    public BigDecimal getBatchFinalCostAtTime() {
        return batchFinalCostAtTime;
    }

    public void setBatchFinalCostAtTime(BigDecimal batchFinalCostAtTime) {
        this.batchFinalCostAtTime = batchFinalCostAtTime;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
