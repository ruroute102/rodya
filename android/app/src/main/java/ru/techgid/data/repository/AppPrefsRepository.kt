package ru.techgid.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранилище пользовательских настроек и локального профиля.
 */
@Singleton
class AppPrefsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("pref_dark_theme")
        val NOTIFICATIONS = booleanPreferencesKey("pref_notifications")
        val MAINTENANCE_REMINDERS = booleanPreferencesKey("pref_maintenance_reminders")
        val OFFLINE_SYNC = booleanPreferencesKey("pref_offline_sync")
        val LANGUAGE = stringPreferencesKey("pref_language")

        val SELECTED_CONFIG_ID = intPreferencesKey("selected_config_id")
        val SELECTED_CAR_NAME = stringPreferencesKey("selected_car_name")

        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")

        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_PHONE = stringPreferencesKey("profile_phone")
        val PROFILE_LOGGED_IN = booleanPreferencesKey("profile_logged_in")
    }

    val darkTheme: Flow<Boolean> = dataStore.data.map { it[Keys.DARK_THEME] ?: false }
    val notifications: Flow<Boolean> = dataStore.data.map { it[Keys.NOTIFICATIONS] ?: true }
    val maintenanceReminders: Flow<Boolean> = dataStore.data.map { it[Keys.MAINTENANCE_REMINDERS] ?: true }
    val offlineSync: Flow<Boolean> = dataStore.data.map { it[Keys.OFFLINE_SYNC] ?: false }
    val language: Flow<String> = dataStore.data.map { it[Keys.LANGUAGE] ?: "ru" }

    val selectedConfigId: Flow<Int> = dataStore.data.map { it[Keys.SELECTED_CONFIG_ID] ?: 1 }
    val selectedCarName: Flow<String> = dataStore.data.map {
        it[Keys.SELECTED_CAR_NAME] ?: "Audi Q3 2011 · 2.0 TFSI"
    }

    val recentSearches: Flow<List<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.RECENT_SEARCHES] ?: ""
        if (raw.isBlank()) emptyList() else raw.split("|")
    }

    suspend fun addRecentSearch(query: String) {
        dataStore.edit { prefs ->
            val current = (prefs[Keys.RECENT_SEARCHES] ?: "")
                .split("|").filter { it.isNotBlank() }
            val updated = (listOf(query) + current.filter { it != query }).take(10)
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString("|")
        }
    }

    val profileName: Flow<String> = dataStore.data.map { it[Keys.PROFILE_NAME] ?: "" }
    val profilePhone: Flow<String> = dataStore.data.map { it[Keys.PROFILE_PHONE] ?: "" }
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[Keys.PROFILE_LOGGED_IN] ?: false }

    suspend fun setDarkTheme(value: Boolean) = dataStore.edit { it[Keys.DARK_THEME] = value }
    suspend fun setNotifications(value: Boolean) = dataStore.edit { it[Keys.NOTIFICATIONS] = value }
    suspend fun setMaintenanceReminders(value: Boolean) = dataStore.edit { it[Keys.MAINTENANCE_REMINDERS] = value }
    suspend fun setOfflineSync(value: Boolean) = dataStore.edit { it[Keys.OFFLINE_SYNC] = value }
    suspend fun setLanguage(value: String) = dataStore.edit { it[Keys.LANGUAGE] = value }

    suspend fun setSelectedCar(configId: Int, displayName: String) {
        dataStore.edit {
            it[Keys.SELECTED_CONFIG_ID] = configId
            it[Keys.SELECTED_CAR_NAME] = displayName
        }
    }

    suspend fun saveProfile(name: String, phone: String) {
        dataStore.edit {
            it[Keys.PROFILE_NAME] = name
            it[Keys.PROFILE_PHONE] = phone
            it[Keys.PROFILE_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        dataStore.edit {
            it.remove(Keys.PROFILE_NAME)
            it.remove(Keys.PROFILE_PHONE)
            it[Keys.PROFILE_LOGGED_IN] = false
        }
    }
}
