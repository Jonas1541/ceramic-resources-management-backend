package com.jonasdurau.ceramicmanagement.glaze.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_glaze_transaction")
public class GlazeTransaction extends BaseEntity {

    private double quantity;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "glaze_id")
    private Glaze glaze;

    @OneToOne(mappedBy = "glazeTransaction")
    private ProductTransaction productTransaction;

    private BigDecimal resourceTotalCostAtTime;
    private BigDecimal machineEnergyConsumptionCostAtTime;
    private BigDecimal employeeTotalCostAtTime;
    private BigDecimal glazeFinalCostAtTime;

    @OneToMany(mappedBy = "glazeTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTransaction> resourceTransactions = new ArrayList<>();

    public GlazeTransaction() {
    }

    public BigDecimal getCost() {
        return glaze.getUnitCost()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Glaze getGlaze() {
        return glaze;
    }

    public void setGlaze(Glaze glaze) {
        this.glaze = glaze;
    }

    public ProductTransaction getProductTransaction() {
        return productTransaction;
    }

    public void setProductTransaction(ProductTransaction productTransaction) {
        this.productTransaction = productTransaction;
    }

    public BigDecimal getResourceTotalCostAtTime() {
        return resourceTotalCostAtTime;
    }

    public void setResourceTotalCostAtTime(BigDecimal resourceTotalCostAtTime) {
        this.resourceTotalCostAtTime = resourceTotalCostAtTime;
    }

    public BigDecimal getMachineEnergyConsumptionCostAtTime() {
        return machineEnergyConsumptionCostAtTime;
    }

    public void setMachineEnergyConsumptionCostAtTime(BigDecimal machineEnergyConsumptionCostAtTime) {
        this.machineEnergyConsumptionCostAtTime = machineEnergyConsumptionCostAtTime;
    }

    public BigDecimal getEmployeeTotalCostAtTime() {
        return employeeTotalCostAtTime;
    }

    public void setEmployeeTotalCostAtTime(BigDecimal employeeTotalCostAtTime) {
        this.employeeTotalCostAtTime = employeeTotalCostAtTime;
    }

    public BigDecimal getGlazeFinalCostAtTime() {
        return glazeFinalCostAtTime;
    }

    public void setGlazeFinalCostAtTime(BigDecimal glazeFinalCostAtTime) {
        this.glazeFinalCostAtTime = glazeFinalCostAtTime;
    }

    public List<ResourceTransaction> getResourceTransactions() {
        return resourceTransactions;
    }
}
