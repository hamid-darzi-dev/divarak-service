package com.divarak.application.service.product

import com.divarak.domain.product.Product
import com.divarak.domain.product.ProductRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class ProductApplicationService(
    private val productRepository: ProductRepository,
) {
    fun addProduct(productRequest: ProductRequest) {
        val product =
            with(productRequest) {
                Product(
                    id = UUID.randomUUID(),
                    name = name,
                    description = description,
                    createdAt = Instant.now(),
                )
            }
        productRepository.add(product)
    }

    fun getAllProducts(): List<ProductResponse> {
        val products = productRepository.getAll()
        return products.map {
            ProductResponse(
                id = it.id,
                name = it.name,
                description = it.description,
                createdAt = it.createdAt,
            )
        }
    }
}
