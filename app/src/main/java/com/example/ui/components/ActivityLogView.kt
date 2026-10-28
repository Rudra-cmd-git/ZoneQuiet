package com.example.ui.components

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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLogEntry
import com.example.data.model.LogType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.IndigoNeon
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RoseWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDnd
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityLogView(
    logs: List<ActivityLogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_logs_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = EmeraldNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Automation Activity Log",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear activity logs",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavySurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transitions logged yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "When you enter or exit your geofence zone, events and DND state changes will appear here.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logs.take(10).forEach { log ->
                        val (icon, color) = getLogVisuals(log.type)
                        val timeStr = formatTimestamp(log.timestamp)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NavySurface,
                            border = BorderStroke(0.5.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.detail,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getLogVisuals(type: LogType): Pair<ImageVector, Color> {
    return when (type) {
        LogType.DND_ON -> Pair(Icons.Default.DoNotDisturbOn, EmeraldNeon)
        LogType.DND_OFF -> Pair(Icons.Default.NotificationsActive, IndigoNeon)
        LogType.PRESERVED -> Pair(Icons.Default.Security, VioletDnd)
        LogType.ENTER -> Pair(Icons.Default.CheckCircle, EmeraldNeon)
        LogType.EXIT -> Pair(Icons.Default.History, IndigoNeon)
        LogType.ERROR -> Pair(Icons.Default.ErrorOutline, RoseWarning)
        LogType.SYSTEM -> Pair(Icons.Default.Settings, TextSecondary)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
