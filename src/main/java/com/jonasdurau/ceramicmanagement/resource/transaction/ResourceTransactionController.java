package com.jonasdurau.ceramicmanagement.resource.transaction;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.resource.transaction.dto.ResourceTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.resource.transaction.dto.ResourceTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentController;

@RestController
@RequestMapping("/api/resources/{parentId}/transactions")
public class ResourceTransactionController extends DependentController<ResourceTransactionResponseDTO, ResourceTransactionRequestDTO, ResourceTransactionResponseDTO, Long, ResourceTransactionService>{
}
