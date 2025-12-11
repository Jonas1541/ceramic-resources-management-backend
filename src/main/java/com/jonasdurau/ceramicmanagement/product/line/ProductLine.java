package com.jonasdurau.ceramicmanagement.product.line;

import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_product_line")
public class ProductLine extends BaseEntity {

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Product> products = new ArrayList<>();

    public ProductLine() {
    }

    public int getProductQuantity() {
        return products.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
