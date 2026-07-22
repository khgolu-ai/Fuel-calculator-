package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FuelEntry

@Composable
fun FuelEntryDialog(
    fuelEntry: FuelEntry?,
    onDismiss: () -> Unit,
    onSave: (FuelEntry) -> Unit
) {
    var quantityText by remember { mutableStateOf(fuelEntry?.quantityLitres?.toString() ?: "") }
    var odometerText by remember { mutableStateOf(fuelEntry?.odometerKm?.toString() ?: "") }
    var priceText by remember { mutableStateOf(fuelEntry?.pricePerLitre?.toString() ?: "") }
    var totalPaidText by remember { mutableStateOf(fuelEntry?.totalAmountPaid?.toString() ?: "") }
    var locationText by remember { mutableStateOf(fuelEntry?.stationLocation ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (fuelEntry == null) "Add Fuel Entry" else "Edit Fuel Entry",
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
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        // Auto calculate total paid if price per litre exists
                        val q = it.toDoubleOrNull()
                        val p = priceText.toDoubleOrNull()
                        if (q != null && p != null) {
                            totalPaidText = (q * p).toString()
                        }
                    },
                    label = { Text("Fuel Quantity (Litres) *") },
                    placeholder = { Text("e.g. 15") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it },
                    label = { Text("Odometer / KM Reading (Optional)") },
                    placeholder = { Text("e.g. 25120") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            val q = quantityText.toDoubleOrNull()
                            val p = it.toDoubleOrNull()
                            if (q != null && p != null) {
                                totalPaidText = (q * p).toString()
                            }
                        },
                        label = { Text("Price/L (Optional)") },
                        placeholder = { Text("e.g. 95") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = totalPaidText,
                        onValueChange = { totalPaidText = it },
                        label = { Text("Total Paid (Optional)") },
                        placeholder = { Text("e.g. 1425") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Petrol Pump / Location (Optional)") },
                    placeholder = { Text("e.g. HP Fuel Station") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull()
                    if (qty == null || qty <= 0) {
                        errorMessage = "Please enter a valid fuel quantity > 0"
                        return@Button
                    }

                    val newEntry = FuelEntry(
                        id = fuelEntry?.id ?: java.util.UUID.randomUUID().toString(),
                        quantityLitres = qty,
                        odometerKm = odometerText.toDoubleOrNull(),
                        pricePerLitre = priceText.toDoubleOrNull(),
                        totalAmountPaid = totalPaidText.toDoubleOrNull(),
                        stationLocation = locationText.ifBlank { null },
                        dateTime = fuelEntry?.dateTime ?: System.currentTimeMillis()
                    )
                    onSave(newEntry)
                }
            ) {
                Text(if (fuelEntry == null) "Add Fuel" else "Save Changes")
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
