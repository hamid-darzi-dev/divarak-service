package com.divarak.application.service.product

import java.time.Instant
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val createdAt: Instant,
)
