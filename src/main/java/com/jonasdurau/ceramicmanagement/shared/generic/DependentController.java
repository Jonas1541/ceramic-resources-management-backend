package com.jonasdurau.ceramicmanagement.shared.generic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class DependentController<LIST_DTO, REQ_DTO, RES_DTO, ID, SERVICE extends DependentCrudService<LIST_DTO, REQ_DTO, RES_DTO, ID>> {
    
    @Autowired
    protected SERVICE service;

    @GetMapping
    public ResponseEntity<List<LIST_DTO>> findAllByParentId(@PathVariable ID parentId) {
        return ResponseEntity.ok(service.findAllByParentId(parentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES_DTO> findById(@PathVariable ID parentId, @PathVariable ID id) {
        return ResponseEntity.ok(service.findById(parentId, id));
    }

    @PostMapping
    public ResponseEntity<RES_DTO> create(@PathVariable ID parentId, @Valid @RequestBody REQ_DTO dto) {
        return ResponseEntity.ok(service.create(parentId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RES_DTO> update(@PathVariable ID parentId, @PathVariable ID id, @Valid @RequestBody REQ_DTO dto) {
        return ResponseEntity.ok(service.update(parentId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID parentId, @PathVariable ID id) {
        service.delete(parentId, id);
        return ResponseEntity.noContent().build();
    }
}
