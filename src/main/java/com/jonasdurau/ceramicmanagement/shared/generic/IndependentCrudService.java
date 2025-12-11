package com.jonasdurau.ceramicmanagement.shared.generic;

import java.util.List;

public interface IndependentCrudService<LIST_DTO, REQ_DTO, RES_DTO, ID> {
    List<LIST_DTO> findAll();
    RES_DTO findById(ID id);
    RES_DTO create(REQ_DTO dto);
    RES_DTO update(ID id, REQ_DTO dto);
    void delete(ID id);
}
