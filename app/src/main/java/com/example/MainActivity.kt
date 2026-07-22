package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FuelEntryDialog
import com.example.ui.components.TripDetailDialog
import com.example.ui.components.VehicleDialog
import com.example.ui.components.VehicleSelectorDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VehiclesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FuelTrackerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.LocalGasStation, Icons.Outlined.LocalGasStation)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Vehicles : Screen("vehicles", "Vehicles", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FuelTrackerApp()
            }
        }
    }
}

@Composable
fun FuelTrackerApp(
    viewModel: FuelTrackerViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val vehicles by viewModel.vehicles.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.userEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav")) {
                val screens = listOf(
                    Screen.Dashboard,
                    Screen.History,
                    Screen.Vehicles,
                    Screen.Settings
                )
                screens.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Dashboard -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.History -> HistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Vehicles -> VehiclesScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.Settings -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // Global Dialogs

        // 1. Vehicle Selector Dialog
        if (viewModel.showVehicleSelectorDialog) {
            VehicleSelectorDialog(
                vehicles = vehicles,
                selectedVehicleId = viewModel.selectedVehicle?.id,
                onSelectVehicle = { veh ->
                    viewModel.selectVehicle(veh)
                    viewModel.showVehicleSelectorDialog = false
                },
                onAddNewVehicleClick = {
                    viewModel.showVehicleSelectorDialog = false
                    viewModel.vehicleToEdit = null
                    viewModel.showVehicleEditDialog = true
                },
                onDismiss = { viewModel.showVehicleSelectorDialog = false }
            )
        }

        // 2. Add / Edit Vehicle Dialog
        if (viewModel.showVehicleEditDialog) {
            VehicleDialog(
                vehicle = viewModel.vehicleToEdit,
                onDismiss = {
                    viewModel.showVehicleEditDialog = false
                    viewModel.vehicleToEdit = null
                },
                onSave = { veh ->
                    viewModel.saveVehicle(veh)
                }
            )
        }

        // 3. Add / Edit Fuel Entry Dialog
        if (viewModel.showFuelEntryDialog) {
            FuelEntryDialog(
                fuelEntry = viewModel.fuelEntryToEdit,
                onDismiss = {
                    viewModel.showFuelEntryDialog = false
                    viewModel.fuelEntryToEdit = null
                },
                onSave = { entry ->
                    viewModel.addOrUpdateFuelEntry(entry)
                }
            )
        }

        // 4. Trip Detail Dialog
        if (viewModel.showTripDetailDialog && viewModel.selectedTripForDetail != null) {
            TripDetailDialog(
                trip = viewModel.selectedTripForDetail!!,
                currencySymbol = viewModel.settings.currencySymbol,
                onDismiss = {
                    viewModel.showTripDetailDialog = false
                    viewModel.selectedTripForDetail = null
                },
                onDeleteClick = { trip ->
                    viewModel.tripToDeleteConfirm = trip
                }
            )
        }

        // 5. Delete Trip Confirmation Dialog
        if (viewModel.tripToDeleteConfirm != null) {
            AlertDialog(
                onDismissRequest = { viewModel.tripToDeleteConfirm = null },
                title = { Text("Delete Trip", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this trip record? This action cannot be undone.") },
                confirmButton = {
                    Button(onClick = { viewModel.deleteTrip(viewModel.tripToDeleteConfirm!!) }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.tripToDeleteConfirm = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 6. Delete Vehicle Confirmation Dialog
        if (viewModel.vehicleToDeleteConfirm != null) {
            AlertDialog(
                onDismissRequest = { viewModel.vehicleToDeleteConfirm = null },
                title = { Text("Delete Vehicle", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete vehicle '${viewModel.vehicleToDeleteConfirm?.name}'?") },
                confirmButton = {
                    Button(onClick = { viewModel.deleteVehicle(viewModel.vehicleToDeleteConfirm!!) }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.vehicleToDeleteConfirm = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
