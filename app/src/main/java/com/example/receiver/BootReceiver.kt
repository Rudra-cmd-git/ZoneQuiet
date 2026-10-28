package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.datastore.ZonePreferencesDataStore
import com.example.data.model.LogType
import com.example.service.GeofenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            val dataStore = ZonePreferencesDataStore(context)
            val geofenceManager = GeofenceManager(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val zone = dataStore.getZone()
                    if (zone.isEnabled) {
                        val result = geofenceManager.registerGeofence(zone)
                        if (result.isSuccess) {
                            dataStore.addLog(
                                title = "Device Reboot / App Restored",
                                detail = "Successfully restored active geofence for '${zone.label}'.",
                                type = LogType.SYSTEM
                            )
                        } else {
                            dataStore.addLog(
                                title = "Reboot Restore Warning",
                                detail = "Could not automatically restore geofence: ${result.exceptionOrNull()?.message}",
                                type = LogType.ERROR
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
