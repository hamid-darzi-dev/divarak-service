package com.divarak.infrastructure.product

import com.company.element.divarak.infrastructure.persistence.postgres.jooq.generated.tables.records.ProductRecord
import com.divarak.domain.product.Product
import org.springframework.stereotype.Component

@Component
class ProductMapper {
    fun map(product: Product): ProductRecord =
        with(product) {
            ProductRecord(
                id = id,
                name = name,
                description = description,
                createdAt = createdAt,
            )
        }

    fun map(productRecord: ProductRecord): Product =
        with(productRecord) {
            Product(
                id = id,
                name = name,
                description = description,
                createdAt = createdAt,
            )
        }
}
