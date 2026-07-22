package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.FuelDetailsCard
import com.example.ui.components.FuelFilledSection
import com.example.ui.components.KmDetailsCard
import com.example.ui.components.TripSummaryCard
import com.example.ui.components.VehicleSelectorHeader
import com.example.ui.components.WarningBanner
import com.example.ui.viewmodel.FuelTrackerViewModel

@Composable
fun DashboardScreen(
    viewModel: FuelTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val calcResult = viewModel.calculationResult

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = "Fuel Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Offline vehicle fuel & kilometer management",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Selected Vehicle Header
        VehicleSelectorHeader(
            selectedVehicle = viewModel.selectedVehicle,
            previousTrip = viewModel.previousTripAvailable,
            onChangeVehicleClick = { viewModel.showVehicleSelectorDialog = true },
            onUsePreviousTripClick = { viewModel.applyPreviousTripData() }
        )

        // Kilometer Details
        KmDetailsCard(
            startingKm = viewModel.startingKmInput,
            endingKm = viewModel.endingKmInput,
            distanceTravelled = calcResult.distanceTravelled,
            onStartingKmChange = { viewModel.startingKmInput = it },
            onEndingKmChange = { viewModel.endingKmInput = it }
        )

        // Fuel Details
        FuelDetailsCard(
            kpl = viewModel.kplInput,
            tankCapacity = viewModel.tankCapacityInput,
            startingFuel = viewModel.startingFuelInput,
            onKplChange = { viewModel.kplInput = it },
            onTankCapacityChange = { viewModel.tankCapacityInput = it },
            onStartingFuelChange = { viewModel.startingFuelInput = it }
        )

        // Fuel Filled During Journey
        FuelFilledSection(
            fuelEntries = viewModel.fuelEntries,
            onAddFuelClick = {
                viewModel.fuelEntryToEdit = null
                viewModel.showFuelEntryDialog = true
            },
            onEditFuelClick = { entry ->
                viewModel.fuelEntryToEdit = entry
                viewModel.showFuelEntryDialog = true
            },
            onDeleteFuelClick = { entry ->
                viewModel.removeFuelEntry(entry)
            }
        )

        // Validation / Warning Banner
        WarningBanner(
            errors = calcResult.errors,
            warnings = calcResult.warnings
        )

        // Result Card (Trip Summary)
        TripSummaryCard(
            result = calcResult,
            currencySymbol = viewModel.settings.currencySymbol
        )

        // Trip Notes Input
        OutlinedTextField(
            value = viewModel.tripNotes,
            onValueChange = { viewModel.tripNotes = it },
            label = { Text("Trip Notes (Optional)") },
            placeholder = { Text("e.g. City drive to office & highway run") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Save Trip Button
        Button(
            onClick = { viewModel.saveCurrentTrip() },
            enabled = calcResult.isValid,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("save_trip_button")
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Trip",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "SAVE TRIP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
