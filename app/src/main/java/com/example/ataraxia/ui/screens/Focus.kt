package com.example.ataraxia.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Spa
import com.example.ataraxia.ui.components.EmptyState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.SectionHeader
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.viewmodel.FocusViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FocusScreen(
    name: String,
    profileImage: String,
    viewModel: FocusViewModel,
    onSpaceActiveChanged: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var isSpaceActive by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var allowCalls by remember { mutableStateOf(true) }
    var keepScreenAwake by remember { mutableStateOf(false) }

    // Focus style: Timer Mode vs Flow Mode
    var focusMode by remember { mutableStateOf("Timer") }
    var targetMinutes by remember { mutableStateOf(25) }

    // Precise timestamp-based background-safe timer logic
    var elapsedSeconds by remember { mutableStateOf(0) }
    var accumulatedSeconds by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(0L) }

    // Notes reflection dialog states
    var showNotesDialog by remember { mutableStateOf(false) }
    var sessionNotesText by remember { mutableStateOf("") }
    var pendingDurationMins by remember { mutableStateOf(0) }
    var pendingIsFlowMode by remember { mutableStateOf(false) }
    var pendingTargetMins by remember { mutableStateOf(0) }

    // Calendar dialog state
    var showCalendarDialog by remember { mutableStateOf(false) }
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    val context = LocalContext.current
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    val sessions by viewModel.allSessions.collectAsState()
    val currentStreak = remember(sessions) { calculateFocusStreak(sessions) }

    val view = LocalView.current
    DisposableEffect(isSpaceActive, keepScreenAwake) {
        if (isSpaceActive && keepScreenAwake) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(isSpaceActive) {
        if (isSpaceActive) {
            elapsedSeconds = 0
            accumulatedSeconds = 0
            isPaused = false
            startTime = System.currentTimeMillis()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    try {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        onSpaceActiveChanged(isSpaceActive)
    }

    LaunchedEffect(isPaused) {
        if (isSpaceActive) {
            if (isPaused) {
                accumulatedSeconds += ((System.currentTimeMillis() - startTime) / 1000).toInt()
                elapsedSeconds = accumulatedSeconds
            } else {
                startTime = System.currentTimeMillis()
            }
        }
    }

    LaunchedEffect(isSpaceActive, isPaused) {
        if (isSpaceActive && !isPaused) {
            while (isActive) {
                delay(500)
                elapsedSeconds = accumulatedSeconds + ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }
    }

    LaunchedEffect(isSpaceActive, isPaused, allowCalls) {
        val enabled = isSpaceActive && !isPaused
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    val filter = if (enabled) {
                        if (allowCalls) {
                            NotificationManager.INTERRUPTION_FILTER_PRIORITY
                        } else {
                            NotificationManager.INTERRUPTION_FILTER_NONE
                        }
                    } else {
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    }
                    notificationManager.setInterruptionFilter(filter)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Space DND active trigger

    fun endSessionAndPromptNotes() {
        if (isSpaceActive) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val finalElapsed = if (!isPaused) {
                accumulatedSeconds + ((System.currentTimeMillis() - startTime) / 1000).toInt()
            } else {
                accumulatedSeconds
            }

            // Save anything 5 seconds or longer
            if (finalElapsed >= 5) {
                val minutes = finalElapsed / 60
                pendingDurationMins = minutes
                pendingIsFlowMode = (focusMode == "Flow")
                pendingTargetMins = if (focusMode == "Timer") targetMinutes else 0
                sessionNotesText = ""
                showNotesDialog = true
            } else {
                isSpaceActive = false
            }
        }
    }

    BackHandler(enabled = isSpaceActive) {
        endSessionAndPromptNotes()
    }

    val selectedSpace = "Focus Session"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
    ) {
        if (!isSpaceActive) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .verticalScroll(scrollState)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AtaraxiaTheme.spacing.Space16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Spa,
                            contentDescription = "Ataraxia Logo",
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
                    ProfileAvatar(
                        name = name,
                        imageUri = profileImage,
                        size = 40.dp,
                        onClick = onNavigateToProfile
                    )
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                // Stats Banner
                val todayMins = remember(sessions) { calculateTodayMinutes(sessions) }
                val weekMins  = remember(sessions) { calculateWeekMinutes(sessions) }
                val monthMins = remember(sessions) { calculateMonthMinutes(sessions) }

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Streak row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (currentStreak > 0) "🔥" else "✨",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (currentStreak > 0) "$currentStreak Day Streak" else "Start your first session",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Stats grid
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

                // Mode selection
                SectionHeader(
                    title = "Focus Mode",
                    subtitle = "Choose your silent flow style."
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Timer", "Flow").forEach { mode ->
                        val isSelected = focusMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { focusMode = mode }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mode == "Timer") "⏳ Timer Mode" else "🌊 Flow Mode",
                                color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                // Duration Configuration (Only in Timer Mode)
                if (focusMode == "Timer") {
                    SectionHeader(
                        title = "Silent Target Time",
                        subtitle = "Protect attention for a custom duration."
                    )

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target Duration",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = "$targetMinutes minutes",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = targetMinutes.toFloat(),
                                onValueChange = { targetMinutes = (it / 5).toInt() * 5 },
                                valueRange = 5f..180f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = DesignTokens.TextSecondary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
                }

                // Focus Setup Options
                SectionHeader(
                    title = "Quiet Time Settings",
                    subtitle = "Protect your focus and space environment."
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Keep Screen Awake
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Keep Screen Awake",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = "Prevent screen from dimming during focus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignTokens.TextSecondary
                                )
                            }
                            Switch(
                                checked = keepScreenAwake,
                                onCheckedChange = { keepScreenAwake = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Allow Calls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Allow Calls",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = "Let calls ring through in priority mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignTokens.TextSecondary
                                )
                            }
                            Switch(
                                checked = allowCalls,
                                onCheckedChange = { allowCalls = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

                AtaraxiaPrimaryButton(
                    text = "Enter Space",
                    onClick = { isSpaceActive = true }
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

                // Recent Sessions header with calendar button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recent Sessions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )
                        Text(
                            text = "Your silent focus record.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignTokens.TextSecondary
                        )
                    }
                    IconButton(onClick = { showCalendarDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "View Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                if (sessions.isEmpty()) {
                    EmptyState(
                        illustration = {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        },
                        title = "Silent Space Awaits",
                        subtitle = "Begin a focus session to practice silent mindfulness and trace your flow."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sessions.forEach { session ->
                            val dateString = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(session.timestamp))
                            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val isStoppedMidway = !session.isFlowMode && session.targetMinutes > 0 && session.durationMinutes < session.targetMinutes
                                        val durationText = if (session.durationMinutes == 0) "< 1 Min" else "${session.durationMinutes} Min"
                                        Text(
                                            text = "$durationText Focus" + 
                                                   (if (session.isFlowMode) " (Flow)" else "") +
                                                   (if (isStoppedMidway) " (Target: ${session.targetMinutes} Min - Stopped midway)" else ""),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = DesignTokens.TextPrimary
                                        )
                                        Text(
                                            text = dateString,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DesignTokens.TextSecondary
                                        )
                                        if (session.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "\"${session.notes}\"",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteFocusSession(session.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete log",
                                            tint = DesignTokens.TextSecondary.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(130.dp))
            }
        } else {
            // Active focus space view
            val transition = rememberInfiniteTransition(label = "sunset")
            val pulseFactor by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB300).copy(alpha = pulseFactor * 0.12f),
                                Color(0xFFFF7043).copy(alpha = pulseFactor * 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(x = 540f, y = 320f),
                            radius = 900f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .padding(vertical = AtaraxiaTheme.spacing.Space16),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { endSessionAndPromptNotes() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Exit Space",
                            tint = DesignTokens.TextPrimary
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    StopwatchAnalogClock(
                        elapsedSeconds = elapsedSeconds,
                        modifier = Modifier.size(140.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space20))

                    Text(
                        text = selectedSpace,
                        style = MaterialTheme.typography.headlineLarge,
                        color = DesignTokens.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                    val displayMins = elapsedSeconds / 60
                    val displaySecs = elapsedSeconds % 60
                    Text(
                        text = if (isPaused) "Session Paused" else "Focus Active: ${String.format("%02d:%02d", displayMins, displaySecs)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isPaused) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AtaraxiaTheme.spacing.Space16),
                    horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaSecondaryButton(
                            text = "Exit Space",
                            onClick = { endSessionAndPromptNotes() }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaPrimaryButton(
                            text = if (isPaused) "Resume" else "Pause",
                            onClick = { isPaused = !isPaused }
                        )
                    }
                }
            }
        }

        // Post-Session Notes input popup Dialog
        if (showNotesDialog) {
            Dialog(onDismissRequest = { /* Force action on buttons */ }) {
                LunafloraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "What would you like to remember from this session?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                        
                        PrimaryTextField(
                            value = sessionNotesText,
                            onValueChange = { sessionNotesText = it },
                            placeholder = "Thoughts, insights, or a word of calm...",
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaSecondaryButton(
                                    text = "Skip Notes",
                                    onClick = {
                                        viewModel.logSession(pendingDurationMins, selectedSpace, "", pendingIsFlowMode, pendingTargetMins)
                                        showNotesDialog = false
                                        isSpaceActive = false
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaPrimaryButton(
                                    text = "Save",
                                    onClick = {
                                        viewModel.logSession(pendingDurationMins, selectedSpace, sessionNotesText, pendingIsFlowMode, pendingTargetMins)
                                        showNotesDialog = false
                                        isSpaceActive = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Calendar Dialog
        if (showCalendarDialog) {
            Dialog(onDismissRequest = { showCalendarDialog = false }) {
                LunafloraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Month navigation header
                        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                            Calendar.getInstance().apply {
                                set(Calendar.YEAR, calendarYear)
                                set(Calendar.MONTH, calendarMonth)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }.time
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (calendarMonth == 0) { calendarMonth = 11; calendarYear-- }
                                else calendarMonth--
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowBack,
                                    contentDescription = "Previous month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            IconButton(onClick = {
                                if (calendarMonth == 11) { calendarMonth = 0; calendarYear++ }
                                else calendarMonth++
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = "Next month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Day-of-week labels
                        val dayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            dayLabels.forEach { label ->
                                Text(
                                    text = label,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextSecondary
                                )
                            }
                        }

                        // Build calendar grid
                        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                        val sessionDays = sessions.map { sdf.format(Date(it.timestamp)).toInt() }.toSet()
                        val calCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, calendarYear)
                            set(Calendar.MONTH, calendarMonth)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val firstDow = calCal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
                        val daysInMonth = calCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val todayKey = sdf.format(Date()).toInt()

                        val totalCells = firstDow + daysInMonth
                        val rows = (totalCells + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val day = cellIndex - firstDow + 1
                                        if (day < 1 || day > daysInMonth) {
                                            Box(modifier = Modifier.weight(1f))
                                        } else {
                                            val dayKey = String.format(
                                                "%04d%02d%02d", calendarYear, calendarMonth + 1, day
                                            ).toInt()
                                            val hasSession = dayKey in sessionDays
                                            val isToday = dayKey == todayKey
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                            else -> Color.Transparent
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = day.toString(),
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = if (isToday) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                                    )
                                                    if (hasSession) {
                                                        Text(
                                                            text = "🌸",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopwatchAnalogClock(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier
) {
    val secondsHandAngle = (elapsedSeconds % 60) * 6f
    val minutes = elapsedSeconds / 60f
    val minutesHandAngle = (minutes % 30) * 12f

    val primaryColor = MaterialTheme.colorScheme.primary
    val textPrimary = DesignTokens.TextPrimary

    Canvas(
        modifier = modifier
    ) {
        val center = center
        val radius = size.minDimension / 2f

        drawCircle(
            color = textPrimary.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        for (i in 0 until 60) {
            val angle = i * 6f
            val isMajor = i % 5 == 0
            val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
            val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            val tickColor = if (isMajor) textPrimary.copy(alpha = 0.5f) else textPrimary.copy(alpha = 0.25f)

            val angleRad = Math.toRadians(angle.toDouble())
            val startX = (center.x + (radius - tickLength) * Math.sin(angleRad)).toFloat()
            val startY = (center.y - (radius - tickLength) * Math.cos(angleRad)).toFloat()
            val endX = (center.x + radius * Math.sin(angleRad)).toFloat()
            val endY = (center.y - radius * Math.cos(angleRad)).toFloat()

            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = tickWidth
            )
        }

        val subDialCenter = Offset(center.x, center.y + radius * 0.3f)
        val subDialRadius = radius * 0.28f
        drawCircle(
            color = textPrimary.copy(alpha = 0.1f),
            radius = subDialRadius,
            center = subDialCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )
        for (i in 0 until 6) {
            val angle = i * 60f
            val angleRad = Math.toRadians(angle.toDouble())
            val tickLength = 3.dp.toPx()
            val startX = (subDialCenter.x + (subDialRadius - tickLength) * Math.sin(angleRad)).toFloat()
            val startY = (subDialCenter.y - (subDialRadius - tickLength) * Math.cos(angleRad)).toFloat()
            val endX = (subDialCenter.x + subDialRadius * Math.sin(angleRad)).toFloat()
            val endY = (subDialCenter.y - subDialRadius * Math.cos(angleRad)).toFloat()

            drawLine(
                color = textPrimary.copy(alpha = 0.3f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        val minAngleRad = Math.toRadians(minutesHandAngle.toDouble())
        val minHandLen = subDialRadius * 0.75f
        val minEndX = (subDialCenter.x + minHandLen * Math.sin(minAngleRad)).toFloat()
        val minEndY = (subDialCenter.y - minHandLen * Math.cos(minAngleRad)).toFloat()
        drawLine(
            color = primaryColor.copy(alpha = 0.8f),
            start = subDialCenter,
            end = Offset(minEndX, minEndY),
            strokeWidth = 2.dp.toPx()
        )
        drawCircle(
            color = primaryColor,
            radius = 2.5.dp.toPx(),
            center = subDialCenter
        )

        val secAngleRad = Math.toRadians(secondsHandAngle.toDouble())
        val secHandLen = radius * 0.82f
        val secEndX = (center.x + secHandLen * Math.sin(secAngleRad)).toFloat()
        val secEndY = (center.y - secHandLen * Math.cos(secAngleRad)).toFloat()
        drawLine(
            color = primaryColor, start = center, end = Offset(secEndX, secEndY), strokeWidth = 2.dp.toPx()
        )

        drawCircle(
            color = primaryColor,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}

private fun calculateFocusStreak(sessions: List<com.example.ataraxia.data.local.entity.FocusSessionEntity>): Int {
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

// ── Stats helpers ──────────────────────────────────────────────────────────

private fun calculateTodayMinutes(sessions: List<com.example.ataraxia.data.local.entity.FocusSessionEntity>): Int {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = sdf.format(Date())
    return sessions.filter { sdf.format(Date(it.timestamp)) == today }
        .sumOf { it.durationMinutes }
}

private fun calculateWeekMinutes(sessions: List<com.example.ataraxia.data.local.entity.FocusSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val weekStart = cal.timeInMillis
    return sessions.filter { it.timestamp >= weekStart }.sumOf { it.durationMinutes }
}

private fun calculateMonthMinutes(sessions: List<com.example.ataraxia.data.local.entity.FocusSessionEntity>): Int {
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

// ── Stat UI components ─────────────────────────────────────────────────────

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
        modifier = androidx.compose.ui.Modifier
            .height(32.dp)
            .width(1.dp)
            .background(DesignTokens.TextSecondary.copy(alpha = 0.2f))
    )
}
