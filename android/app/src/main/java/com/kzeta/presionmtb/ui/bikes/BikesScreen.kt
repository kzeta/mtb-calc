package com.kzeta.presionmtb.ui.bikes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kzeta.presionmtb.PresionApp
import com.kzeta.presionmtb.domain.Bike
import com.kzeta.presionmtb.domain.PressureCalculator
import com.kzeta.presionmtb.ui.theme.LocalPalette
import com.kzeta.presionmtb.ui.theme.Manrope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BikesViewModel(app: PresionApp) : ViewModel() {
    val bikes: StateFlow<List<Bike>> = app.library.observeBikes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(app: PresionApp) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BikesViewModel(app) as T
        }
    }
}

@Composable
fun BikesScreen(app: PresionApp, onOpen: (String) -> Unit) {
    val vm: BikesViewModel = viewModel(factory = BikesViewModel.factory(app))
    val bikes by vm.bikes.collectAsState()
    val palette = LocalPalette.current
    Box(Modifier.fillMaxSize()) {
        if (bikes.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bicis", style = MaterialTheme.typography.headlineMedium, fontFamily = Manrope, fontWeight = FontWeight.Bold, color = palette.ink)
                Text("Guarda cada bici con el aro, el ancho de cubierta y el montaje. La calculadora las usa tal cual.", color = palette.muted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        "Bicis",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                items(bikes, key = { it.id }) { bike ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
                            .clickable { onOpen(bike.id) }
                            .padding(16.dp),
                    ) {
                        Text(bike.name, color = palette.ink, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                        Text(
                            "${bike.wheelSize.label} · ${PressureCalculator.formatKg(bike.weightKg)} kg",
                            color = palette.muted,
                        )
                        Text(
                            "Del. ${bike.front.widthIn}″ / ${bike.front.rimInternalMm.toInt()} mm · ${bike.front.system.label}",
                            color = palette.ink,
                        )
                        Text(
                            "Tras. ${bike.rear.widthIn}″ / ${bike.rear.rimInternalMm.toInt()} mm · ${bike.rear.system.label}",
                            color = palette.ink,
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { onOpen("new") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = palette.lime,
            contentColor = palette.onLime,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar bici")
        }
    }
}
