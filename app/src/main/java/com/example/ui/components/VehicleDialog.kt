package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FuelType
import com.example.data.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (Vehicle) -> Unit
) {
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var registration by remember { mutableStateOf(vehicle?.registrationNumber ?: "") }
    var selectedFuelType by remember { mutableStateOf(vehicle?.fuelType ?: FuelType.DIESEL.displayName) }
    var capacityText by remember { mutableStateOf(vehicle?.tankCapacity?.toString() ?: "50") }
    var kplText by remember { mutableStateOf(vehicle?.defaultKpl?.toString() ?: "15") }
    var notes by remember { mutableStateOf(vehicle?.notes ?: "") }
    var isDefault by remember { mutableStateOf(vehicle?.isDefault ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    val fuelTypes = listOf(
        FuelType.PETROL.displayName,
        FuelType.DIESEL.displayName,
        FuelType.CNG.displayName,
        FuelType.ELECTRIC.displayName,
        FuelType.OTHER.displayName
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (vehicle == null) "Add New Vehicle" else "Edit Vehicle Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vehicle Name *") },
                    placeholder = { Text("e.g. Mahindra Scorpio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = registration,
                    onValueChange = { registration = it },
                    label = { Text("Registration Number *") },
                    placeholder = { Text("e.g. MP XX AB 1234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFuelType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        fuelTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedFuelType = type
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it },
                        label = { Text("Tank Capacity (L) *") },
                        placeholder = { Text("e.g. 60") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = kplText,
                        onValueChange = { kplText = it },
                        label = { Text("Default KPL *") },
                        placeholder = { Text("e.g. 12") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Primary long drive vehicle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDefault = !isDefault }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )
                    Text(
                        text = "Set as default vehicle",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter vehicle name"
                        return@Button
                    }
                    if (registration.isBlank()) {
                        errorMessage = "Please enter registration number"
                        return@Button
                    }
                    val cap = capacityText.toDoubleOrNull()
                    if (cap == null || cap <= 0) {
                        errorMessage = "Tank capacity must be > 0"
                        return@Button
                    }
                    val kpl = kplText.toDoubleOrNull()
                    if (kpl == null || kpl <= 0) {
                        errorMessage = "Default KPL must be > 0"
                        return@Button
                    }

                    val newVeh = Vehicle(
                        id = vehicle?.id ?: 0L,
                        name = name.trim(),
                        registrationNumber = registration.trim().uppercase(),
                        fuelType = selectedFuelType,
                        tankCapacity = cap,
                        defaultKpl = kpl,
                        notes = notes.trim(),
                        isDefault = isDefault
                    )
                    onSave(newVeh)
                }
            ) {
                Text(if (vehicle == null) "Create Vehicle" else "Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
