package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculation.CalculationResult
import com.example.calculation.FuelCalculator
import com.example.data.model.AppSettings
import com.example.data.model.FuelEntry
import com.example.data.model.Trip
import com.example.data.model.Vehicle
import com.example.data.repository.FuelRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FuelTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FuelRepository.getInstance(application)

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trips: StateFlow<List<Trip>> = repository.allTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var selectedVehicle by mutableStateOf<Vehicle?>(null)
        private set

    // Calculator inputs
    var startingKmInput by mutableStateOf("25000")
    var endingKmInput by mutableStateOf("25450")
    var kplInput by mutableStateOf("15")
    var tankCapacityInput by mutableStateOf("50")
    var startingFuelInput by mutableStateOf("20")
    var fuelEntries by mutableStateOf<List<FuelEntry>>(
        listOf(
            FuelEntry(quantityLitres = 15.0, odometerKm = 25150.0, pricePerLitre = 95.0, totalAmountPaid = 1425.0, stationLocation = "Shell Fuel Stop")
        )
    )
    var tripNotes by mutableStateOf("")

    // Last trip offer indicator
    var previousTripAvailable by mutableStateOf<Trip?>(null)
        private set

    // Dialog & UI States
    var showVehicleSelectorDialog by mutableStateOf(false)
    var showVehicleEditDialog by mutableStateOf(false)
    var vehicleToEdit by mutableStateOf<Vehicle?>(null)

    var showFuelEntryDialog by mutableStateOf(false)
    var fuelEntryToEdit by mutableStateOf<FuelEntry?>(null)

    var showTripDetailDialog by mutableStateOf(false)
    var selectedTripForDetail by mutableStateOf<Trip?>(null)

    var showTripEditDialog by mutableStateOf(false)
    var tripToEdit by mutableStateOf<Trip?>(null)

    var tripToDeleteConfirm by mutableStateOf<Trip?>(null)
    var vehicleToDeleteConfirm by mutableStateOf<Vehicle?>(null)

    // App Settings
    var settings by mutableStateOf(AppSettings())
        private set

    private val _userEvent = MutableSharedFlow<String>()
    val userEvent: SharedFlow<String> = _userEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultVehicleCreated()
        }

        viewModelScope.launch {
            vehicles.collectLatest { list ->
                if (selectedVehicle == null && list.isNotEmpty()) {
                    val defaultVeh = list.find { it.isDefault } ?: list.first()
                    selectVehicle(defaultVeh)
                } else if (selectedVehicle != null) {
                    // Refresh selected vehicle if modified
                    list.find { it.id == selectedVehicle?.id }?.let { updated ->
                        selectedVehicle = updated
                    }
                }
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        selectedVehicle = vehicle
        kplInput = FuelCalculator.formatNumber(vehicle.defaultKpl).replace(",", "")
        tankCapacityInput = FuelCalculator.formatNumber(vehicle.tankCapacity).replace(",", "")

        // Check for previous trip
        viewModelScope.launch {
            val lastTrip = repository.getLastTripForVehicle(vehicle.id)
            previousTripAvailable = lastTrip
        }
    }

    fun applyPreviousTripData() {
        val lastTrip = previousTripAvailable ?: return
        startingKmInput = FuelCalculator.formatNumber(lastTrip.endingKm).replace(",", "")
        startingFuelInput = FuelCalculator.formatTwoDecimals(lastTrip.endingFuelAvailable)
        viewModelScope.launch {
            _userEvent.emit("Updated Starting KM (${lastTrip.endingKm}) and Fuel (${FuelCalculator.formatTwoDecimals(lastTrip.endingFuelAvailable)} L) from previous trip.")
        }
    }

    val calculationResult: CalculationResult
        get() {
            val startKm = startingKmInput.toDoubleOrNull()
            val endKm = endingKmInput.toDoubleOrNull()
            val kpl = kplInput.toDoubleOrNull()
            val capacity = tankCapacityInput.toDoubleOrNull()
            val startFuel = startingFuelInput.toDoubleOrNull()

            return FuelCalculator.calculate(
                startingKm = startKm,
                endingKm = endKm,
                kpl = kpl,
                tankCapacity = capacity,
                startingFuel = startFuel,
                fuelEntries = fuelEntries
            )
        }

    fun addOrUpdateFuelEntry(entry: FuelEntry) {
        val current = fuelEntries.toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            current[index] = entry
        } else {
            current.add(entry)
        }
        fuelEntries = current
        showFuelEntryDialog = false
        fuelEntryToEdit = null
    }

    fun removeFuelEntry(entry: FuelEntry) {
        fuelEntries = fuelEntries.filter { it.id != entry.id }
    }

    fun saveCurrentTrip() {
        val veh = selectedVehicle
        if (veh == null) {
            viewModelScope.launch { _userEvent.emit("Please select a vehicle first.") }
            return
        }

        val result = calculationResult
        if (!result.isValid) {
            viewModelScope.launch { _userEvent.emit(result.errors.firstOrNull() ?: "Invalid inputs") }
            return
        }

        val trip = Trip(
            vehicleId = veh.id,
            vehicleName = veh.name,
            vehicleRegistration = veh.registrationNumber,
            startingKm = result.startingKm,
            endingKm = result.endingKm,
            distanceTravelled = result.distanceTravelled,
            kpl = result.kpl,
            tankCapacity = result.tankCapacity,
            startingFuelBalance = result.startingFuel,
            fuelEntries = fuelEntries,
            totalFuelFilled = result.totalFuelFilled,
            fuelConsumed = result.fuelConsumed,
            endingFuelAvailable = result.endingFuelAvailable,
            fuelNeededForFullTank = result.fuelNeededForFullTank,
            totalFuelCost = result.totalFuelCost,
            notes = tripNotes
        )

        viewModelScope.launch {
            repository.insertTrip(trip)
            _userEvent.emit("Trip saved successfully!")

            // Reset inputs for next trip
            startingKmInput = FuelCalculator.formatNumber(result.endingKm).replace(",", "")
            endingKmInput = ""
            startingFuelInput = FuelCalculator.formatTwoDecimals(result.endingFuelAvailable)
            fuelEntries = emptyList()
            tripNotes = ""

            // Refresh last trip offer
            previousTripAvailable = repository.getLastTripForVehicle(veh.id)
        }
    }

    fun saveVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            if (vehicle.id == 0L) {
                repository.insertVehicle(vehicle)
                _userEvent.emit("Vehicle added: ${vehicle.name}")
            } else {
                repository.updateVehicle(vehicle)
                _userEvent.emit("Vehicle updated: ${vehicle.name}")
            }
            showVehicleEditDialog = false
            vehicleToEdit = null
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            _userEvent.emit("Vehicle deleted: ${vehicle.name}")
            vehicleToDeleteConfirm = null
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
            _userEvent.emit("Trip deleted.")
            tripToDeleteConfirm = null
            if (selectedTripForDetail?.id == trip.id) {
                showTripDetailDialog = false
                selectedTripForDetail = null
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        viewModelScope.launch { _userEvent.emit("Settings updated.") }
    }
}
