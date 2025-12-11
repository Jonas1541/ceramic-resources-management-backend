package com.jonasdurau.ceramicmanagement.dryingroom;

import java.util.ArrayList;
import java.util.List;

import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSession;
import com.jonasdurau.ceramicmanagement.machine.Machine;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_drying_room")
public class DryingRoom extends BaseEntity {
    
    private String name;
    private double gasConsumptionPerHour;

    @ManyToMany
    @JoinTable(
        name = "tb_drying_room_machine",
        joinColumns = @JoinColumn(name = "drying_room_id"),
        inverseJoinColumns = @JoinColumn(name = "machine_id")
    )
    private List<Machine> machines = new ArrayList<>();

    @OneToMany(mappedBy = "dryingRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DryingSession> sessions = new ArrayList<>();

    public DryingRoom() {
    }

    public int getUses() {
        return sessions.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGasConsumptionPerHour() {
        return gasConsumptionPerHour;
    }

    public void setGasConsumptionPerHour(double gasConsumptionPerHour) {
        this.gasConsumptionPerHour = gasConsumptionPerHour;
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public List<DryingSession> getSessions() {
        return sessions;
    }
}
