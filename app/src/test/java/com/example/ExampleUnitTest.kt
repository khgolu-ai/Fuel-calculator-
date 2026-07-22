package com.example

import com.example.calculation.FuelCalculator
import com.example.data.model.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testScenario1() {
        // TEST 1
        // Starting KM = 1000, Ending KM = 1300, KPL = 10, Starting Fuel = 20 L, Fuel Filled = 20 L, Tank Capacity = 50 L
        // Expected: Distance = 300 km, Fuel Consumed = 30 L, Fuel Available = 10 L, Fuel Needed = 40 L
        val result = FuelCalculator.calculate(
            startingKm = 1000.0,
            endingKm = 1300.0,
            kpl = 10.0,
            tankCapacity = 50.0,
            startingFuel = 20.0,
            fuelEntries = listOf(FuelEntry(quantityLitres = 20.0))
        )

        assertEquals(300.0, result.distanceTravelled, 0.001)
        assertEquals(30.0, result.fuelConsumed, 0.001)
        assertEquals(10.0, result.endingFuelAvailable, 0.001)
        assertEquals(40.0, result.fuelNeededForFullTank, 0.001)
    }

    @Test
    fun testScenario2() {
        // TEST 2
        // Starting KM = 5000, Ending KM = 5250, KPL = 12.5, Starting Fuel = 30 L, Fuel Filled = 0, Tank Capacity = 50 L
        // Expected: Distance = 250 km, Fuel Consumed = 20 L, Fuel Available = 10 L, Fuel Needed = 40 L
        val result = FuelCalculator.calculate(
            startingKm = 5000.0,
            endingKm = 5250.0,
            kpl = 12.5,
            tankCapacity = 50.0,
            startingFuel = 30.0,
            fuelEntries = emptyList()
        )

        assertEquals(250.0, result.distanceTravelled, 0.001)
        assertEquals(20.0, result.fuelConsumed, 0.001)
        assertEquals(10.0, result.endingFuelAvailable, 0.001)
        assertEquals(40.0, result.fuelNeededForFullTank, 0.001)
    }

    @Test
    fun testScenario3() {
        // TEST 3
        // Starting KM = 10000, Ending KM = 10450, KPL = 15, Starting Fuel = 20 L, Fuel Filled = 15 L, Tank Capacity = 50 L
        // Expected: Distance = 450 km, Fuel Consumed = 30 L, Fuel Available = 5 L, Fuel Needed = 45 L
        val result = FuelCalculator.calculate(
            startingKm = 10000.0,
            endingKm = 10450.0,
            kpl = 15.0,
            tankCapacity = 50.0,
            startingFuel = 20.0,
            fuelEntries = listOf(FuelEntry(quantityLitres = 15.0))
        )

        assertEquals(450.0, result.distanceTravelled, 0.001)
        assertEquals(30.0, result.fuelConsumed, 0.001)
        assertEquals(5.0, result.endingFuelAvailable, 0.001)
        assertEquals(45.0, result.fuelNeededForFullTank, 0.001)
    }
}
