package com.example.ataraxia.features.me.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.data.local.entity.MoodLogEntity
import com.example.ataraxia.ui.components.AtaraxiaDialog
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ScreenEnclosure
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.SettingRow
import com.example.ataraxia.util.StorageHelper
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.ui.theme.DesignTokens
import java.util.Calendar

@Composable
fun MeScreen(
    name: String,
    profileImage: String,
    onProfileImageChange: (String) -> Unit,
    currentThemeMode: AtaraxiaThemeMode,
    reflectionCount: Int,
    breatheSeconds: Int,
    focusMinutes: Int,
    activeDaysCount: Int,
    habitStreakCount: Int,
    todayMood: String,
    greetingTitle: String,
    greetingSubtitle: String,
    appLockEnabled: Boolean,
    onAppLockToggle: (enabled: Boolean, pin: String) -> Unit,
    onNameChange: (String) -> Unit,
    onThemeChange: (AtaraxiaThemeMode) -> Unit,
    onClearData: () -> Unit,
    
    // Switch states
    amoledModeEnabled: Boolean,
    onAmoledModeToggle: (Boolean) -> Unit,
    dynamicColorsEnabled: Boolean,
    onDynamicColorsToggle: (Boolean) -> Unit,
    reducedMotionEnabled: Boolean,
    onReducedMotionToggle: (Boolean) -> Unit,
    biometricsEnabled: Boolean,
    onBiometricsToggle: (Boolean) -> Unit,
    mindfulUsageEnabled: Boolean,
    onMindfulUsageToggle: (Boolean) -> Unit,
    
    reminderDailyEnabled: Boolean,
    onReminderDailyToggle: (Boolean) -> Unit,
    reminderJournalEnabled: Boolean,
    onReminderJournalToggle: (Boolean) -> Unit,
    reminderBreatheEnabled: Boolean,
    onReminderBreatheToggle: (Boolean) -> Unit,
    reminderFocusEnabled: Boolean,
    onReminderFocusToggle: (Boolean) -> Unit,
    reminderMindfulnessEnabled: Boolean,
    onReminderMindfulnessToggle: (Boolean) -> Unit,
    
    // Lists for Insights calculations
    allReflections: List<JournalEntryEntity>,
    allBreatheSessions: List<BreatheSessionEntity>,
    allFocusSessions: List<FocusSessionEntity>,
    allMoodLogs: List<MoodLogEntity>,
    
    scrollToTopKey: Int = 0
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0) scrollState.animateScrollTo(0)
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(name) }
    
    // About dialogs toggles
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val localPath = StorageHelper.saveImageToInternalStorage(context, it)
            if (localPath.isNotEmpty()) {
                val localUri = android.net.Uri.fromFile(java.io.File(localPath))
                onProfileImageChange(localUri.toString())
            }
        }
    }

    val currentThemeItem = remember(currentThemeMode) {
        when (currentThemeMode) {
            AtaraxiaThemeMode.LIGHT -> ThemeItem("💮 Serene", "💮 Original calm light mode", "Serene")
            AtaraxiaThemeMode.AURORA -> ThemeItem("✨ Aurora", "✨ Lavender accented dark mode", "Dark")
            AtaraxiaThemeMode.COSMOS -> ThemeItem("🌙 Cosmos", "🌙 Deep black AMOLED dark mode", "Amoled")
            AtaraxiaThemeMode.FOREST -> ThemeItem("🌿 Forest", "🌿 Green accented dark mode", "Green")
            AtaraxiaThemeMode.SAKURA -> ThemeItem("🌸 Sakura", "🌸 Pink accented light mode", "Pink")
            AtaraxiaThemeMode.AQUA -> ThemeItem("🌊 Aqua", "🌊 Blue ocean light mode", "Blue")
        }
    }

    // Calculations for Universal Insights
    val favoriteIntention = remember(allFocusSessions) {
        allFocusSessions
            .filter { it.durationMinutes > 0 && it.intentionName.isNotBlank() }
            .groupBy { it.intentionName }
            .maxByOrNull { it.value.size }
            ?.key ?: "None"
    }

    val mostActiveDay = remember(allReflections, allBreatheSessions, allFocusSessions, allMoodLogs) {
        val cal = Calendar.getInstance()
        val weekdayCounts = IntArray(7)
        allReflections.forEach {
            cal.timeInMillis = it.timestamp
            weekdayCounts[cal.get(Calendar.DAY_OF_WEEK) - 1]++
        }
        allBreatheSessions.forEach {
            cal.timeInMillis = it.timestamp
            weekdayCounts[cal.get(Calendar.DAY_OF_WEEK) - 1]++
        }
        allFocusSessions.forEach {
            cal.timeInMillis = it.timestamp
            weekdayCounts[cal.get(Calendar.DAY_OF_WEEK) - 1]++
        }
        allMoodLogs.forEach {
            cal.timeInMillis = it.timestamp
            weekdayCounts[cal.get(Calendar.DAY_OF_WEEK) - 1]++
        }
        val weekdays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        var maxIndex = -1
        var maxCount = 0
        weekdayCounts.forEachIndexed { index, count ->
            if (count > maxCount) {
                maxCount = count
                maxIndex = index
            }
        }
        if (maxIndex != -1) weekdays[maxIndex] else "None"
    }

    val peakFocusHour = remember(allFocusSessions) {
        val cal = Calendar.getInstance()
        val hourCounts = IntArray(24)
        allFocusSessions.forEach {
            cal.timeInMillis = it.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if (hour in 0..23) {
                hourCounts[hour]++
            }
        }
        var maxHour = -1
        var maxCount = 0
        hourCounts.forEachIndexed { hour, count ->
            if (count > maxCount) {
                maxCount = count
                maxHour = hour
            }
        }
        if (maxHour != -1) {
            val startHour = if (maxHour == 0) 12 else if (maxHour > 12) maxHour - 12 else maxHour
            val startAmPm = if (maxHour >= 12) "PM" else "AM"
            val nextHour = (maxHour + 1) % 24
            val endHour = if (nextHour == 0) 12 else if (nextHour > 12) nextHour - 12 else nextHour
            val endAmPm = if (nextHour >= 12) "PM" else "AM"
            "$startHour:00 $startAmPm - $endHour:00 $endAmPm"
        } else "None"
    }

    val moodTrend = remember(allMoodLogs) {
        if (allMoodLogs.isEmpty()) {
            "None"
        } else {
            allMoodLogs
                .groupBy { it.mood }
                .maxByOrNull { it.value.size }
                ?.key ?: "None"
        }
    }

    ScreenEnclosure {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AtaraxiaTheme.spacing.Space16, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Spa,
                contentDescription = "Ataraxia Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
            Text(
                text = "Sanctuary",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = DesignTokens.TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1: Personal Header
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(96.dp)
                    ) {
                        ProfileAvatar(
                            name = name,
                            imageUri = profileImage,
                            size = 96.dp,
                            onClick = { imageLauncher.launch("image/*") }
                        )
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { imageLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✏️", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (!isEditingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    newNameInput = name
                                    isEditingName = true
                                }
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                            Text("✏️", style = MaterialTheme.typography.titleSmall)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PrimaryTextField(
                                    value = newNameInput,
                                    onValueChange = { newNameInput = it },
                                    placeholder = "Your name",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                            Text(
                                text = "✔️",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.clickable {
                                    if (newNameInput.isNotBlank()) {
                                        onNameChange(newNameInput)
                                        isEditingName = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "❌",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.clickable { isEditingName = false }
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = greetingTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = greetingSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesignTokens.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🌿 $habitStreakCount Day${if (habitStreakCount == 1) "" else "s"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            Text(
                                text = "Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignTokens.TextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = todayMood.ifEmpty { "Not Logged" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            Text(
                                text = "Today's Mood",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignTokens.TextSecondary
                            )
                        }
                    }
                }
            }

            // SECTION 2: Universal Insights
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Universal Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InsightItem(label = "Favorite Intention", value = favoriteIntention, modifier = Modifier.weight(1f))
                        InsightItem(label = "Most Active Day", value = mostActiveDay, modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InsightItem(label = "Peak Focus Hour", value = peakFocusHour, modifier = Modifier.weight(1f))
                        InsightItem(label = "Mood Profile", value = moodTrend, modifier = Modifier.weight(1f))
                    }
                }
            }

            // SECTION 3: Personal Journey (Progress Overview)
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Personal Journey",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        JourneyStatsItem(title = "Reflections", value = "$reflectionCount", unit = "entries", modifier = Modifier.weight(1f))
                        JourneyStatsItem(title = "Quiet Focus", value = "$focusMinutes", unit = "mins", modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val displayBreatheMin = if (breatheSeconds < 60 && breatheSeconds > 0) 1 else breatheSeconds / 60
                        JourneyStatsItem(title = "Breathing", value = "$displayBreatheMin", unit = "mins", modifier = Modifier.weight(1f))
                        JourneyStatsItem(title = "Active Days", value = "$activeDaysCount", unit = "days", modifier = Modifier.weight(1f))
                    }
                }
            }

            // SECTION 4: Appearance
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingRow(
                            title = "Theme",
                            subtitle = currentThemeItem.name,
                            icon = { Icon(Icons.Outlined.ColorLens, null, tint = DesignTokens.TextPrimary) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = DesignTokens.TextSecondary) },
                            onClick = { showThemeDialog = true }
                        )
                    }
                }
            }

            // SECTION 5: Privacy & Security
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Privacy & Security",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    // Local-Only Storage Explanation Panel (High quality offline text)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(AtaraxiaTheme.spacing.Space12)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🛡️", fontSize = 16.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "100% Offline Sanctuary",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Your entries, breathing intervals, focus timers, and passcode keys are stored encrypted locally on this device. Ataraxia operates without cloud sync or third-party trackers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignTokens.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingRow(
                            title = "Sanctuary Passcode Lock",
                            subtitle = "Require a 4-digit PIN to access the app",
                            icon = { Icon(Icons.Outlined.Lock, null, tint = DesignTokens.TextPrimary) },
                            trailingContent = {
                                Switch(
                                    checked = appLockEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) showPinSetupDialog = true else onAppLockToggle(false, "")
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Dedicated Mindful Usage Section (Future Expandable Card)
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Mindful Usage",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingRow(
                            title = "Mindful Usage Tracking",
                            subtitle = "Generate local stats for breathing and focus logs",
                            trailingContent = {
                                Switch(
                                    checked = mindfulUsageEnabled,
                                    onCheckedChange = onMindfulUsageToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // SECTION 6: Notifications Reminders
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Notifications & Reminders",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingRow(
                            title = "Daily Reminders",
                            subtitle = "Gentle cues to check-in and breathe",
                            trailingContent = {
                                Switch(
                                    checked = reminderDailyEnabled,
                                    onCheckedChange = onReminderDailyToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )

                        SettingRow(
                            title = "Journal Reflections Reminders",
                            subtitle = "Prompts to log thoughts and process emotions",
                            trailingContent = {
                                Switch(
                                    checked = reminderJournalEnabled,
                                    onCheckedChange = onReminderJournalToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )

                        SettingRow(
                            title = "Breathing Prompts",
                            subtitle = "Reminders to take a deep chest breathing pause",
                            trailingContent = {
                                Switch(
                                    checked = reminderBreatheEnabled,
                                    onCheckedChange = onReminderBreatheToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )

                        SettingRow(
                            title = "Quiet Focus Reminders",
                            subtitle = "Prompts to organize workspace study timers",
                            trailingContent = {
                                Switch(
                                    checked = reminderFocusEnabled,
                                    onCheckedChange = onReminderFocusToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )

                        SettingRow(
                            title = "Mindfulness Cues",
                            subtitle = "Subtle alerts highlighting wellness summaries",
                            trailingContent = {
                                Switch(
                                    checked = reminderMindfulnessEnabled,
                                    onCheckedChange = onReminderMindfulnessToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // SECTION 7: About & Danger Zone
            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "About Ataraxia",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingRow(
                            title = "Application Info",
                            subtitle = "Ataraxia Harmony v0.7.0",
                            icon = { Icon(Icons.Outlined.Info, null, tint = DesignTokens.TextPrimary) }
                        )

                        SettingRow(
                            title = "What's New (Changelog)",
                            subtitle = "Explore the v0.7.0 updates log",
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = DesignTokens.TextSecondary) },
                            onClick = { showChangelogDialog = true }
                        )

                        SettingRow(
                            title = "Credits & Contributors",
                            subtitle = "Check out the team behind Ataraxia",
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = DesignTokens.TextSecondary) },
                            onClick = { showCreditsDialog = true }
                        )

                        SettingRow(
                            title = "Privacy Policy",
                            subtitle = "Review local data ownership policies",
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = DesignTokens.TextSecondary) },
                            onClick = { showPrivacyPolicyDialog = true }
                        )
                    }
                }
            }

            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Danger Zone",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF5350)
                    )
                    
                    SettingRow(
                        title = "Reset Ataraxia",
                        subtitle = "Permanently wipe all journals, breathing history, intentions, and settings",
                        icon = { Icon(Icons.Outlined.DeleteForever, null, tint = Color(0xFFEF5350)) },
                        onClick = { showClearDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    if (showClearDialog) {
        AtaraxiaDialog(
            title = "Reset Ataraxia?",
            description = "This action is permanent. All your reflections, breathing metrics, custom focus intentions, and themes settings will be permanently erased.",
            confirmLabel = "Reset",
            onConfirm = {
                showClearDialog = false
                onClearData()
            },
            dismissLabel = "Keep",
            onDismiss = { showClearDialog = false },
            isDestructive = true
        )
    }

    if (showChangelogDialog) {
        AtaraxiaDialog(
            title = "🌿 Changelog v0.7.0",
            description = "• Focus Sanctuary Overhaul: Organize your quiet workspaces with customizable intention tags.\n• Drag-and-Drop Intentions: Long-press to sort and customize your intention tags grid dynamically.\n• Personal Sanctuary Redesign: Access all-new Universal Insights, local streak counters, theme configurations, notifications toggles, and privacy panels in a unified space.",
            confirmLabel = "Close",
            onConfirm = { showChangelogDialog = false },
            dismissLabel = "",
            onDismiss = { showChangelogDialog = false }
        )
    }

    if (showCreditsDialog) {
        AtaraxiaDialog(
            title = "👥 Credits & Contributors",
            description = "Ataraxia was designed and crafted as a digital sanctuary of peace and reflection.\n\nCreated with ❤️",
            confirmLabel = "Close",
            onConfirm = { showCreditsDialog = false },
            dismissLabel = "",
            onDismiss = { showCreditsDialog = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        AtaraxiaDialog(
            title = "🔒 Offline Privacy Charter",
            description = "Ataraxia is committed to 100% data ownership. All your journals, reflections, breathing timestamps, focus sessions, and security PIN keys are stored strictly locally on your device in secure Room databases and private preferences.\n\nNo trackers, no servers, no telemetry. Your peace of mind is fully yours.",
            confirmLabel = "Close",
            onConfirm = { showPrivacyPolicyDialog = false },
            dismissLabel = "",
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    MeThemePicker(
        showThemeDialog = showThemeDialog,
        currentThemeMode = currentThemeMode,
        onDismiss = { showThemeDialog = false },
        onThemeChange = { mode ->
            onThemeChange(mode)
            showThemeDialog = false
        }
    )

    MePasscodeSettings(
        showPinSetupDialog = showPinSetupDialog,
        onDismiss = { showPinSetupDialog = false },
        onAppLockToggle = onAppLockToggle
    )
}

@Composable
private fun InsightItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = DesignTokens.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun JourneyStatsItem(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = DesignTokens.TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

