package com.example.ataraxia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.dao.BreatheDao
import com.example.ataraxia.data.local.dao.FocusDao
import com.example.ataraxia.data.local.dao.JournalDao
import com.example.ataraxia.data.local.dao.MoodLogDao
import com.example.ataraxia.data.local.entity.MoodLogEntity
import com.example.ataraxia.data.preferences.SanctuaryPreferences
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val preferences: SanctuaryPreferences,
    private val journalDao: JournalDao,
    private val breatheDao: BreatheDao,
    private val focusDao: FocusDao,
    private val moodLogDao: MoodLogDao
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), preferences.initialThemeMode)

    init {
        viewModelScope.launch {
            preferences.initializeDefaultThemeIfNecessary()
        }
    }

    // Dynamic Time-of-Day Greeting
    private val _greeting = MutableStateFlow(calculateGreeting(username.value))
    val greeting: StateFlow<GreetingInfo> = _greeting.asStateFlow()

    fun refreshGreeting() {
        _greeting.value = calculateGreeting(username.value)
    }

    private fun calculateGreeting(name: String): GreetingInfo {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val title = when (hour) {
            in 5..11 -> "Good Morning, ${name.ifEmpty { "friend" }} ☀️"
            in 12..16 -> "Good Afternoon, ${name.ifEmpty { "friend" }} 🌿"
            in 17..20 -> "Good Evening, ${name.ifEmpty { "friend" }} 🌙"
            else -> "Burning the midnight oil? 💡"
        }
        val subtitle = when (hour) {
            in 5..11 -> "Let's begin today with clarity."
            in 12..16 -> "Remember to take a small pause today."
            in 17..20 -> "Take a moment to reflect on today."
            else -> "Be kind to yourself."
        }
        return GreetingInfo(title, subtitle)
    }

    // Daily Reflection Prompt
    private val prompts = listOf(
        "What made you smile today?",
        "What challenged you?",
        "What are you grateful for?",
        "What is one thing you're proud of today?",
        "What do you need right now?",
        "Who brought you a moment of comfort today?",
        "What is a soft sound you heard today?",
        "How did you practice patience today?",
        "What is a lesson today taught you?",
        "What can you let go of before tomorrow?",
        "What made you feel peaceful today?",
        "What is one small victory you experienced today?"
    )

    val dailyPrompt: String = prompts[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % prompts.size]

    // Today's Mood Check-in (retrieved reactively from Room DB, overwrites if updated on same day)
    val todayMood: StateFlow<String> = moodLogDao.getAllMoodLogsFlow()
        .map { logs ->
            val todayDateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            logs.firstOrNull { it.dateStr == todayDateStr }?.mood ?: ""
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveTodayMood(mood: String) {
        viewModelScope.launch {
            val todayDateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            moodLogDao.insertMoodLog(MoodLogEntity(todayDateStr, mood, System.currentTimeMillis()))
        }
    }

    // Today's Stats & Overview Aggregation
    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val todayJournalCount: StateFlow<Int> = journalDao.getAllEntries()
        .map { entries ->
            val todayStart = getTodayStartMillis()
            entries.count { it.timestamp >= todayStart }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayFocusMinutes: StateFlow<Int> = focusDao.getAllSessions()
        .map { sessions ->
            val todayStart = getTodayStartMillis()
            sessions.filter { it.timestamp >= todayStart }.sumOf { it.durationMinutes }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayBreatheMinutes: StateFlow<Int> = breatheDao.getAllSessions()
        .map { sessions ->
            val todayStart = getTodayStartMillis()
            val totalSecs = sessions.filter { it.timestamp >= todayStart }.sumOf { it.durationSeconds }
            (totalSecs + 59) / 60
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Overall / Weekly Statistics
    val daysActive: StateFlow<Int> = combine(
        journalDao.getAllEntries(),
        breatheDao.getAllSessions(),
        focusDao.getAllSessions()
    ) { entries, breatheSessions, focusSessions ->
        val uniqueDays = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        entries.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        breatheSessions.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        focusSessions.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        uniqueDays.size
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val thisWeeksReflectionsCount: StateFlow<Int> = journalDao.getAllEntries()
        .map { entries ->
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            entries.count { it.timestamp >= sevenDaysAgo }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBreatheMinutes: StateFlow<Int> = breatheDao.getAllSessions()
        .map { sessions ->
            val totalSecs = sessions.sumOf { it.durationSeconds }
            (totalSecs + 59) / 60
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Recent Activity Aggregator
    val recentActivity: StateFlow<List<RecentActivityItem>> = combine(
        journalDao.getAllEntries(),
        breatheDao.getAllSessions(),
        focusDao.getAllSessions()
    ) { entries, breatheSessions, focusSessions ->
        val items = mutableListOf<RecentActivityItem>()
        entries.firstOrNull()?.let {
            items.add(RecentActivityItem("Journal", it.title.ifEmpty { "Reflection" }, it.timestamp))
        }
        breatheSessions.firstOrNull()?.let {
            val method = it.method
            val displayTitle = if (method.contains("Breathing", ignoreCase = true)) method else "$method Breathing"
            items.add(RecentActivityItem("Breathe", displayTitle, it.timestamp))
        }
        focusSessions.firstOrNull()?.let {
            val space = it.spaceName
            val displayTitle = if (space.contains("Session", ignoreCase = true)) space else "$space Session"
            items.add(RecentActivityItem("Focus", displayTitle, it.timestamp))
        }
        items.sortedByDescending { it.timestamp }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val allMoodLogs: StateFlow<List<MoodLogEntity>> = moodLogDao.getAllMoodLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentHabitStreak: StateFlow<Int> = combine(
        journalDao.getAllEntries(),
        breatheDao.getAllSessions(),
        focusDao.getAllSessions(),
        moodLogDao.getAllMoodLogsFlow()
    ) { entries, breatheSessions, focusSessions, moodLogs ->
        val uniqueDays = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        entries.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        breatheSessions.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        focusSessions.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        moodLogs.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        
        calculateStreakFromDays(uniqueDays)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val amoledMode: StateFlow<Boolean> = preferences.amoledModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dynamicColors: StateFlow<Boolean> = preferences.dynamicColorsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reducedMotion: StateFlow<Boolean> = preferences.reducedMotionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val biometricsEnabled: StateFlow<Boolean> = preferences.biometricsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val mindfulUsagePermission: StateFlow<Boolean> = preferences.mindfulUsagePermissionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderDaily: StateFlow<Boolean> = preferences.reminderDailyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderJournal: StateFlow<Boolean> = preferences.reminderJournalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderBreathe: StateFlow<Boolean> = preferences.reminderBreatheFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderFocus: StateFlow<Boolean> = preferences.reminderFocusFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderMindfulness: StateFlow<Boolean> = preferences.reminderMindfulnessFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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

    fun updateAmoledMode(enabled: Boolean) {
        viewModelScope.launch { preferences.saveAmoledMode(enabled) }
    }
    fun updateDynamicColors(enabled: Boolean) {
        viewModelScope.launch { preferences.saveDynamicColors(enabled) }
    }
    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch { preferences.saveReducedMotion(enabled) }
    }
    fun updateBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.saveBiometricsEnabled(enabled) }
    }
    fun updateMindfulUsagePermission(enabled: Boolean) {
        viewModelScope.launch { preferences.saveMindfulUsagePermission(enabled) }
    }
    fun updateReminderDaily(enabled: Boolean) {
        viewModelScope.launch { preferences.saveReminderDaily(enabled) }
    }
    fun updateReminderJournal(enabled: Boolean) {
        viewModelScope.launch { preferences.saveReminderJournal(enabled) }
    }
    fun updateReminderBreathe(enabled: Boolean) {
        viewModelScope.launch { preferences.saveReminderBreathe(enabled) }
    }
    fun updateReminderFocus(enabled: Boolean) {
        viewModelScope.launch { preferences.saveReminderFocus(enabled) }
    }
    fun updateReminderMindfulness(enabled: Boolean) {
        viewModelScope.launch { preferences.saveReminderMindfulness(enabled) }
    }

    private fun calculateStreakFromDays(uniqueDays: Set<String>): Int {
        if (uniqueDays.isEmpty()) return 0
        val sortedDays = uniqueDays.map { it.toInt() }.sortedDescending()
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        val today = sdf.format(Date()).toInt()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.let { sdf.format(it.time).toInt() }
        
        val newestDay = sortedDays.first()
        if (newestDay != today && newestDay != yesterday) {
            return 0
        }
        
        var streak = 1
        val currentDayCal = Calendar.getInstance().apply {
            val newestStr = newestDay.toString()
            set(Calendar.YEAR, newestStr.take(4).toInt())
            set(Calendar.MONTH, newestStr.substring(4, 6).toInt() - 1)
            set(Calendar.DAY_OF_MONTH, newestStr.takeLast(2).toInt())
        }
        
        for (i in 1 until sortedDays.size) {
            currentDayCal.add(Calendar.DATE, -1)
            val expectedDay = sdf.format(currentDayCal.time).toInt()
            if (sortedDays[i] == expectedDay) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferences.clearAllPreferences()
            journalDao.clearAllEntries()
            breatheDao.clearAllSessions()
            focusDao.clearAllSessions()
            moodLogDao.clearAllMoodLogs()
        }
    }
}

data class GreetingInfo(val title: String, val subtitle: String)

data class RecentActivityItem(
    val type: String, // "Journal", "Breathe", "Focus"
    val title: String,
    val timestamp: Long,
    val extra: String = ""
)
