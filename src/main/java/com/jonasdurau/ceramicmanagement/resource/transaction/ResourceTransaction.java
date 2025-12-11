package com.jonasdurau.ceramicmanagement.resource.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.jonasdurau.ceramicmanagement.batch.Batch;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransaction;
import com.jonasdurau.ceramicmanagement.resource.Resource;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_resource_transaction")
public class ResourceTransaction extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private double quantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(optional = true)
    @JoinColumn(name = "batch_id", nullable = true)
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "glaze_transaction_id", nullable = true)
    private GlazeTransaction glazeTransaction;

    private BigDecimal costAtTime;

    public ResourceTransaction() {
    }

    public BigDecimal getCost() {
        return resource.getUnitValue()
                       .multiply(BigDecimal.valueOf(quantity))
                       .setScale(2, RoundingMode.HALF_UP);
        }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public GlazeTransaction getGlazeTransaction() {
        return glazeTransaction;
    }

    public void setGlazeTransaction(GlazeTransaction glazeTransaction) {
        this.glazeTransaction = glazeTransaction;
    }

    public BigDecimal getCostAtTime() {
        return costAtTime;
    }

    public void setCostAtTime(BigDecimal costAtTime) {
        this.costAtTime = costAtTime;
    }
}
