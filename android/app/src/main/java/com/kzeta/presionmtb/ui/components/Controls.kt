package com.kzeta.presionmtb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kzeta.presionmtb.ui.theme.LocalPalette
import com.kzeta.presionmtb.ui.theme.Manrope

@Composable
fun SectionLabel(text: String) {
    val palette = LocalPalette.current
    Text(
        text = text,
        color = palette.muted,
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

@Composable
fun <T> Segmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    val palette = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val active = option == selected
            TextButton(
                onClick = { onSelect(option) },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (active) palette.lime else palette.surfaceHigh)
                    .border(1.dp, if (active) palette.lime else palette.line, RoundedCornerShape(14.dp)),
            ) {
                Text(
                    text = label(option),
                    color = if (active) palette.onLime else palette.ink,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    suffix: String? = null,
) {
    val palette = LocalPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = palette.lime,
            unfocusedBorderColor = palette.line,
            focusedLabelColor = palette.accent,
            unfocusedLabelColor = palette.muted,
            cursorColor = palette.lime,
            focusedTextColor = palette.ink,
            unfocusedTextColor = palette.ink,
        ),
    )
}

@Composable
fun LimeButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val palette = LocalPalette.current
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) palette.lime else palette.surfaceHigh),
    ) {
        Text(
            text = text,
            color = if (enabled) palette.onLime else palette.muted,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp),
        )
    }
}

@Composable
fun PressureCard(
    title: String,
    psi: Double,
    unitIsPsi: Boolean,
    range: String,
    note: String?,
) {
    val palette = LocalPalette.current
    val primary = if (unitIsPsi) com.kzeta.presionmtb.domain.PressureCalculator.formatPsi(psi)
    else com.kzeta.presionmtb.domain.PressureCalculator.formatBar(psi)
    val secondary = if (unitIsPsi) "${com.kzeta.presionmtb.domain.PressureCalculator.formatBar(psi)} bar"
    else "${com.kzeta.presionmtb.domain.PressureCalculator.formatPsi(psi)} psi"
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Text(title, color = palette.muted, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Text(
            primary,
            color = if (palette.isDark) palette.lime else palette.ink,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            letterSpacing = (-1).sp,
        )
        Text(if (unitIsPsi) "psi" else "bar", color = palette.accent, fontFamily = Manrope, fontWeight = FontWeight.SemiBold)
        Text(secondary, color = palette.muted, style = MaterialTheme.typography.bodyMedium)
        Text(range, color = palette.ink, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        if (note != null) {
            Text(note, color = palette.accent, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
