package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ActivityLogEntry
import com.example.data.model.GeofenceZone
import com.example.data.model.LogType
import com.example.data.model.TransitionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zone_quiet_prefs")

class ZonePreferencesDataStore(private val context: Context) {

    companion object {
        private val KEY_ZONE_ID = stringPreferencesKey("zone_id")
        private val KEY_LATITUDE = doublePreferencesKey("latitude")
        private val KEY_LONGITUDE = doublePreferencesKey("longitude")
        private val KEY_RADIUS_METERS = floatPreferencesKey("radius_meters")
        private val KEY_ZONE_LABEL = stringPreferencesKey("zone_label")
        private val KEY_IS_AUTOMATION_ENABLED = booleanPreferencesKey("is_automation_enabled")
        private val KEY_WAS_DND_PRIOR_ACTIVE = booleanPreferencesKey("was_dnd_prior_active")
        private val KEY_CURRENT_ZONE_STATE = stringPreferencesKey("current_zone_state")
        private val KEY_ACTIVITY_LOGS = stringPreferencesKey("activity_logs_json")
    }

    val zoneFlow: Flow<GeofenceZone> = context.dataStore.data.map { prefs ->
        GeofenceZone(
            id = prefs[KEY_ZONE_ID] ?: "zonequiet_primary_zone",
            latitude = prefs[KEY_LATITUDE] ?: 37.7749,
            longitude = prefs[KEY_LONGITUDE] ?: -122.4194,
            radiusMeters = prefs[KEY_RADIUS_METERS] ?: 250f,
            label = prefs[KEY_ZONE_LABEL] ?: "Quiet Zone",
            isEnabled = prefs[KEY_IS_AUTOMATION_ENABLED] ?: false
        )
    }

    val isAutomationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_AUTOMATION_ENABLED] ?: false
    }

    val wasDndPriorActiveFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WAS_DND_PRIOR_ACTIVE] ?: false
    }

    val currentZoneStateFlow: Flow<TransitionState> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_CURRENT_ZONE_STATE]) {
            "INSIDE" -> TransitionState.INSIDE
            "OUTSIDE" -> TransitionState.OUTSIDE
            else -> TransitionState.UNKNOWN
        }
    }

    val activityLogsFlow: Flow<List<ActivityLogEntry>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[KEY_ACTIVITY_LOGS] ?: "[]"
        deserializeLogs(jsonString)
    }

    suspend fun getZone(): GeofenceZone {
        return zoneFlow.first()
    }

    suspend fun getWasDndPriorActive(): Boolean {
        return context.dataStore.data.first()[KEY_WAS_DND_PRIOR_ACTIVE] ?: false
    }

    suspend fun saveZone(zone: GeofenceZone) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ZONE_ID] = zone.id
            prefs[KEY_LATITUDE] = zone.latitude
            prefs[KEY_LONGITUDE] = zone.longitude
            prefs[KEY_RADIUS_METERS] = zone.radiusMeters
            prefs[KEY_ZONE_LABEL] = zone.label
            prefs[KEY_IS_AUTOMATION_ENABLED] = zone.isEnabled
        }
    }

    suspend fun setAutomationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_AUTOMATION_ENABLED] = enabled
        }
    }

    suspend fun setWasDndPriorActive(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAS_DND_PRIOR_ACTIVE] = active
        }
    }

    suspend fun setCurrentZoneState(state: TransitionState) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_ZONE_STATE] = state.name
        }
    }

    suspend fun addLog(title: String, detail: String, type: LogType) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_ACTIVITY_LOGS] ?: "[]"
            val logs = deserializeLogs(currentJson).toMutableList()
            val newEntry = ActivityLogEntry(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                title = title,
                detail = detail,
                type = type
            )
            logs.add(0, newEntry) // Newest first
            // Limit to last 50 entries
            val trimmed = if (logs.size > 50) logs.take(50) else logs
            prefs[KEY_ACTIVITY_LOGS] = serializeLogs(trimmed)
        }
    }

    suspend fun clearLogs() {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVITY_LOGS] = "[]"
        }
    }

    private fun serializeLogs(logs: List<ActivityLogEntry>): String {
        val array = JSONArray()
        for (log in logs) {
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("timestamp", log.timestamp)
            obj.put("title", log.title)
            obj.put("detail", log.detail)
            obj.put("type", log.type.name)
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeLogs(json: String): List<ActivityLogEntry> {
        return try {
            val list = mutableListOf<ActivityLogEntry>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", LogType.SYSTEM.name)
                val type = try {
                    LogType.valueOf(typeStr)
                } catch (e: Exception) {
                    LogType.SYSTEM
                }
                list.add(
                    ActivityLogEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        title = obj.optString("title", ""),
                        detail = obj.optString("detail", ""),
                        type = type
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
