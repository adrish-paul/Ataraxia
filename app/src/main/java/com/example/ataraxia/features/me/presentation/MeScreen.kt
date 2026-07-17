package com.example.ataraxia.features.me.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.components.AtaraxiaDialog
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.SettingRow
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun MeScreen(
    name: String,
    profileImage: String,
    onProfileImageChange: (String) -> Unit,
    currentThemeMode: AtaraxiaThemeMode,
    reflectionCount: Int,
    breatheSeconds: Int,
    focusMinutes: Int,
    appLockEnabled: Boolean,
    onAppLockToggle: (enabled: Boolean, pin: String) -> Unit,
    onNameChange: (String) -> Unit,
    onThemeChange: (AtaraxiaThemeMode) -> Unit,
    onClearData: () -> Unit,
    scrollToTopKey: Int = 0
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0) scrollState.animateScrollTo(0)
    }

    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }

    var isEditingName by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(name) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onProfileImageChange(it.toString())
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = AtaraxiaTheme.spacing.Space24)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = AtaraxiaTheme.spacing.Space16),
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

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
            
            if (!isEditingName) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().clickable { 
                        newNameInput = name
                        isEditingName = true 
                    }
                ) {
                    Text(text = name, style = MaterialTheme.typography.headlineLarge, color = DesignTokens.TextPrimary)
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                    Text("✏️", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.85f)
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
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.clickable {
                            if (newNameInput.isNotBlank()) {
                                onNameChange(newNameInput)
                                isEditingName = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                    Text(
                        text = "❌",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.clickable { isEditingName = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space4))

            Text(
                text = "Sanctuary member since today",
                style = MaterialTheme.typography.labelLarge,
                color = DesignTokens.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                Text(text = "Your Quiet Stats", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$reflectionCount", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                        Text(text = "Reflections", style = MaterialTheme.typography.labelMedium, color = DesignTokens.TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val displayBreatheMin = if (breatheSeconds < 60 && breatheSeconds > 0) 1 else breatheSeconds / 60
                        Text(text = "${displayBreatheMin}m", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                        Text(text = "Breathed", style = MaterialTheme.typography.labelMedium, color = DesignTokens.TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${focusMinutes}m", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                        Text(text = "Focused", style = MaterialTheme.typography.labelMedium, color = DesignTokens.TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        LunafloraCard(
            onClick = { showThemeDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                Text(text = "Personalization", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = currentThemeItem.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                        Text(text = "Tap to change sanctuary theme", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                    Icon(imageVector = Icons.Outlined.ColorLens, contentDescription = "Select Theme", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                Text(text = "Preferences", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                Column {
                    SettingRow(
                        title = "Quiet Reminders",
                        subtitle = "Gentle prompts to keep reflecting",
                        icon = { Icon(Icons.Outlined.Notifications, null, tint = DesignTokens.TextPrimary) },
                        trailingContent = {
                            Switch(
                                checked = isNotificationsEnabled,
                                onCheckedChange = { isNotificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                    SettingRow(
                        title = "Sanctuary Passcode Lock",
                        subtitle = "Require a 4-digit PIN to access Ataraxia",
                        icon = { Icon(Icons.Outlined.Lock, null, tint = DesignTokens.TextPrimary) },
                        trailingContent = {
                            Switch(
                                checked = appLockEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinSetupDialog = true
                                    } else {
                                        onAppLockToggle(false, "")
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                Text(text = "Danger Zone", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                Column {
                    SettingRow(
                        title = "Reset Quiet Space",
                        subtitle = "Permanently delete reflections and settings history",
                        icon = { Icon(Icons.Outlined.DeleteForever, null, tint = DesignTokens.TextPrimary) },
                        onClick = { showClearDialog = true }
                    )
                    SettingRow(
                        title = "Ataraxia Info",
                        subtitle = "Version 0.3.0",
                        icon = { Icon(Icons.Outlined.Info, null, tint = DesignTokens.TextPrimary) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }

    if (showClearDialog) {
        AtaraxiaDialog(
            title = "Reset Quiet Space?",
            description = "This action is permanent. All your reflections, breathing metrics, and settings will be permanently erased.",
            confirmLabel = "Reset Space",
            onConfirm = {
                showClearDialog = false
                onClearData()
            },
            dismissLabel = "Keep History",
            onDismiss = { showClearDialog = false },
            isDestructive = true
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
