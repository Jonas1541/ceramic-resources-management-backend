package com.jonasdurau.ceramicmanagement.product.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.bisquefiring.BisqueFiring;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransaction;
import com.jonasdurau.ceramicmanagement.glazefiring.GlazeFiring;
import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.product.transaction.employeeusage.ProductTransactionEmployeeUsage;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductOutgoingReason;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;
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
@Table(name = "tb_product_transaction")
public class ProductTransaction extends BaseEntity {

    private String unitName;

    private Instant outgoingAt;

    @Enumerated(EnumType.STRING)
    private ProductState state;

    @Enumerated(EnumType.STRING)
    private ProductOutgoingReason outgoingReason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(optional = true, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "glaze_transaction_id")
    private GlazeTransaction glazeTransaction;

    @ManyToOne(optional = true)
    @JoinColumn(name = "bisque_firing_id")
    private BisqueFiring bisqueFiring;

    @ManyToOne(optional = true)
    @JoinColumn(name = "glaze_firing_id")
    private GlazeFiring glazeFiring;

    @OneToMany(mappedBy = "productTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTransactionEmployeeUsage> employeeUsages = new ArrayList<>();

    private BigDecimal cost;

    public ProductTransaction() {
    }

    public BigDecimal getBisqueFiringCost() {
        if (bisqueFiring != null && bisqueFiring.getBiscuits() != null && !bisqueFiring.getBiscuits().isEmpty()) {
            return bisqueFiring.getCostAtTime()
                    .divide(BigDecimal.valueOf(bisqueFiring.getBiscuits().size()), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getGlazeFiringCost() {
        if (glazeFiring != null && glazeFiring.getGlosts() != null && !glazeFiring.getGlosts().isEmpty()) {
            return glazeFiring.getCostAtTime()
                    .divide(BigDecimal.valueOf(glazeFiring.getGlosts().size()), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getGlazeTransactionCost() {
        return (glazeTransaction != null) ? glazeTransaction.getGlazeFinalCostAtTime() : BigDecimal.ZERO;
    }

    public BigDecimal getTotalCost() {
        return cost
                .add(getBisqueFiringCost())
                .add(getGlazeFiringCost())
                .add(getGlazeTransactionCost());
    }

    public BigDecimal getProfit() {
        BigDecimal profit;
        if (outgoingReason == ProductOutgoingReason.SOLD) {
            profit = product.getPrice();
        } else {
            profit = BigDecimal.valueOf(0);
        }
        return profit;
    }

    public BigDecimal getTotalEmployeeCost() {
        return employeeUsages.stream().map(ProductTransactionEmployeeUsage::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Instant getOutgoingAt() {
        return outgoingAt;
    }

    public void setOutgoingAt(Instant outgoingAt) {
        this.outgoingAt = outgoingAt;
    }

    public ProductState getState() {
        return state;
    }

    public void setState(ProductState state) {
        this.state = state;
    }

    public ProductOutgoingReason getOutgoingReason() {
        return outgoingReason;
    }

    public void setOutgoingReason(ProductOutgoingReason outgoingReason) {
        this.outgoingReason = outgoingReason;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public GlazeTransaction getGlazeTransaction() {
        return glazeTransaction;
    }

    public void setGlazeTransaction(GlazeTransaction glazeTransaction) {
        this.glazeTransaction = glazeTransaction;
        if (glazeTransaction != null && glazeTransaction.getProductTransaction() != this) {
            // Garante que o outro lado da relação seja definido
            glazeTransaction.setProductTransaction(this);
        }
    }

    public BisqueFiring getBisqueFiring() {
        return bisqueFiring;
    }

    public void setBisqueFiring(BisqueFiring bisqueFiring) {
        this.bisqueFiring = bisqueFiring;
    }

    public GlazeFiring getGlazeFiring() {
        return glazeFiring;
    }

    public void setGlazeFiring(GlazeFiring glazeFiring) {
        this.glazeFiring = glazeFiring;
    }

    public List<ProductTransactionEmployeeUsage> getEmployeeUsages() {
        return employeeUsages;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
