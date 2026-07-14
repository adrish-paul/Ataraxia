package com.example.ataraxia.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sanctuary_preferences")

class SanctuaryPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_APP_PIN = stringPreferencesKey("app_pin")
        val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image")
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val usernameFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_USERNAME] ?: ""
        }

    val profileImageFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_PROFILE_IMAGE] ?: ""
        }

    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false
        }

    val isFirstLaunchFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_IS_FIRST_LAUNCH] ?: true
        }

    val themeModeFlow: Flow<AtaraxiaThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeStr = preferences[KEY_THEME_MODE]
            if (modeStr == null) {
                if (isSystemDarkTheme(context)) AtaraxiaThemeMode.AURORA else AtaraxiaThemeMode.SAKURA
            } else {
                val mappedStr = when (modeStr) {
                    "DARK" -> AtaraxiaThemeMode.AURORA.name
                    "AMOLED" -> AtaraxiaThemeMode.COSMOS.name
                    else -> modeStr
                }
                try {
                    AtaraxiaThemeMode.valueOf(mappedStr)
                } catch (e: Exception) {
                    AtaraxiaThemeMode.SAKURA
                }
            }
        }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
        }

    val soundEffectsEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_SOUND_EFFECTS_ENABLED] ?: false
        }

    val appLockEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] ?: false
        }

    val appPinFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_APP_PIN] ?: ""
        }

    suspend fun saveUsername(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = name
        }
    }

    suspend fun saveProfileImage(path: String) {
        dataStore.edit { preferences ->
            preferences[KEY_PROFILE_IMAGE] = path
        }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun saveFirstLaunch(first: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_FIRST_LAUNCH] = first
        }
    }

    private fun isSystemDarkTheme(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    suspend fun initializeDefaultThemeIfNecessary() {
        dataStore.edit { preferences ->
            if (preferences[KEY_THEME_MODE] == null) {
                val defaultMode = if (isSystemDarkTheme(context)) AtaraxiaThemeMode.AURORA else AtaraxiaThemeMode.SAKURA
                preferences[KEY_THEME_MODE] = defaultMode.name
            }
        }
    }

    suspend fun saveThemeMode(mode: AtaraxiaThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveSoundEffectsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun saveAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun saveAppPin(pin: String) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_PIN] = pin
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
