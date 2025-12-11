package com.jonasdurau.ceramicmanagement.glaze.machineusage;

import com.jonasdurau.ceramicmanagement.glaze.Glaze;
import com.jonasdurau.ceramicmanagement.machine.Machine;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_glaze_machine_usage")
public class GlazeMachineUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double usageTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "glaze_id")
    private Glaze glaze;

    @ManyToOne(optional = false)
    @JoinColumn(name = "machine_id")
    private Machine machine;

    public GlazeMachineUsage() {
    }

    public double getEnergyConsumption() {
        double power = machine.getPower() * 0.74;
        return power * usageTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(double usageTime) {
        this.usageTime = usageTime;
    }

    public Glaze getGlaze() {
        return glaze;
    }

    public void setGlaze(Glaze glaze) {
        this.glaze = glaze;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GlazeMachineUsage other = (GlazeMachineUsage) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
