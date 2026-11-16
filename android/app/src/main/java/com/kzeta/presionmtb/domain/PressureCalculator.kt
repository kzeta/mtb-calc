package com.kzeta.presionmtb.domain

import java.util.Locale
import kotlin.math.floor
import kotlin.math.pow

object PressureCalculator {
    const val ATM_PSI = 14.696
    const val PSI_PER_BAR = 14.503773773
    const val REF_RIDER_KG = 75.0
    const val REF_BIKE_KG = 12.0
    const val FRONT_SHARE = 0.42
    const val REAR_SHARE = 0.58
    const val REF_FRONT_PSI = 21.5
    const val REF_REAR_PSI = 24.0
    const val REF_WIDTH_IN = 2.4

    val defaultWheel = WheelSetup(
        widthIn = REF_WIDTH_IN,
        rimInternalMm = 30.0,
        system = TireSystem.TUBELESS,
        casing = Casing.TRAIL,
        maxPsi = null,
    )

    fun recommend(input: PressureInput): WheelAdvice {
        val share = if (input.position == WheelPosition.FRONT) FRONT_SHARE else REAR_SHARE
        val refPsi = if (input.position == WheelPosition.FRONT) REF_FRONT_PSI else REF_REAR_PSI
        val total = input.riderWeightKg + input.bikeWeightKg
        val refTotal = REF_RIDER_KG + REF_BIKE_KG
        val base = refPsi * ((total * share) / (refTotal * share))
        val widthFactor = (REF_WIDTH_IN / input.wheel.widthIn).pow(1.4)
        var psi = base * widthFactor +
            rimAdjust(input.wheel.rimInternalMm) +
            setupAdjust(input.wheel.system) +
            disciplineAdjust(input.discipline) +
            casingAdjust(input.wheel.casing) +
            terrainAdjust(input.terrain)

        val (floorPsi, ceilingPsi) = limits(input.wheel.system)
        var raised = false
        var limited = false
        if (psi < floorPsi) {
            psi = floorPsi
            raised = true
        }
        if (psi > ceilingPsi) {
            psi = ceilingPsi
            limited = true
        }
        val maxPsi = input.wheel.maxPsi
        if (maxPsi != null && psi > maxPsi) {
            psi = maxPsi
            limited = true
            raised = false
        }
        return WheelAdvice(psi, raised, limited)
    }

    fun garagePsi(trailPsi: Double, trailTempC: Double, garageTempC: Double): Double {
        val trailK = trailTempC + 273.15
        val garageK = garageTempC + 273.15
        return (trailPsi + ATM_PSI) * (garageK / trailK) - ATM_PSI
    }

    fun roundPsi(psi: Double): Double = floor(psi * 2.0 + 0.5) / 2.0

    fun roundBar(psi: Double): Double = floor((psi / PSI_PER_BAR) * 20.0 + 0.5) / 20.0

    fun displayRangePsi(advice: WheelAdvice, wheel: WheelSetup): Pair<Double, Double> {
        val center = roundPsi(advice.psi)
        val (floorPsi, ceilingPsi) = limits(wheel.system)
        var low = (center - 1.0).coerceAtLeast(floorPsi)
        var high = (center + 1.0).coerceAtMost(ceilingPsi)
        val maxPsi = wheel.maxPsi
        if (maxPsi != null) {
            low = low.coerceAtMost(maxPsi)
            high = high.coerceAtMost(maxPsi)
        }
        if (high < low) high = low
        return low to high
    }

    fun formatPsi(psi: Double): String {
        val rounded = roundPsi(psi)
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            String.format(Locale("es"), "%.1f", rounded)
        }
    }

    fun formatBar(psi: Double): String =
        String.format(Locale("es"), "%.2f", roundBar(psi))

    fun format(psi: Double, unit: PressureUnit): String =
        if (unit == PressureUnit.PSI) formatPsi(psi) else formatBar(psi)

    fun formatKg(value: Double): String {
        val rounded = floor(value * 10.0 + 0.5) / 10.0
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            String.format(Locale("es"), "%.1f", rounded)
        }
    }

    fun toPsi(value: Double, unit: PressureUnit): Double =
        if (unit == PressureUnit.PSI) value else value * PSI_PER_BAR

    private fun rimAdjust(mm: Double): Double {
        val delta = 30.0 - mm
        return if (delta > 0) delta * 0.15 else delta * 0.08
    }

    private fun setupAdjust(system: TireSystem): Double = when (system) {
        TireSystem.TUBE -> 4.0
        TireSystem.TUBELESS -> 0.0
        TireSystem.INSERT -> -2.5
    }

    private fun disciplineAdjust(discipline: Discipline): Double = when (discipline) {
        Discipline.XC -> 2.0
        Discipline.MARATHON -> 1.5
        Discipline.TRAIL -> 0.0
        Discipline.ENDURO -> -1.5
        Discipline.DH -> -1.0
    }

    private fun casingAdjust(casing: Casing): Double = when (casing) {
        Casing.XC -> 1.5
        Casing.TRAIL -> 0.0
        Casing.ENDURO -> -1.0
        Casing.DH -> -1.5
    }

    private fun terrainAdjust(terrain: Terrain): Double = when (terrain) {
        Terrain.SOFT -> -1.5
        Terrain.MIXED -> 0.0
        Terrain.DRY -> 0.5
        Terrain.ROCK -> 1.5
    }

    private fun limits(system: TireSystem): Pair<Double, Double> = when (system) {
        TireSystem.TUBE -> 18.0 to 45.0
        TireSystem.TUBELESS, TireSystem.INSERT -> 14.0 to 40.0
    }
}

fun parseDecimal(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()
