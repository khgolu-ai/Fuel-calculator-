package com.example.data.model

data class FuelEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val quantityLitres: Double,
    val odometerKm: Double? = null,
    val dateTime: Long = System.currentTimeMillis(),
    val pricePerLitre: Double? = null,
    val totalAmountPaid: Double? = null,
    val stationLocation: String? = null
)
