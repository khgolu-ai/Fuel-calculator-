package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.Trip
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FuelRepository private constructor(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "fuel_tracker.db"
    )
        .fallbackToDestructiveMigration(true)
        .build()

    val vehicleDao = db.vehicleDao()
    val tripDao = db.tripDao()

    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun ensureDefaultVehicleCreated() {
        val vehicles = allVehicles.firstOrNull()
        if (vehicles.isNullOrEmpty()) {
            val scorpio = Vehicle(
                name = "Mahindra Scorpio",
                registrationNumber = "MP XX AB 1234",
                fuelType = "Diesel",
                tankCapacity = 60.0,
                defaultKpl = 12.0,
                notes = "Primary family SUV",
                isDefault = true
            )
            vehicleDao.insertVehicle(scorpio)
        }
    }

    suspend fun insertVehicle(vehicle: Vehicle): Long {
        if (vehicle.isDefault) {
            vehicleDao.clearDefaultVehicles()
        }
        return vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        if (vehicle.isDefault) {
            vehicleDao.clearDefaultVehicles()
        }
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun setDefaultVehicle(id: Long) {
        vehicleDao.clearDefaultVehicles()
        vehicleDao.setDefaultVehicle(id)
    }

    suspend fun insertTrip(trip: Trip): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun getLastTripForVehicle(vehicleId: Long): Trip? {
        return tripDao.getLastTripForVehicle(vehicleId)
    }

    companion object {
        @Volatile
        private var INSTANCE: FuelRepository? = null

        fun getInstance(context: Context): FuelRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FuelRepository(context).also { INSTANCE = it }
            }
        }
    }
}
