package com.example.data.model

data class AppSettings(
    val distanceUnit: String = "KM",
    val fuelUnit: String = "Litres",
    val currencySymbol: String = "₹",
    val decimalPrecision: Int = 2
)
