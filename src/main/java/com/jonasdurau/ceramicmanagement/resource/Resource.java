package com.jonasdurau.ceramicmanagement.resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransaction;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_resource")
public class Resource extends BaseEntity {

    @Column(unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private ResourceCategory category;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitValue;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTransaction> transactions = new ArrayList<>();

    public Resource() {
    }

    public double getCurrentQuantity() {
        double total = 0.0;
        for (ResourceTransaction tx : transactions) {
            if (tx.getType() == TransactionType.INCOMING) {
                total += tx.getQuantity();
            } else {
                total -= tx.getQuantity();
            }
        }
        return total;
    }

    public BigDecimal getCurrentQuantityPrice() {
        return BigDecimal.valueOf(getCurrentQuantity())
                .multiply(this.unitValue)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
    }

    public BigDecimal getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(BigDecimal unitValue) {
        this.unitValue = unitValue;
    }

    public List<ResourceTransaction> getTransactions() {
        return transactions;
    }
}
