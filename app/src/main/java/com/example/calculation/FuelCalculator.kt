package com.example.calculation

import com.example.data.model.FuelEntry
import java.util.Locale

data class CalculationResult(
    val startingKm: Double,
    val endingKm: Double,
    val distanceTravelled: Double,
    val kpl: Double,
    val tankCapacity: Double,
    val startingFuel: Double,
    val totalFuelFilled: Double,
    val totalFuelCost: Double,
    val fuelConsumed: Double,
    val endingFuelAvailable: Double,
    val fuelNeededForFullTank: Double,
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

object FuelCalculator {

    fun calculate(
        startingKm: Double?,
        endingKm: Double?,
        kpl: Double?,
        tankCapacity: Double?,
        startingFuel: Double?,
        fuelEntries: List<FuelEntry>
    ): CalculationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val start = startingKm ?: 0.0
        val end = endingKm ?: 0.0
        val efficiency = kpl ?: 0.0
        val capacity = tankCapacity ?: 0.0
        val startFuel = startingFuel ?: 0.0

        val distance = (end - start).coerceAtLeast(0.0)
        val totalFuelFilled = fuelEntries.sumOf { it.quantityLitres }
        val totalFuelCost = fuelEntries.sumOf { entry ->
            entry.totalAmountPaid ?: ((entry.pricePerLitre ?: 0.0) * entry.quantityLitres)
        }

        // 1. Validations
        if (startingKm != null && endingKm != null && end < start) {
            errors.add("Ending KM cannot be less than Starting KM.")
        }

        if (efficiency <= 0.0) {
            errors.add("KPL must be greater than zero.")
        }

        if (capacity <= 0.0) {
            errors.add("Tank capacity must be greater than zero.")
        }

        if (startFuel < 0.0) {
            errors.add("Starting fuel balance cannot be negative.")
        }

        // 2. Core Calculations (Full floating point precision)
        val fuelConsumed = if (efficiency > 0.0) distance / efficiency else 0.0
        val totalFuelBeforeUsage = startFuel + totalFuelFilled
        val endingFuelAvailable = startFuel + totalFuelFilled - fuelConsumed
        val fuelNeededForFullTank = (capacity - endingFuelAvailable).coerceAtLeast(0.0)

        // 3. Warnings
        if (startFuel > capacity && capacity > 0.0) {
            warnings.add("Starting fuel balance exceeds tank capacity.")
        }

        if (fuelConsumed > totalFuelBeforeUsage && totalFuelBeforeUsage > 0.0) {
            warnings.add("Calculated fuel usage exceeds the available fuel. Please verify KPL, kilometer readings, or fuel entries.")
        }

        if (endingFuelAvailable > capacity && capacity > 0.0) {
            warnings.add("Fuel balance exceeds tank capacity. Please verify fuel entries.")
        }

        if (endingFuelAvailable < 0.0) {
            warnings.add("Calculated fuel balance is negative (${formatTwoDecimals(endingFuelAvailable)} L). Please verify fuel entries or starting balance.")
        }

        val isValid = errors.isEmpty()

        return CalculationResult(
            startingKm = start,
            endingKm = end,
            distanceTravelled = distance,
            kpl = efficiency,
            tankCapacity = capacity,
            startingFuel = startFuel,
            totalFuelFilled = totalFuelFilled,
            totalFuelCost = totalFuelCost,
            fuelConsumed = fuelConsumed,
            endingFuelAvailable = endingFuelAvailable,
            fuelNeededForFullTank = fuelNeededForFullTank,
            isValid = isValid,
            errors = errors,
            warnings = warnings
        )
    }

    fun formatTwoDecimals(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", value.toLong())
        } else {
            String.format(Locale.US, "%,.2f", value)
        }
    }
}
