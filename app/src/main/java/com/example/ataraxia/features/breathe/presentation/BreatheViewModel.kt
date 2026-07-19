package com.example.ataraxia.features.breathe.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.features.breathe.domain.DeleteBreatheSessionUseCase
import com.example.ataraxia.features.breathe.domain.GetBreatheSessionsUseCase
import com.example.ataraxia.features.breathe.domain.SaveBreatheSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.core.content.edit

class BreatheViewModel(
    getSessionsUseCase: GetBreatheSessionsUseCase,
    private val saveSessionUseCase: SaveBreatheSessionUseCase,
    private val deleteSessionUseCase: DeleteBreatheSessionUseCase
) : ViewModel() {

    val allSessions: StateFlow<List<BreatheSessionEntity>> = getSessionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDurationSeconds: StateFlow<Int> = getSessionsUseCase.getTotalDuration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Configuration Settings
    private val _selectedMethod = MutableStateFlow("Box Breathing")
    val selectedMethod: StateFlow<String> = _selectedMethod.asStateFlow()

    private val _selectedDurationMinutes = MutableStateFlow(5)
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    private val _selectedSound = MutableStateFlow("None")
    val selectedSound: StateFlow<String> = _selectedSound.asStateFlow()

    private val _soundVolume = MutableStateFlow(0.5f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _hapticGuidanceEnabled = MutableStateFlow(true)
    val hapticGuidanceEnabled: StateFlow<Boolean> = _hapticGuidanceEnabled.asStateFlow()

    fun updateSelectedMethod(method: String) {
        _selectedMethod.value = method
    }

    fun updateDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes
    }

    fun updateSound(sound: String) {
        _selectedSound.value = sound
    }

    fun updateVolume(volume: Float) {
        _soundVolume.value = volume
    }

    fun updateHapticGuidance(enabled: Boolean) {
        _hapticGuidanceEnabled.value = enabled
    }

    fun logSession(durationSeconds: Int, method: String, mood: String = "") {
        viewModelScope.launch {
            saveSessionUseCase(durationSeconds, method, mood)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            deleteSessionUseCase(id)
        }
    }

    // Custom Breathing Methods/Styles
    private val _customMethods = MutableStateFlow<List<MethodItem>>(emptyList())
    val customMethods: StateFlow<List<MethodItem>> = _customMethods.asStateFlow()

    fun loadCustomMethods(context: android.content.Context) {
        val prefs = context.getSharedPreferences("ataraxia_breathe_prefs", android.content.Context.MODE_PRIVATE)
        val serialized = prefs.getString("custom_breathe_styles", "") ?: ""
        if (serialized.isEmpty()) {
            _customMethods.value = emptyList()
            return
        }
        try {
            val list = serialized.split("|||").map { part ->
                val subparts = part.split(";;")
                MethodItem(
                    name = subparts[0],
                    desc = subparts[1],
                    pattern = subparts[2],
                    inhale = subparts[3].toInt(),
                    hold = subparts[4].toInt(),
                    exhale = subparts[5].toInt(),
                    rest = subparts[6].toInt(),
                    isCustom = true
                )
            }
            _customMethods.value = list
        } catch (e: Exception) {
            _customMethods.value = emptyList()
        }
    }

    private fun saveCustomMethods(context: android.content.Context, list: List<MethodItem>) {
        val prefs = context.getSharedPreferences("ataraxia_breathe_prefs", android.content.Context.MODE_PRIVATE)
        val serialized = list.joinToString("|||") { 
            "${it.name};;${it.desc};;${it.pattern};;${it.inhale};;${it.hold};;${it.exhale};;${it.rest}" 
        }
        prefs.edit { putString("custom_breathe_styles", serialized) }
        _customMethods.value = list
    }

    fun addCustomMethod(context: android.content.Context, item: MethodItem) {
        val current = _customMethods.value.toMutableList()
        current.removeAll { it.name.equals(item.name, ignoreCase = true) }
        current.add(item)
        saveCustomMethods(context, current)
    }

    fun editCustomMethod(context: android.content.Context, oldName: String, newItem: MethodItem) {
        val current = _customMethods.value.toMutableList()
        current.removeAll { it.name.equals(oldName, ignoreCase = true) || it.name.equals(newItem.name, ignoreCase = true) }
        current.add(newItem)
        saveCustomMethods(context, current)
    }

    fun deleteCustomMethod(context: android.content.Context, name: String) {
        val current = _customMethods.value.toMutableList()
        current.removeAll { it.name.equals(name, ignoreCase = true) }
        saveCustomMethods(context, current)
    }
}
