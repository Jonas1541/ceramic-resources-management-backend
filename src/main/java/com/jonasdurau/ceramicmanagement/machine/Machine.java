package com.jonasdurau.ceramicmanagement.machine;

import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_machine")
public class Machine extends BaseEntity {

    @Column(unique = true)
    private String name;
    private double power;

    public Machine() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }
}
