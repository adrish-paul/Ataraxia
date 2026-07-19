package com.example.ataraxia.features.breathe.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.ScreenEnclosure
import com.example.ataraxia.ui.components.AtaraxiaAudioSelectorDialog
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun BreathePrepScreen(
    sessions: List<BreatheSessionEntity>,
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    selectedDurationMinutes: Int,
    onDurationChanged: (Int) -> Unit,
    selectedSound: String,
    onSoundSelected: (String) -> Unit,
    soundVolume: Float,
    onVolumeChanged: (Float) -> Unit,
    hapticGuidanceEnabled: Boolean,
    onHapticGuidanceToggle: (Boolean) -> Unit,
    onBeginSession: () -> Unit,
    showCalendarTrigger: () -> Unit,
    selectedDayNum: Int?,
    onClearDayFilter: () -> Unit,
    displayedSessions: List<BreatheSessionEntity>,
    onDeleteSession: (Long) -> Unit,
    methods: List<MethodItem>,
    onShowMethodPopup: () -> Unit,
    todayMins: Int,
    weekMins: Int,
    monthMins: Int,
    currentStreak: Int,
    scrollState: ScrollState,
    onShowInsights: () -> Unit
) {
    val currentMethod = remember(selectedMethod) {
        methods.firstOrNull { it.name == selectedMethod } ?: methods.first()
    }


    ScreenEnclosure {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocalFlorist,
                    contentDescription = "Breathe",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = "Breathe",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary
                )
            }
            IconButton(onClick = onShowInsights) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = "Breathing Insights",
                    tint = DesignTokens.TextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {


            // Stats Banner Card
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
                            text = if (currentStreak > 0) "$currentStreak Day Streak" else "Start your first session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BreatheStatCell(label = "Today", value = formatBreatheTime(todayMins))
                        BreatheStatDivider()
                        BreatheStatCell(label = "This Week", value = formatBreatheTime(weekMins))
                        BreatheStatDivider()
                        BreatheStatCell(label = "This Month", value = formatBreatheTime(monthMins))
                    }
                }
            }

            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

            // Session Setup Card
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space20)) {
                    Text(
                        text = "Prepare Your Session",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    // 1. Method Selection
                    Column {
                        Text(
                            text = "Breathing Method",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DesignTokens.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f))
                                .clickable { onShowMethodPopup() }
                                .padding(AtaraxiaTheme.spacing.Space12)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentMethod.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = currentMethod.desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DesignTokens.TextSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentMethod.pattern,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                                    Icon(
                                        imageVector = Icons.Outlined.UnfoldMore,
                                        contentDescription = "Select Method",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Duration Slider
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
                                text = "$selectedDurationMinutes min",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                        Slider(
                            value = selectedDurationMinutes.toFloat(),
                            onValueChange = { onDurationChanged(it.toInt().coerceIn(1, 30)) },
                            valueRange = 1f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = DesignTokens.TextSecondary.copy(alpha = 0.2f)
                            )
                        )
                    }

                    AtaraxiaPrimaryButton(
                        text = "Begin Session",
                        onClick = onBeginSession
                    )
                }
            }

            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

            // Sounds & Haptics Configuration Card
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space20)) {
                    Text(
                        text = "Sounds & Haptics Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    // 1. Ambient Sounds Selector
                    Column {
                        Text(
                            text = "Ambient Soundscape",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DesignTokens.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f))
                                    .padding(horizontal = AtaraxiaTheme.spacing.Space12)
                            ) {
                                var showSoundDialog by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showSoundDialog = true }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedSound,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = DesignTokens.TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.UnfoldMore,
                                        contentDescription = "Expand Sound",
                                        tint = DesignTokens.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                AtaraxiaAudioSelectorDialog(
                                    showDialog = showSoundDialog,
                                    onDismiss = { showSoundDialog = false },
                                    selectedSound = selectedSound,
                                    onSoundSelected = onSoundSelected,
                                    soundVolume = soundVolume,
                                    onVolumeChanged = onVolumeChanged
                                )
                            }
                        }

                        if (selectedSound != "None") {
                            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = DesignTokens.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = soundVolume,
                                    onValueChange = onVolumeChanged,
                                    valueRange = 0f..1f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = DesignTokens.TextSecondary.copy(alpha = 0.2f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${(soundVolume * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextSecondary
                                )
                            }
                        }
                    }

                    // 2. Haptic Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Vibration,
                                contentDescription = "Haptics",
                                tint = DesignTokens.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Breathing Guidance Haptics",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = "Vibrations to pace your breathing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DesignTokens.TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = hapticGuidanceEnabled,
                            onCheckedChange = onHapticGuidanceToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

            // History Timeline Card
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val monthName = DateFormatSymbols().months[Calendar.getInstance().get(Calendar.MONTH)]
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedDayNum != null) "Logs: $monthName $selectedDayNum" else "Recent Sessions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            Text(
                                text = if (selectedDayNum != null) "Sessions completed on this day." else "Your breathing history.",
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
                                    .clickable { onClearDayFilter() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        } else {
                            IconButton(onClick = showCalendarTrigger) {
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
                            title = if (selectedDayNum != null) "No sessions today" else "Start with a single breath",
                            subtitle = if (selectedDayNum != null) "No breathe sessions logged on this day." else "Log your first breathing session to anchor yourself in the present."
                        )
                    } else {
                        BreatheHistoryTimeline(
                            sessions = displayedSessions,
                            onDelete = onDeleteSession
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
private fun BreatheStatCell(label: String, value: String) {
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
private fun BreatheStatDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(DesignTokens.TextSecondary.copy(alpha = 0.2f))
    )
}

private fun formatBreatheTime(totalMinutes: Int): String {
    return when {
        totalMinutes == 0 -> "0m"
        totalMinutes < 60 -> "${totalMinutes}m"
        else -> "${totalMinutes / 60}h ${totalMinutes % 60}m".trimEnd().replace("  ", " ")
    }
}

@Composable
private fun BreatheStatsDashboard(sessions: List<BreatheSessionEntity>) {
    val totalSessions = sessions.size
    val totalSeconds = sessions.sumOf { it.durationSeconds }
    val longestSeconds = sessions.maxOfOrNull { it.durationSeconds } ?: 0
    val favoriteMethod = if (sessions.isEmpty()) "None" else sessions.groupBy { it.method }
        .maxByOrNull { it.value.size }?.key ?: "None"

    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
            Text(
                text = "Breathing Insights",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DesignTokens.TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Sessions", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                    Text("$totalSessions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Time", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                    Text(formatBreatheDurationString(totalSeconds), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Favorite Method", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                    Text(favoriteMethod, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Longest Practice", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                    Text(formatBreatheDurationString(longestSeconds), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Weekly Activity (Last 7 Days)", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
            WeeklyActivityChart(sessions = sessions)
        }
    }
}

@Composable
private fun WeeklyActivityChart(sessions: List<BreatheSessionEntity>) {
    val configuration = LocalConfiguration.current
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

private fun formatBreatheDurationString(totalSeconds: Int): String = when {
    totalSeconds < 60 -> "< 1m"
    else -> { val m = totalSeconds / 60; "$m min" }
}
