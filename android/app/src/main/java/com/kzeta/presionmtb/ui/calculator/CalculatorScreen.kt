package com.kzeta.presionmtb.ui.calculator

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kzeta.presionmtb.PresionApp
import com.kzeta.presionmtb.domain.Discipline
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.domain.PressureUnit
import com.kzeta.presionmtb.domain.Terrain
import com.kzeta.presionmtb.domain.WheelAdvice
import com.kzeta.presionmtb.domain.WheelSetup
import com.kzeta.presionmtb.domain.parseDecimal
import com.kzeta.presionmtb.ui.components.LimeButton
import com.kzeta.presionmtb.ui.components.NumberField
import com.kzeta.presionmtb.ui.components.PressureCard
import com.kzeta.presionmtb.ui.components.SectionLabel
import com.kzeta.presionmtb.ui.components.Segmented
import com.kzeta.presionmtb.ui.theme.LocalPalette
import com.kzeta.presionmtb.ui.theme.Manrope

@Composable
fun CalculatorScreen(app: PresionApp) {
    val vm: CalculatorViewModel = viewModel(factory = CalculatorViewModel.factory(app))
    val state by vm.state.collectAsState()
    val palette = LocalPalette.current
    val context = LocalContext.current
    var saveOpen by remember { mutableStateOf(false) }
    var frontUsed by remember { mutableStateOf("") }
    var rearUsed by remember { mutableStateOf("") }
    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) vm.loadWeather()
        else vm.showMessage("Puedes escribir la temperatura y el lugar a mano.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Presión de hoy", style = MaterialTheme.typography.headlineMedium, fontFamily = Manrope, fontWeight = FontWeight.Bold, color = palette.ink)
        Text(
            "Un punto de partida para cada rueda. Afínalo de a medio psi en el sendero.",
            color = palette.muted,
            style = MaterialTheme.typography.bodyMedium,
        )
        SectionLabel("Peso de hoy con equipo")
        NumberField("Kilogramos", state.riderWeight, vm::onWeight, "kg")
        SectionLabel("Bici")
        if (state.bikes.isEmpty()) {
            Text(
                "Todavía no hay bicis guardadas. El cálculo usa una 29 de ${PressureCalculator.REF_BIKE_KG.toInt()} kg con cubiertas 2,40\" tubeless.",
                color = palette.muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            val bike = state.bikes.firstOrNull { it.id == state.selectedBikeId } ?: state.bikes.first()
            Segmented(state.bikes, bike, { it.name }, { vm.onBike(it.id) })
            Text(
                "${bike.wheelSize.label} · ${PressureCalculator.formatKg(bike.weightKg)} kg · del. ${bike.front.widthIn}″ ${bike.front.system.label} · tras. ${bike.rear.widthIn}″ ${bike.rear.system.label}",
                color = palette.muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        SectionLabel("Modalidad")
        Segmented(Discipline.entries, state.discipline, { it.label }, vm::onDiscipline)
        SectionLabel("Terreno")
        Segmented(Terrain.entries, state.terrain, { it.label }, vm::onTerrain)
        SectionLabel("Lugar y temperatura")
        NumberField("Lugar", state.placeName, vm::onPlace)
        NumberField("Temperatura", state.temperature, vm::onTemperature, "°C")
        LimeButton(
            text = if (state.loadingWeather) "Buscando temperatura…" else "Usar mi ubicación",
            enabled = !state.loadingWeather,
            onClick = {
                val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (fine || coarse) vm.loadWeather()
                else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
        )
        val advice = state.advice
        if (advice == null) {
            Text("Escribe el peso para ver la presión.", color = palette.muted, modifier = Modifier.padding(top = 12.dp))
        } else {
            val bike = state.bikes.firstOrNull { it.id == state.selectedBikeId }
            val frontWheel = bike?.front ?: PressureCalculator.defaultWheel
            val rearWheel = bike?.rear ?: PressureCalculator.defaultWheel
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    WheelResult("Delantera", advice.front, frontWheel, state.unit)
                }
                Column(Modifier.weight(1f)) {
                    WheelResult("Trasera", advice.rear, rearWheel, state.unit)
                }
            }
            val sameTemp = kotlin.math.abs((parseDecimal(state.temperature) ?: state.garageTempC) - state.garageTempC) < 0.05
            if (!sameTemp) {
                Text(
                    "Si inflas en casa a ${PressureCalculator.formatKg(state.garageTempC)} °C, pon ${PressureCalculator.format(advice.frontGaragePsi, state.unit)} delante y ${PressureCalculator.format(advice.rearGaragePsi, state.unit)} detrás.",
                    color = palette.ink,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            LimeButton("Guardar esta salida", onClick = {
                frontUsed = PressureCalculator.format(advice.front.psi, state.unit)
                rearUsed = PressureCalculator.format(advice.rear.psi, state.unit)
                saveOpen = true
            })
        }
        Text(
            "Respeta el máximo marcado en la cubierta y en la llanta. La estatura y la edad del perfil no cambian esta cifra.",
            color = palette.muted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        if (state.message != null) {
            Text(state.message ?: "", color = palette.accent, fontFamily = Manrope, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = vm::clearMessage) { Text("Ocultar") }
        }
    }

    if (saveOpen) {
        AlertDialog(
            onDismissRequest = { saveOpen = false },
            title = { Text("Guardar salida", fontFamily = Manrope) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Anota la presión que vas a usar. Empieza en la recomendada.")
                    NumberField("Delantera", frontUsed, { frontUsed = it }, state.unit.label)
                    NumberField("Trasera", rearUsed, { rearUsed = it }, state.unit.label)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val front = parseDecimal(frontUsed)?.let { PressureCalculator.toPsi(it, state.unit) }
                    val rear = parseDecimal(rearUsed)?.let { PressureCalculator.toPsi(it, state.unit) }
                    if (front != null && rear != null && front > 0 && rear > 0) {
                        vm.save(front, rear)
                        saveOpen = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { saveOpen = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun WheelResult(title: String, advice: WheelAdvice, wheel: WheelSetup, unit: PressureUnit) {
    val range = PressureCalculator.displayRangePsi(advice, wheel)
    val unitIsPsi = unit == PressureUnit.PSI
    val rangeText = if (unitIsPsi) {
        "${PressureCalculator.formatPsi(range.first)}–${PressureCalculator.formatPsi(range.second)} psi"
    } else {
        "${PressureCalculator.formatBar(range.first)}–${PressureCalculator.formatBar(range.second)} bar"
    }
    val note = when {
        advice.limitedByMax -> "Tope de cubierta o montaje."
        advice.raisedToMinimum -> "Mínimo de este montaje."
        else -> null
    }
    PressureCard(title, advice.psi, unitIsPsi, rangeText, note)
}
