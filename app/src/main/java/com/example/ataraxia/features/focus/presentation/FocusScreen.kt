package com.example.ataraxia.features.focus.presentation

import androidx.activity.compose.BackHandler
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.ScreenEnclosure
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

    // Expanded intention states
    val intentions by viewModel.allIntentions.collectAsState()
    val context = LocalContext.current
    val sortedIntentions = remember(intentions) {
        val savedOrder = context.getSharedPreferences("ataraxia_prefs", Context.MODE_PRIVATE)
            .getString("focus_intentions_order", "") ?: ""
        if (savedOrder.isBlank()) {
            intentions
        } else {
            val orderMap = savedOrder.split(",").mapIndexed { idx, name -> name to idx }.toMap()
            intentions.sortedBy { orderMap[it.name] ?: 999 }
        }
    }
    var selectedIntention by remember { mutableStateOf<FocusIntentionEntity?>(null) }
    var showIntentionDialog by remember { mutableStateOf(false) }

    // Sensory settings
    var enableHalfwayReminder by remember { mutableStateOf(true) }
    var enableRemindersIntervalMins by remember { mutableIntStateOf(0) } // 0 = disabled

    // Post-session logs payload details
    var showNotesDialog by remember { mutableStateOf(false) }
    var pendingDurationMins by remember { mutableStateOf(0) }
    var pendingIsFlowMode by remember { mutableStateOf(false) }
    var pendingTargetMins by remember { mutableStateOf(0) }
    var pendingCompletionStatus by remember { mutableStateOf("Completed") }

    // Top-right insights dialog trigger state
    var showInsightsDialog by remember { mutableStateOf(false) }

    var showCalendarDialog by remember { mutableStateOf(false) }
    var showDndPermissionDialog by remember { mutableStateOf(false) }
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }

    // History sorting & filtering
    var sortOrder by remember { mutableStateOf("Newest") } // "Newest", "Oldest", "Longest"
    var selectedIntentionFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    val sessions by viewModel.allSessions.collectAsState()
    val displayedSessions = remember(sessions, selectedDayNum, calendarMonth, calendarYear, sortOrder, selectedIntentionFilter, selectedStatusFilter) {
        val base = if (selectedDayNum == null) {
            sessions
        } else {
            sessions.filter { session ->
                val sessionCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
                sessionCal.get(Calendar.YEAR) == calendarYear &&
                sessionCal.get(Calendar.MONTH) == calendarMonth &&
                sessionCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
            }
        }

        // Apply intention and status filters
        val filtered = base.filter { session ->
            (selectedIntentionFilter == null || session.intentionName == selectedIntentionFilter) &&
            (selectedStatusFilter == null || session.completionStatus == selectedStatusFilter)
        }

        when (sortOrder) {
            "Newest" -> filtered.sortedByDescending { it.timestamp }
            "Oldest" -> filtered.sortedBy { it.timestamp }
            "Longest" -> filtered.sortedByDescending { it.durationMinutes }
            else -> filtered.sortedByDescending { it.timestamp }
        }
    }

    val currentStreak = remember(sessions) { calculateFocusStreak(sessions) }

    // Fallback intention selection setup on data load
    LaunchedEffect(sortedIntentions) {
        if (sortedIntentions.isNotEmpty() && selectedIntention == null) {
            selectedIntention = sortedIntentions.firstOrNull { it.name == "Study" } ?: sortedIntentions.first()
        }
    }

    fun startFocusSession() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            showDndPermissionDialog = true
        } else {
            isSpaceActive = true
        }
    }

    LaunchedEffect(isSpaceActive, showNotesDialog) {
        onSpaceActiveChanged(isSpaceActive || showNotesDialog)
    }

    // Intercept back button when focus space is active or notes/insights dialog is shown
    BackHandler(enabled = isSpaceActive || showNotesDialog || showInsightsDialog) {
        if (showNotesDialog) {
            showNotesDialog = false
        } else if (showInsightsDialog) {
            showInsightsDialog = false
        } else if (isSpaceActive) {
            isSpaceActive = false
            onSpaceActiveChanged(false)
        }
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
            ScreenEnclosure {
                // Header with Analytics button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
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
                    IconButton(onClick = { showInsightsDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Focus Insights",
                            tint = DesignTokens.TextPrimary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(idleScrollState)
                ) {


                    // Streak Banner Card
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

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                    // CARD 1: Session Intention Selector (Workspace Location completely removed)
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Session Intention",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DesignTokens.TextSecondary
                                )
                                Text(
                                    text = "Change Intention",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showIntentionDialog = true }
                                )
                            }
                            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                            // Visual Intention Carousel Highlight
                            selectedIntention?.let { intention ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .clickable { showIntentionDialog = true }
                                        .padding(14.dp)
                                ) {
                                    Text(text = intention.icon, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = intention.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (intention.description.isNotEmpty()) {
                                            Text(
                                                text = intention.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DesignTokens.TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                    // CARD 2: Focus Mode + Duration + Enter Sanctuary Button
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                            // Focus Mode Selector (Sliding Segmented Toggle)
                            Column {
                                Text(
                                    text = "Focus Mode",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DesignTokens.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(percent = 50)
                                        )
                                        .padding(4.dp)
                                ) {
                                    val totalWidth = maxWidth
                                    val selectedIndex = if (focusMode == "Timer") 0 else 1
                                    val slideWidth = totalWidth / 2
                                    
                                    val offsetAnimation by androidx.compose.animation.core.animateDpAsState(
                                        targetValue = slideWidth * selectedIndex,
                                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.85f, stiffness = 400f),
                                        label = "focus_mode_slide"
                                    )

                                    // Sliding Background Indicator
                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetAnimation.roundToPx(), 0) }
                                            .width(slideWidth)
                                            .fillMaxHeight()
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(percent = 50)
                                            )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Countdown Mode Toggle Box
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(percent = 50))
                                                .clickable { focusMode = "Timer" },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val textColor by animateColorAsState(
                                                targetValue = if (focusMode == "Timer") MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                                label = "timer_toggle_text_color"
                                            )
                                            Text(
                                                text = "Countdown Mode",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = textColor
                                            )
                                        }

                                        // Flow Mode Toggle Box
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(percent = 50))
                                                .clickable { focusMode = "Flow" },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val textColor by animateColorAsState(
                                                targetValue = if (focusMode == "Flow") MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                                label = "flow_toggle_text_color"
                                            )
                                            Text(
                                                text = "Flow Mode",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }

                            // Countdown mode duration slider
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

                            Spacer(modifier = Modifier.height(4.dp))

                            AtaraxiaPrimaryButton(
                                text = "Enter Sanctuary",
                                onClick = { startFocusSession() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                    // CARD 3: Settings card (toggles + header inside)
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space20)) {
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )

                            // Switches for wake lock & calls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Allow Calls", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextPrimary)
                                    Text(text = "Allow incoming calls to ring through DND.", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                }
                                Switch(
                                    checked = allowCalls,
                                    onCheckedChange = { allowCalls = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Keep Screen Awake", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextPrimary)
                                    Text(text = "Keep active display lit during focus.", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                }
                                Switch(
                                    checked = keepScreenAwake,
                                    onCheckedChange = { keepScreenAwake = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            // Dynamic sensory reminders switches
                            if (focusMode == "Timer") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Halfway Reminder", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextPrimary)
                                        Text(text = "Soft vibration alert when 50% time remains.", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                    }
                                    Switch(
                                        checked = enableHalfwayReminder,
                                        onCheckedChange = { enableHalfwayReminder = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            } else {
                                // Flow mode interval reminders
                                Column {
                                    Text(text = "Flow Interval Reminders", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextPrimary)
                                    Text(text = "Optional soft haptic tick to mark intervals without alert sounds.", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val intervalOptions = listOf(0, 15, 30, 60)
                                        intervalOptions.forEach { mins ->
                                            val isSel = enableRemindersIntervalMins == mins
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                                    .clickable { enableRemindersIntervalMins = mins }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (mins == 0) "None" else "${mins}m",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                    // History Logs Timeline layout (displayed directly, tabs removed)
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                            // Filter & Sort Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val monthName = DateFormatSymbols().months[calendarMonth]
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedDayNum != null) "Focus Logs: $monthName $selectedDayNum" else "Focus Logs",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DesignTokens.TextPrimary
                                    )
                                    Text(
                                        text = "Manage your session logs offline.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignTokens.TextSecondary
                                    )
                                }
                                if (selectedDayNum != null) {
                                    Text(
                                        text = "Clear Date",
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

                            // Interactive Sorting & Filtering (nested in tight Column to minimize vertical space wastage)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Interactive Sorting Row 1: Sorts
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val sorts = listOf("Newest", "Oldest", "Longest")
                                    sorts.forEach { order ->
                                        val isSel = sortOrder == order
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                                .clickable { sortOrder = order }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = order, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary)
                                        }
                                    }
                                }

                                // Interactive Sorting Row 2: Filters (Intention categories, Completed, Cancelled)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Filter Intention
                                    val uniqueIntentions = remember(sessions) {
                                        sessions.map { it.intentionName }.distinct()
                                    }
                                    uniqueIntentions.forEach { filterInt ->
                                        val isSel = selectedIntentionFilter == filterInt
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                                .clickable { selectedIntentionFilter = if (isSel) null else filterInt }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = filterInt, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary)
                                        }
                                    }

                                    // Filter Status
                                    val statuses = listOf("Completed", "Cancelled")
                                    statuses.forEach { filterStat ->
                                        val isSel = selectedStatusFilter == filterStat
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                                                .clickable { selectedStatusFilter = if (isSel) null else filterStat }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = filterStat, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary)
                                        }
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
                                    title = "No sessions found",
                                    subtitle = "Every meaningful achievement begins with a moment of focused attention."
                                )
                            } else {
                                FocusHistoryTimeline(
                                    sessions = displayedSessions
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(130.dp))
                }
            }
        } else if (isSpaceActive) {
            selectedIntention?.let { intention ->
                FocusActiveSession(
                    selectedSpace = "Focus Sanctuary",
                    focusMode = focusMode,
                    targetMinutes = targetMinutes,
                    selectedIntention = intention,
                    keepScreenAwake = keepScreenAwake,
                    allowCalls = allowCalls,
                    enableHalfwayReminder = enableHalfwayReminder,
                    enableRemindersIntervalMins = enableRemindersIntervalMins,
                    onEndSession = { elapsed, isFlow, status ->
                        pendingDurationMins = elapsed
                        pendingIsFlowMode = isFlow
                        pendingTargetMins = if (isFlow) 0 else targetMinutes
                        pendingCompletionStatus = status
                        isSpaceActive = false
                        showNotesDialog = true
                    }
                )
            }
        }

        // Expanded Intention Selector Dialog
        selectedIntention?.let { intention ->
            FocusIntentionSelectorDialog(
                showDialog = showIntentionDialog,
                intentions = sortedIntentions,
                selectedIntention = intention,
                onDismiss = { showIntentionDialog = false },
                onIntentionSelected = { selectedIntention = it },
                onAddCustomIntention = { name, icon, color, desc ->
                    viewModel.addCustomIntention(name, icon, color, desc)
                }
            )
        }

        // Post-Session Notes dialog
        selectedIntention?.let { intention ->
            FocusPostSessionDialog(
                showNotesDialog = showNotesDialog,
                pendingDurationMins = pendingDurationMins,
                pendingIsFlowMode = pendingIsFlowMode,
                pendingTargetMins = pendingTargetMins,
                selectedSpace = "Workspace",
                selectedIntention = intention,
                completionStatus = pendingCompletionStatus,
                onNotesSaved = { notes, enjoyed, distracted, rating, status ->
                    viewModel.logSession(
                        durationMinutes = pendingDurationMins,
                        spaceName = "Workspace",
                        notes = notes,
                        isFlowMode = pendingIsFlowMode,
                        targetMinutes = pendingTargetMins,
                        intentionName = intention.name,
                        intentionIcon = intention.icon,
                        intentionColorHex = intention.colorHex,
                        completionStatus = status,
                        reflectionEnjoyed = enjoyed,
                        reflectionDistracted = distracted,
                        reflectionFocusRate = rating
                    )
                    showNotesDialog = false
                }
            )
        }

        // DND policy permission Dialog
        if (showDndPermissionDialog) {
            Dialog(onDismissRequest = { showDndPermissionDialog = false }) {
                LunafloraCard(modifier = Modifier.fillMaxWidth().padding(horizontal = AtaraxiaTheme.spacing.Space8)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "Do Not Disturb Permission Required", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary, textAlign = TextAlign.Center)
                        Text(text = "To manage incoming notifications, Ataraxia needs permission to toggle Do Not Disturb control. Open settings to grant permission?", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaSecondaryButton(text = "No", onClick = { showDndPermissionDialog = false })
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaPrimaryButton(text = "Yes", onClick = {
                                    showDndPermissionDialog = false
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                                    } catch (_: Exception) {}
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

        // Focus Insights Dialog (Popup Statistics view)
        FocusInsightsDialog(
            showDialog = showInsightsDialog,
            onDismiss = { showInsightsDialog = false },
            sessions = sessions
        )
    }
}

@Composable
fun FocusInsightsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    sessions: List<FocusSessionEntity>
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            val currentView = androidx.compose.ui.platform.LocalView.current
            var window: android.view.Window? = null
            var parentView = currentView.parent
            while (parentView != null) {
                if (parentView is androidx.compose.ui.window.DialogWindowProvider) {
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
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = AtaraxiaTheme.spacing.Space8)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                          Text(
                              text = "🌿 Focus Insights",
                              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                              color = DesignTokens.TextPrimary
                          )
                          IconButton(onClick = onDismiss) {
                              Icon(
                                  imageVector = Icons.Default.Close,
                                  contentDescription = "Close",
                                  tint = DesignTokens.TextSecondary
                              )
                          }
                      }
                      
                      Box(
                          modifier = Modifier
                              .weight(1f)
                              .verticalScroll(rememberScrollState())
                      ) {
                          FocusStatistics(sessions = sessions)
                      }
                  }
              }
          }
      }
}

@Composable
private fun FocusHistoryTimeline(
    sessions: List<FocusSessionEntity>
) {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("yyyyMMdd", locale) }
    val displaySdf = remember(locale) { SimpleDateFormat("MMMM d, yyyy", locale) }
    val timeSdf = remember(locale) { SimpleDateFormat("h:mm a", locale) }
    val grouped = remember(sessions, sdf, displaySdf) {
        val cal = Calendar.getInstance()
        sessions.groupBy { session ->
            val dateKey = sdf.format(Date(session.timestamp))
            try {
                val y = dateKey.substring(0, 4).toInt()
                val m = dateKey.substring(4, 6).toInt() - 1
                val d = dateKey.substring(6, 8).toInt()
                cal.set(y, m, d)
                displaySdf.format(cal.time)
            } catch (_: Exception) {
                dateKey
            }
        }.toSortedMap(reverseOrder())
    }

    var selectedSessionForDetail by remember { mutableStateOf<FocusSessionEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
        grouped.forEach { (displayDate, daySessions) ->
            Text(text = displayDate, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextSecondary, modifier = Modifier.padding(vertical = 2.dp))

            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                daySessions.forEach { session ->
                    LunafloraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSessionForDetail = session }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = session.intentionIcon, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = session.intentionName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                                    val timeStr = timeSdf.format(Date(session.timestamp))
                                    Text(text = "• $timeStr", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                val durationLabel = if (session.isFlowMode) {
                                    "${session.durationMinutes} Min Focus (Flow)"
                                } else {
                                    val stoppedMidway = session.targetMinutes > 0 && session.durationMinutes < session.targetMinutes
                                    if (stoppedMidway) {
                                        "${session.durationMinutes} Min Focus (Target: ${session.targetMinutes}m - Cancelled midway)"
                                    } else {
                                        "${session.durationMinutes} Min Focus (Target: ${session.targetMinutes}m)"
                                    }
                                }
                                Text(
                                    text = durationLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedSessionForDetail?.let { session ->
        FocusSessionDetailDialog(
            session = session,
            onDismiss = { selectedSessionForDetail = null }
        )
    }
}

@Composable
private fun FocusSessionDetailDialog(
    session: FocusSessionEntity,
    onDismiss: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val fullDateSdf = remember(locale) { SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", locale) }

    Dialog(onDismissRequest = onDismiss) {
        LunafloraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AtaraxiaTheme.spacing.Space8)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header (Intention Icon + Name)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = session.intentionIcon, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = session.intentionName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.15f))

                // Session Summary Info Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Date", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                        Text(
                            text = fullDateSdf.format(Date(session.timestamp)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesignTokens.TextPrimary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                    }

                    // Duration details
                    val modeLabel = if (session.isFlowMode) "Flow Mode" else "Countdown Mode"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Duration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                        val durationStr = if (session.isFlowMode) {
                            "${session.durationMinutes} minutes"
                        } else {
                            "${session.durationMinutes}m (Target: ${session.targetMinutes}m)"
                        }
                        Text(text = durationStr, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary)
                    }

                    // Focus Mode label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Focus Mode", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                        Text(text = modeLabel, style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                    }

                    // Completion Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Status", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                        Text(
                            text = session.completionStatus,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (session.completionStatus == "Completed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    // Focus Rating (Stars)
                    if (session.reflectionFocusRate > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Focus Rating", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= session.reflectionFocusRate) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = if (star <= session.reflectionFocusRate) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Reflection Answers
                if (session.reflectionEnjoyed.isNotEmpty() || session.reflectionDistracted.isNotEmpty() || session.notes.isNotEmpty()) {
                    HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.15f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (session.reflectionEnjoyed.isNotEmpty()) {
                            Column {
                                Text(text = "What went well today?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = session.reflectionEnjoyed, style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                            }
                        }

                        if (session.reflectionDistracted.isNotEmpty()) {
                            Column {
                                Text(text = "What distracted you?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = session.reflectionDistracted, style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                            }
                        }

                        if (session.notes.isNotEmpty()) {
                            Column {
                                Text(text = "Reflection Notes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = DesignTokens.TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "\"${session.notes}\"", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = DesignTokens.TextPrimary)
                            }
                        }
                    }
                }

                HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.15f))

                // Close Button
                AtaraxiaPrimaryButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
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
    return sessions.filter { sdf.format(Date(it.timestamp)) == today && it.completionStatus == "Completed" }
        .sumOf { it.durationMinutes }
}

private fun calculateWeekMinutes(sessions: List<FocusSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val weekStart = cal.timeInMillis
    return sessions.filter { it.timestamp >= weekStart && it.completionStatus == "Completed" }.sumOf { it.durationMinutes }
}

private fun calculateMonthMinutes(sessions: List<FocusSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val monthStart = cal.timeInMillis
    return sessions.filter { it.timestamp >= monthStart && it.completionStatus == "Completed" }.sumOf { it.durationMinutes }
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
