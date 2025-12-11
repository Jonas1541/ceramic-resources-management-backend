package com.jonasdurau.ceramicmanagement.product.line;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.product.line.dto.ProductLineRequestDTO;
import com.jonasdurau.ceramicmanagement.product.line.dto.ProductLineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/product-lines")
public class ProductLineController extends IndependentController<ProductLineResponseDTO, ProductLineRequestDTO, ProductLineResponseDTO, Long, ProductLineService>{
}
