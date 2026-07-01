package dev.yinon.plantsnwater.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("app-settings")

enum class ThemeMode { System, Light, Dark }
enum class PlantListMode { Compact, Detailed }

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val defaultReminderHour: Int = 9,
    val defaultReminderMinute: Int = 0,
    val themeMode: ThemeMode = ThemeMode.System,
    val plantListMode: PlantListMode = PlantListMode.Detailed,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
)

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            notificationsEnabled = preferences[Keys.NotificationsEnabled] ?: true,
            defaultReminderHour = preferences[Keys.DefaultReminderHour] ?: 9,
            defaultReminderMinute = preferences[Keys.DefaultReminderMinute] ?: 0,
            themeMode = preferences[Keys.ThemeMode]?.let(ThemeMode::valueOf) ?: ThemeMode.System,
            plantListMode = preferences[Keys.PlantListMode]?.let(PlantListMode::valueOf) ?: PlantListMode.Detailed,
            firstDayOfWeek = preferences[Keys.FirstDayOfWeek]?.let(DayOfWeek::valueOf) ?: DayOfWeek.MONDAY
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NotificationsEnabled] = enabled }
    }

    suspend fun setDefaultReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.DefaultReminderHour] = hour.coerceIn(0, 23)
            it[Keys.DefaultReminderMinute] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    suspend fun setPlantListMode(mode: PlantListMode) {
        dataStore.edit { it[Keys.PlantListMode] = mode.name }
    }

    private object Keys {
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val DefaultReminderHour = intPreferencesKey("default_reminder_hour")
        val DefaultReminderMinute = intPreferencesKey("default_reminder_minute")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val PlantListMode = stringPreferencesKey("plant_list_mode")
        val FirstDayOfWeek = stringPreferencesKey("first_day_of_week")
    }
}
