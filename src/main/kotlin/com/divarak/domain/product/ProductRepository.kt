package com.divarak.domain.product

interface ProductRepository {
    fun add(product: Product)

    fun getAll(): List<Product>
}
