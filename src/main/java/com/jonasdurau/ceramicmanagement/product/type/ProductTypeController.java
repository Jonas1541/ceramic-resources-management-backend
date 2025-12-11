package com.jonasdurau.ceramicmanagement.product.type;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.product.type.dto.ProductTypeRequestDTO;
import com.jonasdurau.ceramicmanagement.product.type.dto.ProductTypeResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController extends IndependentController<ProductTypeResponseDTO, ProductTypeRequestDTO, ProductTypeResponseDTO, Long, ProductTypeService>{
}
