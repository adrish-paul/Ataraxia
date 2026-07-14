package com.example.ataraxia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.preferences.SanctuaryPreferences
import com.example.ataraxia.data.repository.JournalRepository
import com.example.ataraxia.data.repository.SessionRepository
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferences: SanctuaryPreferences,
    private val journalRepository: JournalRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val username: StateFlow<String> = preferences.usernameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val profileImage: StateFlow<String> = preferences.profileImageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val onboardingCompleted: StateFlow<Boolean> = preferences.onboardingCompletedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isFirstLaunch: StateFlow<Boolean> = preferences.isFirstLaunchFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<AtaraxiaThemeMode> = preferences.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AtaraxiaThemeMode.SAKURA)

    init {
        viewModelScope.launch {
            preferences.initializeDefaultThemeIfNecessary()
        }
    }

    val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val soundEffectsEnabled: StateFlow<Boolean> = preferences.soundEffectsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appLockEnabled: StateFlow<Boolean> = preferences.appLockEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appPin: StateFlow<String> = preferences.appPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun completeOnboarding(name: String) {
        viewModelScope.launch {
            preferences.saveUsername(name)
            preferences.saveOnboardingCompleted(true)
        }
    }

    fun completeFirstLaunch() {
        viewModelScope.launch {
            preferences.saveFirstLaunch(false)
        }
    }

    fun updateUsername(name: String) {
        viewModelScope.launch {
            preferences.saveUsername(name)
        }
    }

    fun updateProfileImage(path: String) {
        viewModelScope.launch {
            preferences.saveProfileImage(path)
        }
    }

    fun updateThemeMode(mode: AtaraxiaThemeMode) {
        viewModelScope.launch {
            preferences.saveThemeMode(mode)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveNotificationsEnabled(enabled)
        }
    }

    fun updateSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveSoundEffectsEnabled(enabled)
        }
    }

    fun updateAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveAppLockEnabled(enabled)
        }
    }

    fun updateAppPin(pin: String) {
        viewModelScope.launch {
            preferences.saveAppPin(pin)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferences.clearAllPreferences()
            journalRepository.clearAll()
            sessionRepository.clearAllSessions()
        }
    }
}
