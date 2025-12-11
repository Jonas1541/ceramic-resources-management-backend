package com.jonasdurau.ceramicmanagement.shared.generic;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class IndependentController<LIST_DTO, REQ_DTO, RES_DTO, ID, SERVICE extends IndependentCrudService<LIST_DTO, REQ_DTO, RES_DTO, ID>> {
    
    @Autowired
    protected SERVICE service;

    @GetMapping
    public ResponseEntity<List<LIST_DTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES_DTO> findById(@PathVariable ID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<RES_DTO> create(@Valid @RequestBody REQ_DTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RES_DTO> update(@PathVariable ID id, @Valid @RequestBody REQ_DTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
