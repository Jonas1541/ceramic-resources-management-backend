package com.jonasdurau.ceramicmanagement.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.product.line.ProductLine;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransaction;
import com.jonasdurau.ceramicmanagement.product.type.ProductType;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_product")
public class Product extends BaseEntity {

    private String name;
    private BigDecimal price;
    private double height;
    private double length;
    private double width;
    private double glazeQuantityPerUnit;
    private double weight;
    private long unitCounter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_type_id")
    private ProductType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_line_id")
    private ProductLine line;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTransaction> transactions = new ArrayList<>();

    public Product() {
    }

    public int getProductStock() {
        int stock = 0;
        for(ProductTransaction tx : transactions) {
            if(tx.getOutgoingReason() == null) {
                stock++;
            }
        }
        return stock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getglazeQuantityPerUnit() {
        return glazeQuantityPerUnit;
    }

    public void setglazeQuantityPerUnit(double glazeQuantityPerUnit) {
        this.glazeQuantityPerUnit = glazeQuantityPerUnit;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public ProductLine getLine() {
        return line;
    }

    public void setLine(ProductLine line) {
        this.line = line;
    }

    public List<ProductTransaction> getTransactions() {
        return transactions;
    }

    public long getUnitCounter() {
        return unitCounter;
    }

    public void setUnitCounter(long unitCounter) {
        this.unitCounter = unitCounter;
    }
}
