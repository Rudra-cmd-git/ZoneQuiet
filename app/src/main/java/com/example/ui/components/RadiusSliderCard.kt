package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.IndigoNeon
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RadiusSliderCard(
    zoneLabel: String,
    radiusMeters: Float,
    onLabelChange: (String) -> Unit,
    onRadiusChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(100f, 250f, 500f, 1000f, 1500f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radius_settings_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Zone Name Field
            OutlinedTextField(
                value = zoneLabel,
                onValueChange = onLabelChange,
                label = { Text("Zone Name") },
                placeholder = { Text("e.g. Office, Library, Studio") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = IndigoNeon,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoNeon,
                    unfocusedBorderColor = BorderSubtle,
                    focusedLabelColor = IndigoNeon,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zone_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Radius Metric Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = IndigoNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Geofence Radius",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IndigoNeon.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, IndigoNeon.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${radiusMeters.roundToInt()} meters",
                        color = IndigoNeon,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Continuous Radius Slider (50m to 2000m)
            Slider(
                value = radiusMeters,
                onValueChange = { onRadiusChange(it) },
                valueRange = 50f..2000f,
                steps = 38, // 50m increments
                colors = SliderDefaults.colors(
                    thumbColor = IndigoNeon,
                    activeTrackColor = IndigoNeon,
                    inactiveTrackColor = BorderSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radius_slider")
            )

            // Preset Quick-Tap Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.forEach { preset ->
                    val isSelected = (radiusMeters.roundToInt() == preset.roundToInt())
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRadiusChange(preset) },
                        label = {
                            Text(
                                text = if (preset >= 1000) "${(preset / 1000).toInt()} km" else "${preset.toInt()} m",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoNeon.copy(alpha = 0.25f),
                            selectedLabelColor = IndigoNeon,
                            containerColor = NavySurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IndigoNeon else BorderSubtle
                        )
                    )
                }
            }
        }
    }
}
