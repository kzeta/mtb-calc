package com.kzeta.presionmtb.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.domain.PressureUnit
import com.kzeta.presionmtb.domain.RiderProfile
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

data class ProfileForm(
    val id: String? = null,
    val createdAt: String? = null,
    val name: String = "",
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val garage: String = "20",
    val unit: PressureUnit = PressureUnit.PSI,
    val message: String? = null,
    val error: String? = null,
)

class ProfileViewModel(private val app: PresionApp) : ViewModel() {
    private val _state = MutableStateFlow(ProfileForm())
    val state: StateFlow<ProfileForm> = _state.asStateFlow()

    init {
        viewModelScope.launch { app.library.getProfile()?.let { apply(it) } }
    }

    fun onName(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onWeight(value: String) = _state.update { it.copy(weight = value, error = null) }
    fun onHeight(value: String) = _state.update { it.copy(height = value, error = null) }
    fun onAge(value: String) = _state.update { it.copy(age = value, error = null) }
    fun onGarage(value: String) = _state.update { it.copy(garage = value, error = null) }
    fun onUnit(value: PressureUnit) = _state.update { it.copy(unit = value) }

    fun save() {
        val form = _state.value
        val weight = parseDecimal(form.weight)
        val height = form.height.trim().takeIf { it.isNotEmpty() }?.let { parseDecimal(it) }
        val age = form.age.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        val garage = parseDecimal(form.garage)
        val error = when {
            weight == null || weight !in 20.0..250.0 -> "El peso tiene que estar entre 20 y 250 kg."
            height != null && height !in 100.0..230.0 -> "La estatura tiene que estar entre 100 y 230 cm."
            age != null && age !in 5..100 -> "La edad tiene que estar entre 5 y 100."
            garage == null || garage !in -30.0..50.0 -> "Revisa la temperatura del garaje."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error, message = null) }
            return
        }
        viewModelScope.launch {
            val now = AppTime.now()
            app.library.saveProfile(
                RiderProfile(
                    id = form.id ?: AppTime.newId(),
                    name = form.name.trim(),
                    weightKg = weight!!,
                    heightCm = height ?: 0.0,
                    age = age ?: 0,
                    garageTempC = garage!!,
                    unit = form.unit,
                    createdAt = form.createdAt ?: now,
                    updatedAt = now,
                    deletedAt = null,
                ),
            )
            val saved = app.library.getProfile()
            if (saved != null) apply(saved)
            _state.update { it.copy(message = "Perfil guardado.", error = null) }
        }
    }

    fun export(uri: Uri) {
        viewModelScope.launch {
            try {
                app.library.writeExport(uri)
                _state.update { it.copy(message = "Archivo exportado.", error = null) }
            } catch (error: Exception) {
                _state.update { it.copy(error = error.message ?: "No se pudo exportar.", message = null) }
            }
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            try {
                app.library.importFrom(uri)
                app.library.getProfile()?.let { apply(it) }
                _state.update { it.copy(message = "Datos importados.", error = null) }
            } catch (error: Exception) {
                _state.update { it.copy(error = error.message ?: "No se pudo importar.", message = null) }
            }
        }
    }

    private fun apply(profile: RiderProfile) {
        _state.update {
            it.copy(
                id = profile.id,
                createdAt = profile.createdAt,
                name = profile.name,
                weight = if (profile.weightKg > 0) PressureCalculator.formatKg(profile.weightKg) else "",
                height = if (profile.heightCm > 0) PressureCalculator.formatKg(profile.heightCm) else "",
                age = if (profile.age > 0) profile.age.toString() else "",
                garage = PressureCalculator.formatKg(profile.garageTempC),
                unit = profile.unit,
            )
        }
    }

    companion object {
        fun factory(app: PresionApp) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(app) as T
        }
    }
}

@Composable
fun ProfileScreen(app: PresionApp) {
    val vm: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(app))
    val state by vm.state.collectAsState()
    val palette = LocalPalette.current
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.export(uri)
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.import(uri)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium, fontFamily = Manrope, fontWeight = FontWeight.Bold, color = palette.ink)
        Text(
            "El cálculo usa el peso. La estatura y la edad se guardan con tu perfil.",
            color = palette.muted,
            style = MaterialTheme.typography.bodyMedium,
        )
        NumberField("Nombre", state.name, vm::onName)
        NumberField("Peso con equipo", state.weight, vm::onWeight, "kg")
        NumberField("Estatura", state.height, vm::onHeight, "cm")
        NumberField("Edad", state.age, vm::onAge)
        NumberField("Temperatura del garaje", state.garage, vm::onGarage, "°C")
        SectionLabel("Unidad principal")
        Segmented(PressureUnit.entries, state.unit, { it.label }, vm::onUnit)
        if (state.error != null) Text(state.error ?: "", color = palette.accent)
        if (state.message != null) Text(state.message ?: "", color = palette.accent, fontFamily = Manrope, fontWeight = FontWeight.SemiBold)
        LimeButton("Guardar perfil", vm::save)
        TextButton(onClick = { export.launch("presion-mtb.json") }) { Text("Exportar datos") }
        TextButton(onClick = { import.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.padding(bottom = 24.dp)) {
            Text("Importar datos")
        }
    }
}
