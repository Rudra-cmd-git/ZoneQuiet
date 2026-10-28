package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.datastore.ZonePreferencesDataStore
import com.example.data.model.LogType
import com.example.data.model.TransitionState
import com.example.service.DndNotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.example.zonequiet.ACTION_GEOFENCE_EVENT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        val pendingResult = goAsync()
        val dataStore = ZonePreferencesDataStore(context)
        val dndHelper = DndNotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (geofencingEvent.hasError()) {
                    val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                    dataStore.addLog(
                        title = "Geofence Error",
                        detail = "Code: ${geofencingEvent.errorCode} - $errorMessage",
                        type = LogType.ERROR
                    )
                    return@launch
                }

                val transition = geofencingEvent.geofenceTransition
                val zone = dataStore.getZone()

                when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        // 1. Check if DND is already active right before we change it
                        val wasAlreadyActive = dndHelper.isDndActive()
                        dataStore.setWasDndPriorActive(wasAlreadyActive)
                        dataStore.setCurrentZoneState(TransitionState.INSIDE)

                        if (wasAlreadyActive) {
                            // Already active: keep it on, do not disturb the user's manual preference
                            dataStore.addLog(
                                title = "Entered ${zone.label}",
                                detail = "DND was already active prior to entry. State preserved.",
                                type = LogType.PRESERVED
                            )
                            dndHelper.postTransitionNotification(
                                title = "Entered ${zone.label}",
                                message = "Do Not Disturb was already ON; state preserved."
                            )
                        } else {
                            // Turn DND on automatically
                            val success = dndHelper.enableDnd()
                            if (success) {
                                dataStore.addLog(
                                    title = "Entered ${zone.label}",
                                    detail = "Do Not Disturb automatically turned ON.",
                                    type = LogType.DND_ON
                                )
                                dndHelper.postTransitionNotification(
                                    title = "Entered ${zone.label}",
                                    message = "Do Not Disturb automatically turned ON."
                                )
                            } else {
                                dataStore.addLog(
                                    title = "Entered ${zone.label}",
                                    detail = "Failed to enable DND: Notification policy permission required.",
                                    type = LogType.ERROR
                                )
                            }
                        }
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        dataStore.setCurrentZoneState(TransitionState.OUTSIDE)
                        val wasPriorActive = dataStore.getWasDndPriorActive()

                        // CRITICAL: Never disable DND if it was already enabled before entering the area
                        if (wasPriorActive) {
                            dataStore.addLog(
                                title = "Exited ${zone.label}",
                                detail = "DND preserved ON because it was already active prior to entering.",
                                type = LogType.PRESERVED
                            )
                            dndHelper.postTransitionNotification(
                                title = "Exited ${zone.label}",
                                message = "DND kept ON (it was enabled prior to entering this zone)."
                            )
                        } else {
                            // Was turned on by the geofence entry; now safely turn it off
                            val success = dndHelper.disableDnd()
                            if (success) {
                                dataStore.addLog(
                                    title = "Exited ${zone.label}",
                                    detail = "Do Not Disturb automatically turned OFF.",
                                    type = LogType.DND_OFF
                                )
                                dndHelper.postTransitionNotification(
                                    title = "Exited ${zone.label}",
                                    message = "Do Not Disturb automatically turned OFF."
                                )
                            } else {
                                dataStore.addLog(
                                    title = "Exited ${zone.label}",
                                    detail = "Failed to turn off DND: Notification policy permission required.",
                                    type = LogType.ERROR
                                )
                            }
                        }
                    }

                    else -> {
                        // Other transitions (dwell, etc.)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
