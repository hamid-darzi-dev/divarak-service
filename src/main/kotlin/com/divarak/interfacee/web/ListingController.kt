package com.divarak.interfacee.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/listings")
class ListingController {
    private val listings =
        listOf(
            Listing(1, "Car"),
            Listing(2, "Apartment"),
            Listing(4, "TV"),
        )

    private val listingsMap = listings.associateBy { it.id }

    @GetMapping("/{id}")
    fun getListing(
        @PathVariable id: Int,
    ): ResponseEntity<Listing> {
        val listing = listingsMap[id]
        return if (listing != null) {
            ResponseEntity.ok(listing)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
