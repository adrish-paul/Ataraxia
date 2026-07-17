package com.example.ataraxia.features.focus.presentation

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onSpaceActiveChanged: (Boolean) -> Unit,
    scrollToTopKey: Int = 0
) {
    var isSpaceActive by remember { mutableStateOf(false) }
    var allowCalls by remember { mutableStateOf(true) }
    var keepScreenAwake by remember { mutableStateOf(false) }

    var focusMode by remember { mutableStateOf("Timer") }
    var targetMinutes by remember { mutableStateOf(25) }

    var showNotesDialog by remember { mutableStateOf(false) }
    var pendingDurationMins by remember { mutableStateOf(0) }
    var pendingIsFlowMode by remember { mutableStateOf(false) }
    var pendingTargetMins by remember { mutableStateOf(0) }

    var showCalendarDialog by remember { mutableStateOf(false) }
    var showDndPermissionDialog by remember { mutableStateOf(false) }
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    val sessions by viewModel.allSessions.collectAsState()
    val displayedSessions = remember(sessions, selectedDayNum, calendarMonth, calendarYear) {
        if (selectedDayNum == null) {
            sessions
        } else {
            sessions.filter { session ->
                val sessionCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
                sessionCal.get(Calendar.YEAR) == calendarYear &&
                sessionCal.get(Calendar.MONTH) == calendarMonth &&
                sessionCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
            }
        }
    }
    val currentStreak = remember(sessions) { calculateFocusStreak(sessions) }

    var selectedSpace by remember { mutableStateOf("Study Space") }
    val spaces = listOf("Study Space", "Work Space", "Read Space", "Meditate Space", "Rest Space")

    fun startFocusSession() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            showDndPermissionDialog = true
        } else {
            isSpaceActive = true
        }
    }

    fun endSessionAndPromptNotes(elapsedSecs: Int, isFlow: Boolean) {
        val totalMins = elapsedSecs / 60
        if (totalMins >= 1 || elapsedSecs >= 5) {
            pendingDurationMins = if (totalMins == 0) 1 else totalMins
            pendingIsFlowMode = isFlow
            pendingTargetMins = if (isFlow) 0 else targetMinutes
            isSpaceActive = false
            showNotesDialog = true
        } else {
            isSpaceActive = false
        }
    }

    LaunchedEffect(isSpaceActive) {
        onSpaceActiveChanged(isSpaceActive)
    }

    val idleScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0 && !isSpaceActive) idleScrollState.animateScrollTo(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
    ) {
        if (!isSpaceActive && !showNotesDialog) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .verticalScroll(idleScrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AtaraxiaTheme.spacing.Space16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Spa,
                            contentDescription = "Focus",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                        Text(
                            text = "Focus",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = DesignTokens.TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                // Stats Banner
                val todayMins = remember(sessions) { calculateTodayMinutes(sessions) }
                val weekMins = remember(sessions) { calculateWeekMinutes(sessions) }
                val monthMins = remember(sessions) { calculateMonthMinutes(sessions) }

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (currentStreak > 0) "🔥" else "✨",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (currentStreak > 0) "$currentStreak Day Streak" else "Start your first focus session",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FocusStatCell(label = "Today", value = formatFocusTime(todayMins))
                            FocusStatDivider()
                            FocusStatCell(label = "This Week", value = formatFocusTime(weekMins))
                            FocusStatDivider()
                            FocusStatCell(label = "This Month", value = formatFocusTime(monthMins))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

                // Mode selection & Configurations Card
                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space20)) {
                        // Focus Space selector
                        Column {
                            Text(
                                text = "Focus Space",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = DesignTokens.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                spaces.take(3).forEach { space ->
                                    val isSelected = selectedSpace == space
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                            .clickable { selectedSpace = space }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = space.split(" ")[0], style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                spaces.drop(3).forEach { space ->
                                    val isSelected = selectedSpace == space
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                            .clickable { selectedSpace = space }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = space.split(" ")[0], style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary)
                                    }
                                }
                                Box(modifier = Modifier.weight(1f))
                            }
                        }

                        // Mode Selector Chips
                        Column {
                            Text(
                                text = "Focus Type",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = DesignTokens.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val modes = listOf("Timer", "Flow")
                                modes.forEach { mode ->
                                    val isSelected = focusMode == mode
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable { focusMode = mode }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = mode, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary)
                                    }
                                }
                            }
                        }

                        // Timer Mode Configs
                        if (focusMode == "Timer") {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Duration",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = DesignTokens.TextSecondary
                                    )
                                    Text(
                                        text = "$targetMinutes min",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = targetMinutes.toFloat(),
                                    onValueChange = { targetMinutes = it.toInt().coerceIn(5, 180) },
                                    valueRange = 5f..180f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = DesignTokens.TextSecondary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }

                        // Switches for Call Allowance & Screen Awake
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Allow Calls", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextPrimary)
                                    Text(text = "Unmute ringtone alerts during focus.", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                }
                                Switch(checked = allowCalls, onCheckedChange = { allowCalls = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Keep Screen Awake", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextPrimary)
                                    Text(text = "Prevent device display from sleeping.", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                }
                                Switch(checked = keepScreenAwake, onCheckedChange = { keepScreenAwake = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
                            }
                        }

                        AtaraxiaPrimaryButton(
                            text = "Enter Space",
                            onClick = { startFocusSession() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

                // Sessions Logs Card
                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val monthName = DateFormatSymbols().months[calendarMonth]
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedDayNum != null) "Focus Logs: $monthName $selectedDayNum" else "Recent Sessions",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = if (selectedDayNum != null) "Focus logs on selected calendar day." else "Your focus history.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignTokens.TextSecondary
                                )
                            }
                            if (selectedDayNum != null) {
                                Text(
                                    text = "Show All",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { selectedDayNum = null }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            } else {
                                IconButton(onClick = { showCalendarDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = "View Calendar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        if (displayedSessions.isEmpty()) {
                            EmptyState(
                                illustration = {
                                    Icon(
                                        imageVector = Icons.Outlined.Spa,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                },
                                title = if (selectedDayNum != null) "No sessions today" else "No focus sessions logged yet",
                                subtitle = if (selectedDayNum != null) "You didn't log any focus sessions on this day." else "Start focusing to log your quiet sessions."
                            )
                        } else {
                            FocusHistoryTimeline(sessions = displayedSessions, onDelete = { id -> viewModel.deleteFocusSession(id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(130.dp))
            }
        } else if (isSpaceActive) {
            FocusActiveSession(
                selectedSpace = selectedSpace,
                focusMode = focusMode,
                keepScreenAwake = keepScreenAwake,
                allowCalls = allowCalls,
                onEndSession = { elapsed, isFlow ->
                    endSessionAndPromptNotes(elapsed, isFlow)
                }
            )
        }

        // Post-Session Notes dialog
        FocusPostSessionDialog(
            showNotesDialog = showNotesDialog,
            onNotesSaved = { notes ->
                viewModel.logSession(pendingDurationMins, selectedSpace, notes, pendingIsFlowMode, pendingTargetMins)
                showNotesDialog = false
            }
        )

        // DND policy permission Dialog
        if (showDndPermissionDialog) {
            Dialog(onDismissRequest = { showDndPermissionDialog = false }) {
                LunafloraCard(modifier = Modifier.fillMaxWidth().padding(horizontal = AtaraxiaTheme.spacing.Space8)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "Do Not Disturb Permission Required", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary, textAlign = TextAlign.Center)
                        Text(text = "To allow silent sessions and manage incoming notifications, Ataraxia needs permission to toggle Do Not Disturb control. Open settings to grant permission?", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaSecondaryButton(text = "No", onClick = { showDndPermissionDialog = false })
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaPrimaryButton(text = "Yes", onClick = {
                                    showDndPermissionDialog = false
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }

        // Calendar Dialog
        FocusCalendarDialog(
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
    }
}

@Composable
private fun FocusHistoryTimeline(sessions: List<FocusSessionEntity>, onDelete: (Long) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("yyyyMMdd", locale) }
    val displaySdf = remember(locale) { SimpleDateFormat("MMMM d, yyyy", locale) }
    val grouped = remember(sessions, sdf) {
        sessions.groupBy { sdf.format(Date(it.timestamp)) }.toSortedMap(reverseOrder())
    }

    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
        grouped.forEach { (dateKey, daySessions) ->
            val displayDate = try {
                val y = dateKey.substring(0, 4).toInt()
                val m = dateKey.substring(4, 6).toInt() - 1
                val d = dateKey.substring(6, 8).toInt()
                displaySdf.format(Calendar.getInstance().apply { set(y, m, d) }.time)
            } catch (_: Exception) { dateKey }

            Text(text = displayDate, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextSecondary, modifier = Modifier.padding(vertical = 2.dp))

            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                daySessions.forEach { session ->
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                Column {
                                    Text(text = session.spaceName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary)
                                    val durationLabel = if (session.isFlowMode) {
                                        "${session.durationMinutes} Min Focus (Flow)"
                                    } else {
                                        val stoppedMidway = session.targetMinutes > 0 && session.durationMinutes < session.targetMinutes
                                        if (stoppedMidway) {
                                            "${session.durationMinutes} Min Focus (Target: ${session.targetMinutes} Min - Stopped midway)"
                                        } else {
                                            "${session.durationMinutes} Min Focus"
                                        }
                                    }
                                    Text(text = durationLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    if (session.notes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "\"${session.notes}\"", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = DesignTokens.TextSecondary)
                                    }
                                }
                            }
                            IconButton(onClick = { onDelete(session.id) }) {
                                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = DesignTokens.TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Streak and Time helpers
private fun calculateFocusStreak(sessions: List<FocusSessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val uniqueDays = sessions.map { session ->
        sdf.format(Date(session.timestamp)).toInt()
    }.distinct().sortedDescending()

    if (uniqueDays.isEmpty()) return 0

    val today = sdf.format(Date()).toInt()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.let { sdf.format(it.time).toInt() }

    val newestDay = uniqueDays.first()
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

private fun calculateTodayMinutes(sessions: List<FocusSessionEntity>): Int {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = sdf.format(Date())
    return sessions.filter { sdf.format(Date(it.timestamp)) == today }
        .sumOf { it.durationMinutes }
}

private fun calculateWeekMinutes(sessions: List<FocusSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val weekStart = cal.timeInMillis
    return sessions.filter { it.timestamp >= weekStart }.sumOf { it.durationMinutes }
}

private fun calculateMonthMinutes(sessions: List<FocusSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val monthStart = cal.timeInMillis
    return sessions.filter { it.timestamp >= monthStart }.sumOf { it.durationMinutes }
}

private fun formatFocusTime(totalMinutes: Int): String {
    return when {
        totalMinutes == 0 -> "0m"
        totalMinutes < 60 -> "${totalMinutes}m"
        else -> "${totalMinutes / 60}h ${totalMinutes % 60}m".trimEnd().replace("  ", " ")
    }
}

@Composable
private fun FocusStatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary
        )
    }
}

@Composable
private fun FocusStatDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(DesignTokens.TextSecondary.copy(alpha = 0.2f))
    )
}
