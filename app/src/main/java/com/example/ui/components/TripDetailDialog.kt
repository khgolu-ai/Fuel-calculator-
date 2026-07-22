package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calculation.FuelCalculator
import com.example.data.model.Trip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripDetailDialog(
    trip: Trip,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onDeleteClick: (Trip) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trip.dateTime))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Trip Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vehicle Info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Vehicle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = trip.vehicleName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = trip.vehicleRegistration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Odometer & Distance
                DetailCard(title = "Kilometers") {
                    DetailRow("Starting KM", "${FuelCalculator.formatNumber(trip.startingKm)} km")
                    DetailRow("Ending KM", "${FuelCalculator.formatNumber(trip.endingKm)} km")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetailRow("Distance Travelled", "${FuelCalculator.formatNumber(trip.distanceTravelled)} km", isBold = true)
                }

                // Fuel Metrics
                DetailCard(title = "Fuel Metrics") {
                    DetailRow("KPL / Mileage", "${FuelCalculator.formatNumber(trip.kpl)} km/L")
                    DetailRow("Tank Capacity", "${FuelCalculator.formatNumber(trip.tankCapacity)} L")
                    DetailRow("Starting Fuel", "${FuelCalculator.formatTwoDecimals(trip.startingFuelBalance)} L")
                    DetailRow("Total Fuel Filled", "${FuelCalculator.formatTwoDecimals(trip.totalFuelFilled)} L")
                    DetailRow("Calculated Fuel Consumed", "${FuelCalculator.formatTwoDecimals(trip.fuelConsumed)} L")
                    DetailRow("Ending Fuel Available", "${FuelCalculator.formatTwoDecimals(trip.endingFuelAvailable)} L")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetailRow(
                        "Fuel Required for Full Tank",
                        "${FuelCalculator.formatTwoDecimals(trip.fuelNeededForFullTank)} L",
                        isHighlight = true
                    )
                }

                // Fuel Entries List
                if (trip.fuelEntries.isNotEmpty()) {
                    DetailCard(title = "Fuel Entries (${trip.fuelEntries.size})") {
                        trip.fuelEntries.forEachIndexed { idx, entry ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "Entry #${idx + 1}: ${FuelCalculator.formatTwoDecimals(entry.quantityLitres)} Litres",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val details = mutableListOf<String>()
                                if (entry.odometerKm != null) details.add("KM: ${FuelCalculator.formatNumber(entry.odometerKm)}")
                                if (entry.pricePerLitre != null) details.add("$currencySymbol${entry.pricePerLitre}/L")
                                if (entry.totalAmountPaid != null) details.add("Paid: $currencySymbol${entry.totalAmountPaid}")
                                if (!entry.stationLocation.isNullOrBlank()) details.add(entry.stationLocation)

                                if (details.isNotEmpty()) {
                                    Text(
                                        text = details.joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (idx < trip.fuelEntries.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                if (trip.totalFuelCost > 0) {
                    DetailRow("Total Expense", "$currencySymbol${FuelCalculator.formatTwoDecimals(trip.totalFuelCost)}", isHighlight = true)
                }

                if (trip.notes.isNotBlank()) {
                    DetailCard(title = "Notes") {
                        Text(
                            text = trip.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { onDeleteClick(trip) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
