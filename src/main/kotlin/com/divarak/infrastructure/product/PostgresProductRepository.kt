package com.divarak.infrastructure.product

import com.company.element.divarak.infrastructure.persistence.postgres.jooq.generated.tables.references.PRODUCT
import com.divarak.domain.product.Product
import com.divarak.domain.product.ProductRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class PostgresProductRepository(
    private val dslContext: DSLContext,
    private val productMapper: ProductMapper,
) : ProductRepository {
    override fun add(product: Product) {
        val record = productMapper.map(product)
        dslContext
            .insertInto(PRODUCT)
            .set(record)
            .execute()
    }

    override fun getAll(): List<Product> {
        val records = dslContext.selectFrom(PRODUCT)
        return records.map {
            productMapper.map(it)
        }
    }
}
