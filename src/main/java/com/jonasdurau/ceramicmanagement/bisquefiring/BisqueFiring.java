package com.jonasdurau.ceramicmanagement.bisquefiring;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.bisquefiring.employeeusage.BisqueFiringEmployeeUsage;
import com.jonasdurau.ceramicmanagement.kiln.Kiln;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_bisque_firing")
public class BisqueFiring extends BaseEntity {

    private double temperature;
    private double burnTime;
    private double coolingTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kiln_id")
    private Kiln kiln;

    @OneToMany(mappedBy = "bisqueFiring")
    private List<ProductTransaction> biscuits = new ArrayList<>();

    @OneToMany(mappedBy = "bisqueFiring", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BisqueFiringEmployeeUsage> employeeUsages = new ArrayList<>();

    private BigDecimal costAtTime;

    public BisqueFiring() {
    }

    public BigDecimal calculateEmployeeTotalCost() {
        return employeeUsages.stream()
                .map(BisqueFiringEmployeeUsage::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getEnergyConsumption() {
        return (kiln.getPower() * 0.74) * (burnTime + coolingTime);
    }

    public double getGasConsumption() {
        return burnTime * kiln.getGasConsumptionPerHour();
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(double burnTime) {
        this.burnTime = burnTime;
    }

    public double getCoolingTime() {
        return coolingTime;
    }

    public void setCoolingTime(double coolingTime) {
        this.coolingTime = coolingTime;
    }

    public Kiln getKiln() {
        return kiln;
    }

    public void setKiln(Kiln kiln) {
        this.kiln = kiln;
    }

    public List<ProductTransaction> getBiscuits() {
        return biscuits;
    }

    public List<BisqueFiringEmployeeUsage> getEmployeeUsages() {
        return employeeUsages;
    }

    public BigDecimal getCostAtTime() {
        return costAtTime;
    }

    public void setCostAtTime(BigDecimal costAtTime) {
        this.costAtTime = costAtTime;
    }
}
