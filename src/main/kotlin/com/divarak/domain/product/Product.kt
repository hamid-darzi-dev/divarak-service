package com.divarak.domain.product

import java.time.Instant
import java.util.UUID

data class Product(
    val id: UUID,
    val name: String,
    val description: String,
    val createdAt: Instant,
)
