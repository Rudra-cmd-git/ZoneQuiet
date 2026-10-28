package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PermissionState
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RoseWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PermissionCard(
    permissions: PermissionState,
    onRequestLocation: () -> Unit,
    onRequestBackgroundLocation: () -> Unit,
    onRequestDndPolicy: () -> Unit,
    onRequestNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !permissions.isAllGranted,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("permission_checklist_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
            border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AmberAlert.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Permissions Required",
                                tint = AmberAlert,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Permissions Required",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "ZoneQuiet requires system access to automate DND reliably.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Location Access
                PermissionRowItem(
                    title = "Precise Location",
                    description = "Needed to detect entering and exiting your quiet zone",
                    isGranted = permissions.hasFineLocation,
                    actionLabel = "Grant",
                    onAction = onRequestLocation,
                    testTag = "grant_fine_location_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Background Location
                PermissionRowItem(
                    title = "Background Location",
                    description = "Required for geofencing when the phone is locked or app is closed",
                    isGranted = permissions.hasBackgroundLocation,
                    actionLabel = "Allow 'All the time'",
                    onAction = onRequestBackgroundLocation,
                    enabled = permissions.hasFineLocation,
                    testTag = "grant_bg_location_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Do Not Disturb (Notification Policy) Access
                PermissionRowItem(
                    title = "Do Not Disturb Control",
                    description = "Allows the app to silence and restore phone interruptions",
                    isGranted = permissions.hasNotificationPolicy,
                    actionLabel = "Open Settings",
                    onAction = onRequestDndPolicy,
                    testTag = "grant_dnd_policy_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Notifications (Android 13+)
                PermissionRowItem(
                    title = "Status Notifications",
                    description = "Provides confirmation banners when DND triggers or is preserved",
                    isGranted = permissions.hasPostNotifications,
                    actionLabel = "Enable",
                    onAction = onRequestNotifications,
                    testTag = "grant_notifications_button"
                )
            }
        }
    }
}

@Composable
private fun PermissionRowItem(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    enabled: Boolean = true,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavySurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) EmeraldNeon else RoseWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldNeon.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Granted",
                        color = EmeraldNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onAction,
                    enabled = enabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AmberAlert
                    ),
                    border = BorderStroke(1.dp, if (enabled) AmberAlert else TextMuted),
                    modifier = Modifier.testTag(testTag)
                ) {
                    Text(text = actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
