package com.kzeta.presionmtb.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kzeta.presionmtb.PresionApp
import com.kzeta.presionmtb.data.TrailException
import com.kzeta.presionmtb.domain.Bike
import com.kzeta.presionmtb.domain.Discipline
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.domain.PressureInput
import com.kzeta.presionmtb.domain.PressureLog
import com.kzeta.presionmtb.domain.PressureUnit
import com.kzeta.presionmtb.domain.RideAdvice
import com.kzeta.presionmtb.domain.Terrain
import com.kzeta.presionmtb.domain.WheelPosition
import com.kzeta.presionmtb.domain.AppTime
import com.kzeta.presionmtb.domain.parseDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class CalculatorUiState(
    val bikes: List<Bike> = emptyList(),
    val selectedBikeId: String? = null,
    val riderWeight: String = "",
    val weightSeeded: Boolean = false,
    val discipline: Discipline = Discipline.TRAIL,
    val terrain: Terrain = Terrain.MIXED,
    val temperature: String = "20",
    val placeName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val garageTempC: Double = 20.0,
    val unit: PressureUnit = PressureUnit.PSI,
    val loadingWeather: Boolean = false,
    val message: String? = null,
    val advice: RideAdvice? = null,
)

class CalculatorViewModel(
    private val app: PresionApp,
) : ViewModel() {
    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(app.library.observeProfile(), app.library.observeBikes()) { profile, bikes ->
                profile to bikes
            }.collect { (profile, bikes) ->
                _state.update { current ->
                    val selected = when {
                        current.selectedBikeId != null && bikes.any { it.id == current.selectedBikeId } ->
                            current.selectedBikeId
                        else -> bikes.firstOrNull()?.id
                    }
                    var next = current.copy(
                        bikes = bikes,
                        selectedBikeId = selected,
                        garageTempC = profile?.garageTempC ?: 20.0,
                        unit = profile?.unit ?: PressureUnit.PSI,
                    )
                    if (!current.weightSeeded && profile != null && profile.weightKg > 0 && current.riderWeight.isBlank()) {
                        next = next.copy(
                            riderWeight = PressureCalculator.formatKg(profile.weightKg),
                            weightSeeded = true,
                        )
                    }
                    next.copy(advice = compute(next))
                }
            }
        }
    }

    fun onWeight(value: String) = edit { it.copy(riderWeight = value, weightSeeded = true) }
    fun onDiscipline(value: Discipline) = edit { it.copy(discipline = value) }
    fun onTerrain(value: Terrain) = edit { it.copy(terrain = value) }
    fun onTemperature(value: String) = edit { it.copy(temperature = value) }
    fun onPlace(value: String) = edit { it.copy(placeName = value) }
    fun onBike(id: String) = edit { it.copy(selectedBikeId = id) }
    fun clearMessage() = _state.update { it.copy(message = null) }
    fun showMessage(text: String) = _state.update { it.copy(message = text, loadingWeather = false) }

    fun loadWeather() {
        viewModelScope.launch {
            _state.update { it.copy(loadingWeather = true, message = null) }
            try {
                val place = app.conditions.current()
                edit {
                    it.copy(
                        loadingWeather = false,
                        temperature = String.format(Locale("es"), "%.1f", place.temperatureC),
                        placeName = place.name,
                        latitude = place.latitude,
                        longitude = place.longitude,
                    )
                }
            } catch (error: TrailException) {
                _state.update { it.copy(loadingWeather = false, message = error.message) }
            } catch (_: Exception) {
                _state.update {
                    it.copy(loadingWeather = false, message = "No se pudo leer el clima.")
                }
            }
        }
    }

    fun save(frontUsedPsi: Double, rearUsedPsi: Double) {
        val snapshot = _state.value
        val advice = snapshot.advice ?: return
        val rider = parseDecimal(snapshot.riderWeight) ?: return
        val temp = parseDecimal(snapshot.temperature) ?: return
        val bike = snapshot.bikes.firstOrNull { it.id == snapshot.selectedBikeId }
        viewModelScope.launch {
            val now = AppTime.now()
            app.library.saveLog(
                PressureLog(
                    id = AppTime.newId(),
                    bikeId = bike?.id,
                    bikeName = bike?.name ?: "Sin bici guardada",
                    placeName = snapshot.placeName.ifBlank { "Sin lugar" },
                    latitude = snapshot.latitude,
                    longitude = snapshot.longitude,
                    temperatureC = temp,
                    garageTempC = snapshot.garageTempC,
                    riderWeightKg = rider,
                    bikeWeightKg = bike?.weightKg ?: PressureCalculator.REF_BIKE_KG,
                    discipline = snapshot.discipline,
                    terrain = snapshot.terrain,
                    frontRecommendedPsi = advice.front.psi,
                    rearRecommendedPsi = advice.rear.psi,
                    frontUsedPsi = frontUsedPsi,
                    rearUsedPsi = rearUsedPsi,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null,
                ),
            )
            _state.update { it.copy(message = "Salida guardada en el historial.") }
        }
    }

    private fun edit(transform: (CalculatorUiState) -> CalculatorUiState) {
        _state.update { current ->
            val next = transform(current)
            next.copy(advice = compute(next))
        }
    }

    private fun compute(state: CalculatorUiState): RideAdvice? {
        val rider = parseDecimal(state.riderWeight) ?: return null
        val temp = parseDecimal(state.temperature) ?: return null
        if (rider <= 0) return null
        val bike = state.bikes.firstOrNull { it.id == state.selectedBikeId }
        val bikeWeight = bike?.weightKg ?: PressureCalculator.REF_BIKE_KG
        val front = bike?.front ?: PressureCalculator.defaultWheel
        val rear = bike?.rear ?: PressureCalculator.defaultWheel
        if (bikeWeight <= 0 || front.widthIn <= 0 || rear.widthIn <= 0) return null
        val frontAdvice = PressureCalculator.recommend(
            PressureInput(rider, bikeWeight, front, WheelPosition.FRONT, state.discipline, state.terrain),
        )
        val rearAdvice = PressureCalculator.recommend(
            PressureInput(rider, bikeWeight, rear, WheelPosition.REAR, state.discipline, state.terrain),
        )
        return RideAdvice(
            front = frontAdvice,
            rear = rearAdvice,
            frontGaragePsi = PressureCalculator.garagePsi(frontAdvice.psi, temp, state.garageTempC),
            rearGaragePsi = PressureCalculator.garagePsi(rearAdvice.psi, temp, state.garageTempC),
        )
    }

    companion object {
        fun factory(app: PresionApp) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CalculatorViewModel(app) as T
        }
    }
}
