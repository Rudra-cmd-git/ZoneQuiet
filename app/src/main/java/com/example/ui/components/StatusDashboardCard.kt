package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransitionState
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.IndigoNeon
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDnd

@Composable
fun StatusDashboardCard(
    isAutomationEnabled: Boolean,
    isDndActive: Boolean,
    dndStatusDescription: String,
    currentTransitionState: TransitionState,
    zoneLabel: String,
    wasDndPriorActive: Boolean,
    onToggleAutomation: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor by animateColorAsState(
        targetValue = if (isAutomationEnabled) EmeraldNeon else TextMuted,
        label = "activeColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_dashboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = BorderStroke(1.dp, if (isAutomationEnabled) EmeraldNeon.copy(alpha = 0.5f) else BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Master Automation Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = activeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Geofence Automation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAutomationEnabled) "Active & Monitoring in Background" else "Automation Disabled",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isAutomationEnabled) EmeraldNeon else TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Switch(
                    checked = isAutomationEnabled,
                    onCheckedChange = onToggleAutomation,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldNeon,
                        checkedTrackColor = EmeraldDark,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = NavySurface
                    ),
                    modifier = Modifier.testTag("automation_master_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dual Status Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: System DND Status
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NavySurface,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDndActive) Icons.Default.DoNotDisturbOn else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isDndActive) VioletDnd else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "System DND",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dndStatusDescription,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDndActive) VioletDnd else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Metric 2: Zone Location State
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NavySurface,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WhereToVote,
                                contentDescription = null,
                                tint = when (currentTransitionState) {
                                    TransitionState.INSIDE -> EmeraldNeon
                                    TransitionState.OUTSIDE -> IndigoNeon
                                    TransitionState.UNKNOWN -> TextSecondary
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zone Status",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (currentTransitionState) {
                                TransitionState.INSIDE -> "Inside ($zoneLabel)"
                                TransitionState.OUTSIDE -> "Outside ($zoneLabel)"
                                TransitionState.UNKNOWN -> "Awaiting GPS Fix"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = when (currentTransitionState) {
                                    TransitionState.INSIDE -> EmeraldNeon
                                    TransitionState.OUTSIDE -> TextPrimary
                                    TransitionState.UNKNOWN -> TextMuted
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Safety rule indicator card: "Never disable DND if it was already enabled before entering"
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavySurface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (wasDndPriorActive && currentTransitionState == TransitionState.INSIDE) {
                            "Safety Guard: DND was already ON prior to entry. It will NOT be disabled when exiting."
                        } else {
                            "Smart Guard: Will never turn off DND if you turned it on manually before entering."
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    )
                }
            }
        }
    }
}
