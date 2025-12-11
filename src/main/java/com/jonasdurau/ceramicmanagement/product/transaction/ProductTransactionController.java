package com.jonasdurau.ceramicmanagement.product.transaction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.product.transaction.dto.ProductTransactionRequestDTO;
import com.jonasdurau.ceramicmanagement.product.transaction.dto.ProductTransactionResponseDTO;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductOutgoingReason;
import com.jonasdurau.ceramicmanagement.product.transaction.enums.ProductState;

@RestController
@RequestMapping("/api/products/{productId}/transactions")
public class ProductTransactionController {

    @Autowired
    private ProductTransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<ProductTransactionResponseDTO>> findAllByProduct(@PathVariable Long productId) {
        List<ProductTransactionResponseDTO> list = transactionService.findAllByProduct(productId);
        return ResponseEntity.ok(list);
    }

    @GetMapping(params = "state")
    public ResponseEntity<List<ProductTransactionResponseDTO>> findAllByState(@RequestParam ProductState state) {
        List<ProductTransactionResponseDTO> list = transactionService.findAllByState(state);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ProductTransactionResponseDTO> findById(@PathVariable Long productId, @PathVariable Long transactionId) {
        ProductTransactionResponseDTO dto = transactionService.findById(productId, transactionId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<List<ProductTransactionResponseDTO>> create(@PathVariable Long productId, @RequestParam int quantity, @RequestBody ProductTransactionRequestDTO dto) {
        List<ProductTransactionResponseDTO> created = transactionService.create(productId, quantity, dto);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId, @PathVariable Long transactionId) {
        transactionService.delete(productId, transactionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{transactionId}", params = "outgoingReason")
    public ResponseEntity<ProductTransactionResponseDTO> outgoing(@PathVariable Long productId, @PathVariable Long transactionId, @RequestParam ProductOutgoingReason outgoingReason) {
        ProductTransactionResponseDTO dto = transactionService.outgoing(productId, transactionId, outgoingReason);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/outgoing-by-quantity")
    public ResponseEntity<List<ProductTransactionResponseDTO>> outgoingByQuantity(@PathVariable Long productId, @RequestParam int quantity, @RequestParam ProductState state, @RequestParam ProductOutgoingReason outgoingReason) {
        List<ProductTransactionResponseDTO> list = transactionService.outgoingByQuantity(productId, quantity, state, outgoingReason);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<ProductTransactionResponseDTO> cancelOutgoing(@PathVariable Long productId, @PathVariable Long transactionId) {
        ProductTransactionResponseDTO dto = transactionService.cancelOutgoing(productId, transactionId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/cancel-outgoing-by-quantity")
    public ResponseEntity<List<ProductTransactionResponseDTO>> cancelOutgoingByQuantity(@PathVariable Long productId, @RequestParam int quantity, @RequestParam ProductState state) {
        List<ProductTransactionResponseDTO> list = transactionService.cancelOutgoingByQuantity(productId, quantity, state);
        return ResponseEntity.ok(list);
    }
}
