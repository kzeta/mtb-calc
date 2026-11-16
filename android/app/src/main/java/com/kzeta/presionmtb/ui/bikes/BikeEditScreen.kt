package com.kzeta.presionmtb.ui.bikes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kzeta.presionmtb.PresionApp
import com.kzeta.presionmtb.domain.AppTime
import com.kzeta.presionmtb.domain.Bike
import com.kzeta.presionmtb.domain.Casing
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.domain.TireSystem
import com.kzeta.presionmtb.domain.WheelSetup
import com.kzeta.presionmtb.domain.WheelSize
import com.kzeta.presionmtb.domain.parseDecimal
import com.kzeta.presionmtb.ui.components.LimeButton
import com.kzeta.presionmtb.ui.components.NumberField
import com.kzeta.presionmtb.ui.components.SectionLabel
import com.kzeta.presionmtb.ui.components.Segmented
import com.kzeta.presionmtb.ui.theme.LocalPalette
import com.kzeta.presionmtb.ui.theme.Manrope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WheelForm(
    val width: String = "2,4",
    val rim: String = "30",
    val maxPsi: String = "",
    val system: TireSystem = TireSystem.TUBELESS,
    val casing: Casing = Casing.TRAIL,
)

data class BikeForm(
    val name: String = "",
    val wheelSize: WheelSize = WheelSize.IN_29,
    val weight: String = "12",
    val front: WheelForm = WheelForm(),
    val rear: WheelForm = WheelForm(),
    val error: String? = null,
    val done: Boolean = false,
)

class BikeEditViewModel(
    private val app: PresionApp,
    private val bikeId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(BikeForm())
    val state: StateFlow<BikeForm> = _state.asStateFlow()
    private var createdAt: String? = null

    init {
        if (bikeId != "new") {
            viewModelScope.launch {
                app.library.getBike(bikeId)?.let { bike ->
                    createdAt = bike.createdAt
                    _state.value = BikeForm(
                        name = bike.name,
                        wheelSize = bike.wheelSize,
                        weight = PressureCalculator.formatKg(bike.weightKg),
                        front = bike.front.toForm(),
                        rear = bike.rear.toForm(),
                    )
                }
            }
        }
    }

    fun onName(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onSize(value: WheelSize) = _state.update { it.copy(wheelSize = value) }
    fun onWeight(value: String) = _state.update { it.copy(weight = value, error = null) }
    fun onFront(value: WheelForm) = _state.update { it.copy(front = value, error = null) }
    fun onRear(value: WheelForm) = _state.update { it.copy(rear = value, error = null) }

    fun save() {
        val form = _state.value
        val bike = form.toBike(bikeId, createdAt)
        if (bike == null) {
            _state.update { it.copy(error = "Revisa el nombre, el peso y las medidas de cada rueda.") }
            return
        }
        viewModelScope.launch {
            app.library.saveBike(bike)
            _state.update { it.copy(done = true) }
        }
    }

    fun delete() {
        if (bikeId == "new") return
        viewModelScope.launch {
            val bike = app.library.getBike(bikeId) ?: return@launch
            app.library.softDeleteBike(bike)
            _state.update { it.copy(done = true) }
        }
    }

    companion object {
        fun factory(app: PresionApp, bikeId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BikeEditViewModel(app, bikeId) as T
        }
    }
}

private fun WheelSetup.toForm() = WheelForm(
    width = widthIn.toString().replace('.', ','),
    rim = if (rimInternalMm % 1.0 == 0.0) rimInternalMm.toInt().toString() else rimInternalMm.toString().replace('.', ','),
    maxPsi = maxPsi?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString().replace('.', ',') } ?: "",
    system = system,
    casing = casing,
)

private fun WheelForm.toSetup(): WheelSetup? {
    val width = parseDecimal(width) ?: return null
    val rim = parseDecimal(rim) ?: return null
    val max = maxPsi.trim().takeIf { it.isNotEmpty() }?.let { parseDecimal(it) ?: return null }
    if (width !in 1.5..4.0 || rim !in 15.0..50.0) return null
    if (max != null && max !in 8.0..80.0) return null
    return WheelSetup(width, rim, system, casing, max)
}

private fun BikeForm.toBike(bikeId: String, createdAt: String?): Bike? {
    val weightKg = parseDecimal(weight) ?: return null
    if (name.isBlank() || weightKg !in 4.0..45.0) return null
    val frontSetup = front.toSetup() ?: return null
    val rearSetup = rear.toSetup() ?: return null
    val now = AppTime.now()
    return Bike(
        id = if (bikeId == "new") AppTime.newId() else bikeId,
        name = name.trim(),
        wheelSize = wheelSize,
        weightKg = weightKg,
        front = frontSetup,
        rear = rearSetup,
        createdAt = createdAt ?: now,
        updatedAt = now,
        deletedAt = null,
    )
}

@Composable
fun BikeEditScreen(app: PresionApp, bikeId: String, onDone: () -> Unit) {
    val vm: BikeEditViewModel = viewModel(
        key = bikeId,
        factory = BikeEditViewModel.factory(app, bikeId),
    )
    val state by vm.state.collectAsState()
    val palette = LocalPalette.current
    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (bikeId == "new") "Nueva bici" else "Editar bici", fontFamily = Manrope, fontWeight = FontWeight.Bold, color = palette.ink)
        NumberField("Nombre", state.name, vm::onName)
        SectionLabel("Medida de rueda")
        Segmented(WheelSize.entries, state.wheelSize, { it.label }, vm::onSize)
        NumberField("Peso de la bici", state.weight, vm::onWeight, "kg")
        WheelEditor("Rueda delantera", state.front, vm::onFront)
        WheelEditor("Rueda trasera", state.rear, vm::onRear)
        if (state.error != null) Text(state.error ?: "", color = palette.accent)
        LimeButton("Guardar bici", vm::save)
        if (bikeId != "new") {
            TextButton(onClick = vm::delete, modifier = Modifier.padding(bottom = 24.dp)) {
                Text("Eliminar bici", color = palette.accent)
            }
        }
    }
}

@Composable
private fun WheelEditor(title: String, form: WheelForm, onChange: (WheelForm) -> Unit) {
    SectionLabel(title)
    NumberField("Ancho de cubierta", form.width, { onChange(form.copy(width = it)) }, "pulgadas")
    NumberField("Ancho interno de llanta", form.rim, { onChange(form.copy(rim = it)) }, "mm")
    NumberField("Máximo de la cubierta", form.maxPsi, { onChange(form.copy(maxPsi = it)) }, "psi")
    SectionLabel("Montaje")
    Segmented(TireSystem.entries, form.system, { it.label }) { onChange(form.copy(system = it)) }
    SectionLabel("Carcasa")
    Segmented(Casing.entries, form.casing, { it.label }) { onChange(form.copy(casing = it)) }
}
