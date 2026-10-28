package com.example.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.GeofenceZone
import com.example.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    companion object {
        const val GEOFENCE_PENDING_INTENT_REQUEST_CODE = 2001
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, GEOFENCE_PENDING_INTENT_REQUEST_CODE, intent, flags)
    }

    @SuppressLint("MissingPermission")
    suspend fun registerGeofence(zone: GeofenceZone): Result<Unit> {
        return try {
            val geofence = Geofence.Builder()
                .setRequestId(zone.id)
                .setCircularRegion(zone.latitude, zone.longitude, zone.radiusMeters)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                // Set low battery responsiveness (e.g. 5 seconds)
                .setNotificationResponsiveness(5000)
                .build()

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(request, geofencePendingIntent).await()
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeGeofence(): Result<Unit> {
        return try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
