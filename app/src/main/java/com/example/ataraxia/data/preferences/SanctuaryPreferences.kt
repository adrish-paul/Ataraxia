package com.example.ataraxia.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sanctuary_preferences")

class SanctuaryPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_APP_PIN = stringPreferencesKey("app_pin")
        val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image")
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val KEY_TODAY_MOOD = stringPreferencesKey("today_mood")
        val KEY_TODAY_MOOD_DATE = stringPreferencesKey("today_mood_date")
        val KEY_DRAFT_TITLE = stringPreferencesKey("draft_title")
        val KEY_DRAFT_CONTENT = stringPreferencesKey("draft_content")
        val KEY_DRAFT_MOOD = stringPreferencesKey("draft_mood")
        val KEY_DRAFT_TAGS = stringPreferencesKey("draft_tags")
        val KEY_DRAFT_PROMPT = stringPreferencesKey("draft_prompt")
        val KEY_DRAFT_TIMESTAMP = longPreferencesKey("draft_timestamp")
        
        // Phase 5 Keys
        val KEY_AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEY_BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        val KEY_MINDFUL_USAGE_PERMISSION = booleanPreferencesKey("mindful_usage_permission")
        
        val KEY_REMINDER_DAILY = booleanPreferencesKey("reminder_daily")
        val KEY_REMINDER_JOURNAL = booleanPreferencesKey("reminder_journal")
        val KEY_REMINDER_BREATHE = booleanPreferencesKey("reminder_breathe")
        val KEY_REMINDER_FOCUS = booleanPreferencesKey("reminder_focus")
        val KEY_REMINDER_MINDFULNESS = booleanPreferencesKey("reminder_mindfulness")
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
                if (isSystemDarkTheme(context)) AtaraxiaThemeMode.FOREST else AtaraxiaThemeMode.AQUA
            } else {
                val mappedStr = when (modeStr) {
                    "DARK" -> AtaraxiaThemeMode.FOREST.name
                    "AMOLED" -> AtaraxiaThemeMode.COSMOS.name
                    else -> modeStr
                }
                try {
                    AtaraxiaThemeMode.valueOf(mappedStr)
                } catch (_: Exception) {
                    AtaraxiaThemeMode.AQUA
                }
            }
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

    val todayMoodFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val savedDate = preferences[KEY_TODAY_MOOD_DATE] ?: ""
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            if (savedDate == todayDate) {
                preferences[KEY_TODAY_MOOD] ?: ""
            } else {
                ""
            }
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

    val defaultThemeMode: AtaraxiaThemeMode
        get() = if (isSystemDarkTheme(context)) AtaraxiaThemeMode.FOREST else AtaraxiaThemeMode.SAKURA

    val initialThemeMode: AtaraxiaThemeMode
        get() {
            return try {
                val modeStr = kotlinx.coroutines.runBlocking {
                    dataStore.data.map { it[KEY_THEME_MODE] }.firstOrNull()
                }
                if (modeStr == null) {
                    defaultThemeMode
                } else {
                    val mappedStr = when (modeStr) {
                        "DARK" -> AtaraxiaThemeMode.FOREST.name
                        "AMOLED" -> AtaraxiaThemeMode.COSMOS.name
                        else -> modeStr
                    }
                    AtaraxiaThemeMode.valueOf(mappedStr)
                }
            } catch (e: Exception) {
                defaultThemeMode
            }
        }

    suspend fun initializeDefaultThemeIfNecessary() {
        dataStore.edit { preferences ->
            if (preferences[KEY_THEME_MODE] == null) {
                val defaultMode = if (isSystemDarkTheme(context)) AtaraxiaThemeMode.FOREST else AtaraxiaThemeMode.SAKURA
                preferences[KEY_THEME_MODE] = defaultMode.name
            }
        }
    }

    suspend fun saveThemeMode(mode: AtaraxiaThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
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

    suspend fun saveTodayMood(mood: String) {
        val todayDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        dataStore.edit { preferences ->
            preferences[KEY_TODAY_MOOD] = mood
            preferences[KEY_TODAY_MOOD_DATE] = todayDate
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val journalDraftFlow: Flow<JournalDraft?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val title = preferences[KEY_DRAFT_TITLE] ?: ""
            val content = preferences[KEY_DRAFT_CONTENT] ?: ""
            val mood = preferences[KEY_DRAFT_MOOD] ?: ""
            val tags = preferences[KEY_DRAFT_TAGS] ?: ""
            val prompt = preferences[KEY_DRAFT_PROMPT] ?: ""
            val timestamp = preferences[KEY_DRAFT_TIMESTAMP] ?: 0L
            if (content.isNotBlank() || title.isNotBlank()) {
                JournalDraft(title, content, mood, tags, prompt, timestamp)
            } else {
                null
            }
        }

    suspend fun saveJournalDraft(title: String, content: String, mood: String, tags: String, prompt: String, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_DRAFT_TITLE] = title
            preferences[KEY_DRAFT_CONTENT] = content
            preferences[KEY_DRAFT_MOOD] = mood
            preferences[KEY_DRAFT_TAGS] = tags
            preferences[KEY_DRAFT_PROMPT] = prompt
            preferences[KEY_DRAFT_TIMESTAMP] = timestamp
        }
    }

    suspend fun clearJournalDraft() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_DRAFT_TITLE)
            preferences.remove(KEY_DRAFT_CONTENT)
            preferences.remove(KEY_DRAFT_MOOD)
            preferences.remove(KEY_DRAFT_TAGS)
            preferences.remove(KEY_DRAFT_PROMPT)
            preferences.remove(KEY_DRAFT_TIMESTAMP)
        }
    }

    private fun getBooleanFlow(key: Preferences.Key<Boolean>, defaultValue: Boolean = false): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    val amoledModeFlow = getBooleanFlow(KEY_AMOLED_MODE, false)
    val dynamicColorsFlow = getBooleanFlow(KEY_DYNAMIC_COLORS, false)
    val reducedMotionFlow = getBooleanFlow(KEY_REDUCED_MOTION, false)
    val biometricsEnabledFlow = getBooleanFlow(KEY_BIOMETRICS_ENABLED, false)
    val mindfulUsagePermissionFlow = getBooleanFlow(KEY_MINDFUL_USAGE_PERMISSION, true)
    
    val reminderDailyFlow = getBooleanFlow(KEY_REMINDER_DAILY, true)
    val reminderJournalFlow = getBooleanFlow(KEY_REMINDER_JOURNAL, true)
    val reminderBreatheFlow = getBooleanFlow(KEY_REMINDER_BREATHE, true)
    val reminderFocusFlow = getBooleanFlow(KEY_REMINDER_FOCUS, true)
    val reminderMindfulnessFlow = getBooleanFlow(KEY_REMINDER_MINDFULNESS, true)

    suspend fun saveAmoledMode(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_AMOLED_MODE] = enabled }
    }
    suspend fun saveDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_DYNAMIC_COLORS] = enabled }
    }
    suspend fun saveReducedMotion(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_REDUCED_MOTION] = enabled }
    }
    suspend fun saveBiometricsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_BIOMETRICS_ENABLED] = enabled }
    }
    suspend fun saveMindfulUsagePermission(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_MINDFUL_USAGE_PERMISSION] = enabled }
    }
    suspend fun saveReminderDaily(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_REMINDER_DAILY] = enabled }
    }
    suspend fun saveReminderJournal(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_REMINDER_JOURNAL] = enabled }
    }
    suspend fun saveReminderBreathe(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_REMINDER_BREATHE] = enabled }
    }
    suspend fun saveReminderFocus(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_REMINDER_FOCUS] = enabled }
    }
    suspend fun saveReminderMindfulness(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_REMINDER_MINDFULNESS] = enabled }
    }
}

data class JournalDraft(
    val title: String,
    val content: String,
    val mood: String,
    val tags: String,
    val prompt: String,
    val timestamp: Long
)
