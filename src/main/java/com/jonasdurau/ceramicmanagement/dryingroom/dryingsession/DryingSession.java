package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.dryingroom.DryingRoom;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.employeeusage.DryingSessionEmployeeUsage;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_drying_session")
public class DryingSession extends BaseEntity {

    private double hours;

    @ManyToOne(optional = false)
    @JoinColumn(name = "drying_room_id")
    private DryingRoom dryingRoom;

    @OneToMany(mappedBy = "dryingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DryingSessionEmployeeUsage> employeeUsages = new ArrayList<>();

    private BigDecimal costAtTime;

    public DryingSession() {
    }

    public BigDecimal calculateEmployeeTotalCost() {
        return employeeUsages.stream()
                .map(DryingSessionEmployeeUsage::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getEnergyConsumption() {
        double totalKW = dryingRoom.getMachines().stream().mapToDouble(Machine::getPower).sum() * 0.74;
        return totalKW * hours;
    }

    public double getGasConsumption() {
        return dryingRoom.getGasConsumptionPerHour() * hours;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public DryingRoom getDryingRoom() {
        return dryingRoom;
    }

    public void setDryingRoom(DryingRoom dryingRoom) {
        this.dryingRoom = dryingRoom;
    }

    public List<DryingSessionEmployeeUsage> getEmployeeUsages() {
        return employeeUsages;
    }

    public BigDecimal getCostAtTime() {
        return costAtTime;
    }

    public void setCostAtTime(BigDecimal costAtTime) {
        this.costAtTime = costAtTime;
    }
}
