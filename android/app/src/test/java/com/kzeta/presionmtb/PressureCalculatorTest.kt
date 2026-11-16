package com.kzeta.presionmtb

import com.kzeta.presionmtb.domain.Casing
import com.kzeta.presionmtb.domain.Discipline
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.domain.PressureInput
import com.kzeta.presionmtb.domain.Terrain
import com.kzeta.presionmtb.domain.TireSystem
import com.kzeta.presionmtb.domain.WheelPosition
import com.kzeta.presionmtb.domain.WheelSetup
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PressureCalculatorTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun referenceFrontAndRear() {
        val front = PressureCalculator.recommend(reference(WheelPosition.FRONT))
        val rear = PressureCalculator.recommend(reference(WheelPosition.REAR))
        assertEquals(21.5, front.psi, 0.0001)
        assertEquals(24.0, rear.psi, 0.0001)
        assertFalse(front.raisedToMinimum)
        assertFalse(front.limitedByMax)
    }

    @Test
    fun tubeRimAndSidewall() {
        val tube = PressureCalculator.recommend(reference(WheelPosition.REAR).let {
            it.copy(wheel = it.wheel.copy(system = TireSystem.TUBE))
        })
        assertEquals(28.0, tube.psi, 0.0001)
        val rim = PressureCalculator.recommend(reference(WheelPosition.FRONT).let {
            it.copy(wheel = it.wheel.copy(rimInternalMm = 25.0))
        })
        assertEquals(22.25, rim.psi, 0.0001)
        val sidewall = PressureCalculator.recommend(reference(WheelPosition.REAR).let {
            it.copy(wheel = it.wheel.copy(maxPsi = 20.0))
        })
        assertEquals(20.0, sidewall.psi, 0.0001)
        assertTrue(sidewall.limitedByMax)
    }

    @Test
    fun garageKeepsPressureWhenTemperaturesMatch() {
        assertEquals(24.0, PressureCalculator.garagePsi(24.0, 20.0, 20.0), 0.0001)
    }

    @Test
    fun fixturesMatchTheSharedOracle() {
        val raw = checkNotNull(javaClass.classLoader).getResourceAsStream("pressure-cases.json")?.use { it.readBytes() }
            ?: error("Falta pressure-cases.json")
        val file = json.decodeFromString(FixtureFile.serializer(), raw.toString(Charsets.UTF_8))
        file.cases.forEach { item ->
            val advice = PressureCalculator.recommend(item.toInput())
            assertEquals(item.name, item.expectedPsi, advice.psi, 0.0001)
            assertEquals(item.name, item.raisedToMinimum, advice.raisedToMinimum)
            assertEquals(item.name, item.limitedByMax, advice.limitedByMax)
        }
        file.garage.forEach { item ->
            val psi = PressureCalculator.garagePsi(item.trailPsi, item.trailTempC, item.garageTempC)
            assertEquals(item.name, item.expectedPsi, psi, 0.0001)
        }
        file.rounding.forEach { item ->
            assertEquals(item.displayPsi, PressureCalculator.roundPsi(item.psi), 0.0001)
            assertEquals(item.displayBar, PressureCalculator.roundBar(item.psi), 0.0001)
        }
    }

    private fun reference(position: WheelPosition) = PressureInput(
        riderWeightKg = 75.0,
        bikeWeightKg = 12.0,
        wheel = WheelSetup(2.4, 30.0, TireSystem.TUBELESS, Casing.TRAIL, null),
        position = position,
        discipline = Discipline.TRAIL,
        terrain = Terrain.MIXED,
    )
}

@Serializable
private data class FixtureFile(
    val cases: List<FixtureCase>,
    val garage: List<GarageFixture>,
    val rounding: List<RoundingFixture>,
)

@Serializable
private data class FixtureCase(
    val name: String,
    val riderWeightKg: Double,
    val bikeWeightKg: Double,
    val widthIn: Double,
    val rimInternalMm: Double,
    val system: String,
    val casing: String,
    val discipline: String,
    val terrain: String,
    val maxPsi: Double? = null,
    val position: String,
    val expectedPsi: Double,
    val raisedToMinimum: Boolean,
    val limitedByMax: Boolean,
)

@Serializable
private data class GarageFixture(
    val name: String,
    val trailPsi: Double,
    val trailTempC: Double,
    val garageTempC: Double,
    val expectedPsi: Double,
)

@Serializable
private data class RoundingFixture(
    val psi: Double,
    val displayPsi: Double,
    val displayBar: Double,
)

private fun FixtureCase.toInput() = PressureInput(
    riderWeightKg = riderWeightKg,
    bikeWeightKg = bikeWeightKg,
    wheel = WheelSetup(
        widthIn = widthIn,
        rimInternalMm = rimInternalMm,
        system = TireSystem.fromWire(system),
        casing = Casing.fromWire(casing),
        maxPsi = maxPsi,
    ),
    position = if (position == "front") WheelPosition.FRONT else WheelPosition.REAR,
    discipline = Discipline.fromWire(discipline),
    terrain = Terrain.fromWire(terrain),
)
