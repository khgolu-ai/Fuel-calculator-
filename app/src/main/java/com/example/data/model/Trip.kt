package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val vehicleName: String,
    val vehicleRegistration: String,
    val startingKm: Double,
    val endingKm: Double,
    val distanceTravelled: Double,
    val kpl: Double,
    val tankCapacity: Double,
    val startingFuelBalance: Double,
    val fuelEntries: List<FuelEntry> = emptyList(),
    val totalFuelFilled: Double,
    val fuelConsumed: Double,
    val endingFuelAvailable: Double,
    val fuelNeededForFullTank: Double,
    val totalFuelCost: Double = 0.0,
    val dateTime: Long = System.currentTimeMillis(),
    val notes: String = ""
)
