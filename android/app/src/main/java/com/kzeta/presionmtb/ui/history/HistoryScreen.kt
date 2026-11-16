package com.kzeta.presionmtb.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kzeta.presionmtb.PresionApp
import com.kzeta.presionmtb.domain.AppTime
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.domain.PressureLog
import com.kzeta.presionmtb.domain.PressureUnit
import com.kzeta.presionmtb.ui.theme.LocalPalette
import com.kzeta.presionmtb.ui.theme.Manrope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

data class HistoryUi(val logs: List<PressureLog> = emptyList(), val unit: PressureUnit = PressureUnit.PSI)

class HistoryViewModel(private val app: PresionApp) : ViewModel() {
    val state = combine(app.library.observeLogs(), app.library.observeProfile()) { logs, profile ->
        HistoryUi(logs, profile?.unit ?: PressureUnit.PSI)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUi())

    fun delete(log: PressureLog) {
        viewModelScope.launch { app.library.softDeleteLog(log) }
    }

    companion object {
        fun factory(app: PresionApp) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(app) as T
        }
    }
}

@Composable
fun HistoryScreen(app: PresionApp) {
    val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(app))
    val state by vm.state.collectAsState()
    val palette = LocalPalette.current
    var pending by remember { mutableStateOf<PressureLog?>(null) }
    if (state.logs.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Historial", style = MaterialTheme.typography.headlineMedium, fontFamily = Manrope, fontWeight = FontWeight.Bold, color = palette.ink)
            Text("Cuando guardes una salida vas a ver el lugar, la temperatura y la presión que usaste.", color = palette.muted)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Historial",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                color = palette.ink,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        items(state.logs, key = { it.id }) { log ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                Text(log.placeName, color = palette.ink, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                Text(formatWhen(log.createdAt), color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                Text("${log.bikeName} · ${PressureCalculator.formatKg(log.temperatureC)} °C", color = palette.ink)
                Text(
                    "Usada ${PressureCalculator.format(log.frontUsedPsi, state.unit)} / ${PressureCalculator.format(log.rearUsedPsi, state.unit)} ${state.unit.label}",
                    color = if (palette.isDark) palette.lime else palette.accent,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Recomendada ${PressureCalculator.format(log.frontRecommendedPsi, state.unit)} / ${PressureCalculator.format(log.rearRecommendedPsi, state.unit)} ${state.unit.label}",
                    color = palette.muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { pending = log }) { Text("Eliminar", color = palette.accent) }
            }
        }
    }
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Eliminar salida") },
            text = { Text("Se oculta del historial y queda marcada por si más adelante sincronizas.") },
            confirmButton = {
                TextButton(onClick = {
                    pending?.let(vm::delete)
                    pending = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar") } },
        )
    }
}

private fun formatWhen(iso: String): String = try {
    val zoned = AppTime.parse(iso).atZone(ZoneId.systemDefault())
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale("es")).format(zoned)
} catch (_: Exception) {
    iso
}
