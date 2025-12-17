package com.jonasdurau.ceramicmanagement.glaze.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.glaze.transaction.dto.GlazeTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.transaction.dto.GlazeTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentController;

@RestController
@RequestMapping("/api/glazes/{parentId}/transactions")
public class GlazeTransactionController extends DependentController<GlazeTransactionResponseDTO, GlazeTransactionRequestDTO, GlazeTransactionResponseDTO, Long, GlazeTransactionService> {

    @Autowired
    public GlazeTransactionController(GlazeTransactionService service) {
        super(service);
    }
}
