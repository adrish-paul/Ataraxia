package com.example.ataraxia.features.breathe.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BreatheScreenState {
    PREPARATION, ACTIVE, COMPLETION
}

@Composable
fun BreatheScreen(
    viewModel: BreatheViewModel,
    onSessionActiveChanged: (Boolean) -> Unit,
    scrollToTopKey: Int = 0
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessions by viewModel.allSessions.collectAsState()

    val selectedMethod by viewModel.selectedMethod.collectAsState()
    val selectedDurationMinutes by viewModel.selectedDurationMinutes.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val hapticGuidanceEnabled by viewModel.hapticGuidanceEnabled.collectAsState()

    var screenState by remember { mutableStateOf(BreatheScreenState.PREPARATION) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var showCalendarDialog by remember { mutableStateOf(false) }
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }

    var sortOrder by remember { mutableStateOf("Newest") }
    var showInsightsDialog by remember { mutableStateOf(false) }

    val displayedSessions = remember(sessions, selectedDayNum, calendarMonth, calendarYear, sortOrder) {
        val filtered = if (selectedDayNum == null) {
            sessions
        } else {
            sessions.filter { session ->
                val sessionCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
                sessionCal.get(Calendar.YEAR) == calendarYear &&
                sessionCal.get(Calendar.MONTH) == calendarMonth &&
                sessionCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
            }
        }
        when (sortOrder) {
            "Newest"  -> filtered.sortedByDescending { it.timestamp }
            "Oldest"  -> filtered.sortedBy { it.timestamp }
            "Longest" -> filtered.sortedByDescending { it.durationSeconds }
            else      -> filtered.sortedByDescending { it.timestamp }
        }
    }

    LaunchedEffect(screenState) {
        onSessionActiveChanged(screenState == BreatheScreenState.ACTIVE || screenState == BreatheScreenState.COMPLETION)
    }

    // Intercept back button
    BackHandler(enabled = screenState != BreatheScreenState.PREPARATION) {
        if (screenState == BreatheScreenState.COMPLETION) {
            screenState = BreatheScreenState.PREPARATION
        }
    }

    val view = LocalView.current
    DisposableEffect(screenState) {
        if (screenState == BreatheScreenState.ACTIVE) {
            view.keepScreenOn = true
        }
        onDispose { view.keepScreenOn = false }
    }

    // Load custom methods on launch
    LaunchedEffect(Unit) {
        viewModel.loadCustomMethods(context)
    }

    val prebuiltMethods = remember {
        listOf(
            MethodItem("4-7-8", "Dr. Weil's famous sleep and relaxation technique.", "4 - 7 - 8", 4, 7, 8, 0, false),
            MethodItem("Box Breathing", "Equal ratios of inhale, hold, exhale, hold.", "4 - 4 - 4 - 4", 4, 4, 4, 4, false),
            MethodItem("Calm", "Slow, relaxing everyday breathing.", "4 - 2 - 4 - 2", 4, 2, 4, 2, false),
            MethodItem("Custom Cycle", "Balanced everyday centering pattern.", "4 - 4", 4, 4, 0, 0, false),
            MethodItem("Deep Relaxation", "Slow, soothing release of bodily tension.", "4 - 7 - 8 - 2", 4, 7, 8, 2, false),
            MethodItem("Exam Calm", "Quiet your mind before high performance.", "4 - 4 - 6", 4, 4, 6, 0, false),
            MethodItem("Morning Energy", "Invigorating breathing to start your morning.", "3 - 1 - 3", 3, 1, 3, 0, false),
            MethodItem("Sleep Preparation", "Transition your mind into deep sleep.", "4 - 6 - 6 - 2", 4, 6, 6, 2, false),
            MethodItem("Stress Relief", "Rapid tension release for high stress.", "5 - 2 - 5 - 2", 5, 2, 5, 2, false)
        ).sortedBy { it.name.lowercase() }
    }

    val customMethods by viewModel.customMethods.collectAsState()
    val allMethods = remember(customMethods, prebuiltMethods) {
        customMethods.sortedBy { it.name.lowercase() } + prebuiltMethods
    }

    val currentMethodItem = remember(selectedMethod, allMethods) {
        allMethods.firstOrNull { it.name == selectedMethod }
            ?: allMethods.firstOrNull { it.name == "Box Breathing" }
            ?: allMethods.first()
    }

    var showMethodPopup by remember { mutableStateOf(false) }

    val idleScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0 && screenState == BreatheScreenState.PREPARATION) {
            idleScrollState.animateScrollTo(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
    ) {
        when (screenState) {
            BreatheScreenState.PREPARATION -> {
                // Calculate Stats values
                val todayMins = remember(sessions) { calculateTodayBreatheMinutes(sessions) }
                val weekMins  = remember(sessions) { calculateWeekBreatheMinutes(sessions) }
                val monthMins = remember(sessions) { calculateMonthBreatheMinutes(sessions) }
                val currentStreak = remember(sessions) { calculateBreatheStreak(sessions) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        BreathePrepScreen(
                            sessions = sessions,
                            selectedMethod = selectedMethod,
                            onMethodSelected = { viewModel.updateSelectedMethod(it) },
                            selectedDurationMinutes = selectedDurationMinutes,
                            onDurationChanged = { viewModel.updateDuration(it) },
                            selectedSound = selectedSound,
                            onSoundSelected = { viewModel.updateSound(it) },
                            soundVolume = soundVolume,
                            onVolumeChanged = { viewModel.updateVolume(it) },
                            hapticGuidanceEnabled = hapticGuidanceEnabled,
                            onHapticGuidanceToggle = { viewModel.updateHapticGuidance(it) },
                            onBeginSession = { screenState = BreatheScreenState.ACTIVE },
                            showCalendarTrigger = { showCalendarDialog = true },
                            selectedDayNum = selectedDayNum,
                            onClearDayFilter = { selectedDayNum = null },
                            displayedSessions = displayedSessions,
                            onDeleteSession = { id -> viewModel.deleteSession(id) },
                            methods = allMethods,
                            onShowMethodPopup = { showMethodPopup = true },
                            todayMins = todayMins,
                            weekMins = weekMins,
                            monthMins = monthMins,
                            currentStreak = currentStreak,
                            scrollState = idleScrollState,
                            onShowInsights = { showInsightsDialog = true }
                        )
                    }
                }
            }

            BreatheScreenState.ACTIVE -> {
                BreatheActiveSession(
                    selectedMethod = selectedMethod,
                    inhaleSeconds = currentMethodItem.inhale,
                    holdSeconds = currentMethodItem.hold,
                    exhaleSeconds = currentMethodItem.exhale,
                    restSeconds = currentMethodItem.rest,
                    targetDurationMinutes = selectedDurationMinutes,
                    selectedSound = selectedSound,
                    soundVolume = soundVolume,
                    hapticGuidanceEnabled = hapticGuidanceEnabled,
                    onEndSession = { duration, _, completed ->
                        elapsedSeconds = duration
                        if (completed || duration >= 10) {
                            screenState = BreatheScreenState.COMPLETION
                        } else {
                            screenState = BreatheScreenState.PREPARATION
                        }
                    }
                )
            }

            BreatheScreenState.COMPLETION -> {
                BreatheCompletionScreen(
                    durationSeconds = elapsedSeconds,
                    methodName = selectedMethod,
                    onContinue = { mood ->
                        viewModel.logSession(elapsedSeconds, selectedMethod, mood)
                        screenState = BreatheScreenState.PREPARATION
                    }
                )
            }
        }

        // Method Selector Dialog
        BreatheMethodSelector(
            showMethodPopup = showMethodPopup,
            selectedMethod = selectedMethod,
            methods = allMethods,
            onDismiss = { showMethodPopup = false },
            onMethodSelected = {
                viewModel.updateSelectedMethod(it)
                showMethodPopup = false
            },
            onSaveCustomStyle = { viewModel.addCustomMethod(context, it) },
            onEditCustomStyle = { oldName, newItem -> viewModel.editCustomMethod(context, oldName, newItem) },
            onDeleteCustomStyle = { viewModel.deleteCustomMethod(context, it) }
        )

        // Calendar Dialog
        BreatheCalendarDialog(
            showCalendarDialog = showCalendarDialog,
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            selectedDayNum = selectedDayNum,
            sessions = sessions,
            onDismiss = { showCalendarDialog = false },
            onMonthChanged = { month, year ->
                calendarMonth = month
                calendarYear = year
                selectedDayNum = null
            },
            onDaySelected = { day ->
                selectedDayNum = day
                showCalendarDialog = false
            }
        )

        // Insights Dialog
        BreatheInsightsDialog(
            showDialog = showInsightsDialog,
            onDismiss = { showInsightsDialog = false },
            sessions = sessions
        )
    }
}

@Composable
fun BreatheInsightsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    sessions: List<BreatheSessionEntity>
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            val currentView = LocalView.current
            var window: android.view.Window? = null
            var parentView = currentView.parent
            while (parentView != null) {
                if (parentView is DialogWindowProvider) {
                    window = parentView.window
                    break
                }
                parentView = parentView.parent
            }
            window?.let { w ->
                w.setBackgroundDrawableResource(android.R.color.transparent)
                w.decorView.setBackgroundResource(android.R.color.transparent)
                w.setElevation(0f)
                w.decorView.elevation = 0f
            }
            LunafloraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space8)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AtaraxiaTheme.spacing.Space8),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "🌿 Breathing Insights",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    val totalSessions = sessions.size
                    val totalSeconds = sessions.sumOf { it.durationSeconds }
                    val longestSeconds = sessions.maxOfOrNull { it.durationSeconds } ?: 0
                    val favoriteMethod = if (sessions.isEmpty()) "None" else sessions.groupBy { it.method }
                        .maxByOrNull { it.value.size }?.key ?: "None"

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InsightRow("Total Sessions", "$totalSessions")
                        InsightRow("Total Time", formatBreatheDuration(totalSeconds))
                        InsightRow("Favorite Method", favoriteMethod)
                        InsightRow("Longest Practice", formatBreatheDuration(longestSeconds))
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Weekly Activity (Last 7 Days)",
                        style = MaterialTheme.typography.labelSmall,
                        color = DesignTokens.TextSecondary
                    )
                    WeeklyActivityChart(sessions = sessions)

                    AtaraxiaPrimaryButton(
                        text = "Done",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DesignTokens.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WeeklyActivityChart(sessions: List<BreatheSessionEntity>) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = configuration.locales[0]
    val sdf = SimpleDateFormat("EEE", locale)
    val daySdf = SimpleDateFormat("yyyyMMdd", locale)
    
    val days = remember(sessions) {
        val list = mutableListOf<Pair<String, Int>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -6)
        
        for (@Suppress("unused") ignored in 0..6) {
            val dateKey = daySdf.format(cal.time)
            val dayName = sdf.format(cal.time)
            val dayMins = sessions.filter { daySdf.format(Date(it.timestamp)) == dateKey }
                .sumOf { it.durationSeconds } / 60
            list.add(dayName to dayMins)
            cal.add(Calendar.DATE, 1)
        }
        list
    }

    val maxMins = days.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { (name, mins) ->
            val barHeightFraction = mins.toFloat() / maxMins
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DesignTokens.TextSecondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(barHeightFraction)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = DesignTokens.TextSecondary)
                Text(text = "${mins}m", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ── Stats Calculations Helpers ──
private fun calculateBreatheStreak(sessions: List<BreatheSessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val uniqueDays = sessions.map { sdf.format(Date(it.timestamp)).toInt() }
        .distinct()
        .sortedDescending()

    val todayStr = sdf.format(Date()).toInt()
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    val yesterdayStr = sdf.format(cal.time).toInt()

    if (uniqueDays.first() != todayStr && uniqueDays.first() != yesterdayStr) {
        return 0
    }

    var streak = 1
    val currentDayCal = Calendar.getInstance()
    val newestStr = uniqueDays.first().toString()
    try {
        currentDayCal.set(Calendar.YEAR, newestStr.substring(0, 4).toInt())
        currentDayCal.set(Calendar.MONTH, newestStr.substring(4, 6).toInt() - 1)
        currentDayCal.set(Calendar.DAY_OF_MONTH, newestStr.substring(6, 8).toInt())
    } catch (_: Exception) {
        return 1
    }

    for (i in 1 until uniqueDays.size) {
        currentDayCal.add(Calendar.DATE, -1)
        val expectedDay = sdf.format(currentDayCal.time).toInt()
        if (uniqueDays[i] == expectedDay) {
            streak++
        } else {
            break
        }
    }
    return streak
}

private fun calculateTodayBreatheMinutes(sessions: List<BreatheSessionEntity>): Int {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = sdf.format(Date())
    val totalSeconds = sessions.filter { sdf.format(Date(it.timestamp)) == today }
        .sumOf { it.durationSeconds }
    return totalSeconds / 60
}

private fun calculateWeekBreatheMinutes(sessions: List<BreatheSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val weekStart = cal.timeInMillis
    val totalSeconds = sessions.filter { it.timestamp >= weekStart }
        .sumOf { it.durationSeconds }
    return totalSeconds / 60
}

private fun calculateMonthBreatheMinutes(sessions: List<BreatheSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val monthStart = cal.timeInMillis
    val totalSeconds = sessions.filter { it.timestamp >= monthStart }
        .sumOf { it.durationSeconds }
    return totalSeconds / 60
}

private fun formatBreatheDuration(totalSeconds: Int): String = when {
    totalSeconds < 60 -> "< 1m"
    else -> { val m = totalSeconds / 60; "$m min" }
}
