package com.jonasdurau.ceramicmanagement.product.employeeusage;

import com.jonasdurau.ceramicmanagement.employee.Employee;
import com.jonasdurau.ceramicmanagement.product.Product;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEmployeeUsage;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_product_employee_usage")
public class ProductEmployeeUsage extends BaseEmployeeUsage {

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductEmployeeUsage() {
    }

    public ProductEmployeeUsage(Long id, double usageTime, Employee employee, Product product) {
        super(id, usageTime, employee);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
