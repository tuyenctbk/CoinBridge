package com.common.shake.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import java.util.Locale
import kotlin.math.roundToInt

enum class SensitivityTier(val label: String, val description: String) {
    HIGH("High", "Triggers with light wrist flick"),
    STANDARD("Standard", "Deliberate hand shake"),
    LOW("Low", "Vigorous shake (prevents false pocket triggers)")
}

fun classifyGForce(g: Float): SensitivityTier = when {
    g <= 2.0f -> SensitivityTier.HIGH
    g <= 3.0f -> SensitivityTier.STANDARD
    else -> SensitivityTier.LOW
}

@Composable
fun ShakeSensitivitySettingCard(
    isEnabled: Boolean,
    thresholdGForce: Float,
    onEnabledChanged: (Boolean) -> Unit,
    onThresholdChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = remember(thresholdGForce) { classifyGForce(thresholdGForce) }
    val tierLabel = when (tier) {
        SensitivityTier.HIGH -> stringResource(R.string.shake_tier_high)
        SensitivityTier.STANDARD -> stringResource(R.string.shake_tier_standard)
        SensitivityTier.LOW -> stringResource(R.string.shake_tier_low)
    }
    val tierDesc = when (tier) {
        SensitivityTier.HIGH -> stringResource(R.string.shake_tier_high_desc)
        SensitivityTier.STANDARD -> stringResource(R.string.shake_tier_standard_desc)
        SensitivityTier.LOW -> stringResource(R.string.shake_tier_low_desc)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shake_sensitivity_setting_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.shake_gesture_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.shake_gesture_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChanged,
                    modifier = Modifier.testTag("shake_master_switch")
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Value readout & classification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.shake_sensitivity_label, tierLabel),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", thresholdGForce)} g",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = tierDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Continuous Slider (1.5g to 4.5g in 0.1g steps)
                Slider(
                    value = thresholdGForce,
                    onValueChange = { onThresholdChanged((it * 10f).roundToInt() / 10f) },
                    valueRange = 1.5f..4.5f,
                    steps = 29,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shake_sensitivity_slider")
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(stringResource(R.string.shake_preset_gentle), 1.8f, "gentle"),
                        Triple(stringResource(R.string.shake_preset_normal), 2.5f, "normal"),
                        Triple(stringResource(R.string.shake_preset_firm), 3.5f, "firm")
                    ).forEach { (label, value, tagKey) ->
                        OutlinedButton(
                            onClick = { onThresholdChanged(value) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shake_preset_$tagKey"),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
