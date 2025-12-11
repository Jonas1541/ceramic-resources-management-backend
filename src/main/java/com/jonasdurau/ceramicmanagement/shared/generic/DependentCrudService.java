package com.jonasdurau.ceramicmanagement.shared.generic;

import java.util.List;

public interface DependentCrudService<LIST_DTO, REQ_DTO, RES_DTO, ID> {
    List<LIST_DTO> findAllByParentId(ID parentId);
    RES_DTO findById(ID parentId, ID id);
    RES_DTO create(ID parentId, REQ_DTO dto);
    RES_DTO update(ID parentId, ID id, REQ_DTO dto);
    void delete(ID parentId, ID id);
}
