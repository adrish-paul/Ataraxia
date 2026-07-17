package com.example.ataraxia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.dao.BreatheDao
import com.example.ataraxia.data.local.dao.FocusDao
import com.example.ataraxia.data.local.dao.JournalDao
import com.example.ataraxia.data.preferences.SanctuaryPreferences
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferences: SanctuaryPreferences,
    private val journalDao: JournalDao,
    private val breatheDao: BreatheDao,
    private val focusDao: FocusDao
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
            journalDao.clearAllEntries()
            breatheDao.clearAllSessions()
            focusDao.clearAllSessions()
        }
    }
}
