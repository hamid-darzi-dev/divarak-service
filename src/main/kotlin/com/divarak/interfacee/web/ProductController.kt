package com.divarak.interfacee.web

import com.divarak.application.service.product.ProductApplicationService
import com.divarak.application.service.product.ProductRequest
import com.divarak.application.service.product.ProductResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val service: ProductApplicationService,
) {
    @PostMapping("/add")
    fun addProduct(
        @RequestBody request: ProductRequest,
    ) {
        service.addProduct(request)
    }

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductResponse>> {
        val productsResponses = service.getAllProducts()
        return ResponseEntity.ok().body(productsResponses)
    }
}
