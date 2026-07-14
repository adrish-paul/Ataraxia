package com.example.ataraxia.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Palette
import com.example.ataraxia.ui.components.AtaraxiaDialog
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.SectionHeader
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
    onClearData: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var tempPinInput by remember { mutableStateOf("") }
    var pinSetupError by remember { mutableStateOf("") }

    var isEditingName by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(name) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onProfileImageChange(it.toString())
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
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AtaraxiaTheme.spacing.Space16),
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

        // 2. Profile Details Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image avatar with click selection capability and pencil overlay
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
                        Text(
                            text = "✏️",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
            
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
                        style = MaterialTheme.typography.headlineLarge,
                        color = DesignTokens.TextPrimary
                    )
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
                        modifier = Modifier.clickable {
                            isEditingName = false
                        }
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

        // 3. User Statistics Display
        SectionHeader(title = "Your Quiet Stats")
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$reflectionCount",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Text(
                        text = "Reflections",
                        style = MaterialTheme.typography.labelMedium,
                        color = DesignTokens.TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayBreatheMin = if (breatheSeconds < 60 && breatheSeconds > 0) 1 else breatheSeconds / 60
                    Text(
                        text = "${displayBreatheMin}m",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Text(
                        text = "Breathed",
                        style = MaterialTheme.typography.labelMedium,
                        color = DesignTokens.TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${focusMinutes}m",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Text(
                        text = "Focused",
                        style = MaterialTheme.typography.labelMedium,
                        color = DesignTokens.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        // 4. Personalization configurations
        SectionHeader(title = "Personalization")
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

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

        LunafloraCard(
            onClick = { showThemeDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentThemeItem.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ColorLens,
                        contentDescription = "Select Theme",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showThemeDialog) {
            Dialog(onDismissRequest = { showThemeDialog = false }) {
                LunafloraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Sanctuary Theme",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            IconButton(
                                onClick = { showThemeDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = DesignTokens.TextSecondary
                                )
                            }
                        }

                        val themes = listOf(
                            ThemeItem("🌸 Sakura", "🌸 Pink accented light mode", "Pink"),
                            ThemeItem("✨ Aurora", "✨ Lavender accented dark mode", "Dark"),
                            ThemeItem("🌙 Cosmos", "🌙 Deep black AMOLED dark mode", "Amoled"),
                            ThemeItem("💮 Serene", "💮 Original calm light mode", "Serene"),
                            ThemeItem("🌿 Forest", "🌿 Green accented dark mode", "Green"),
                            ThemeItem("🌊 Aqua", "🌊 Blue ocean light mode", "Blue")
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
                        ) {
                            themes.forEach { themeItem ->
                                val themeModeVal = when (themeItem.name) {
                                    "💮 Serene" -> AtaraxiaThemeMode.LIGHT
                                    "✨ Aurora" -> AtaraxiaThemeMode.AURORA
                                    "🌙 Cosmos" -> AtaraxiaThemeMode.COSMOS
                                    "🌿 Forest" -> AtaraxiaThemeMode.FOREST
                                    "🌸 Sakura" -> AtaraxiaThemeMode.SAKURA
                                    "🌊 Aqua" -> AtaraxiaThemeMode.AQUA
                                    else -> AtaraxiaThemeMode.LIGHT
                                }
                                val isSelected = currentThemeMode == themeModeVal
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onThemeChange(themeModeVal)
                                            showThemeDialog = false
                                        }
                                        .padding(AtaraxiaTheme.spacing.Space12)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Outlined.ColorLens else Icons.Outlined.Palette,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                            Column {
                                                Text(
                                                    text = themeItem.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                    ),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
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

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        // 5. Preferences selectors
        SectionHeader(title = "Preferences")
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRow(
                    title = "Quiet Reminders",
                    subtitle = "Gentle prompts to keep reflecting",
                    icon = { Icon(Icons.Outlined.Notifications, null, tint = DesignTokens.TextPrimary) },
                    trailingContent = {
                        Switch(
                            checked = isNotificationsEnabled,
                            onCheckedChange = { isNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
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
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        // 6. Danger settings
        SectionHeader(title = "Danger Zone")
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRow(
                    title = "Reset Quiet Space",
                    subtitle = "Permanently delete reflections and settings history",
                    icon = { Icon(Icons.Outlined.DeleteForever, null, tint = DesignTokens.TextPrimary) },
                    onClick = { showClearDialog = true }
                )
                SettingRow(
                    title = "Ataraxia Info",
                    subtitle = "Version 0.2.0",
                    icon = { Icon(Icons.Outlined.Info, null, tint = DesignTokens.TextPrimary) }
                )
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

    if (showPinSetupDialog) {
        Dialog(onDismissRequest = { showPinSetupDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = DesignTokens.CardBackground,
                tonalElevation = AtaraxiaTheme.elevation.Medium
            ) {
                Column(modifier = Modifier.padding(AtaraxiaTheme.spacing.Space24)) {
                    Text(
                        text = "Set 4-Digit Passcode",
                        style = MaterialTheme.typography.titleLarge,
                        color = DesignTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                    PrimaryTextField(
                        value = tempPinInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                tempPinInput = input
                            }
                        },
                        placeholder = "Enter 4 digits",
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinSetupError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                        Text(
                            text = pinSetupError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaSecondaryButton(
                                text = "Cancel",
                                onClick = {
                                    showPinSetupDialog = false
                                    tempPinInput = ""
                                    pinSetupError = ""
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaPrimaryButton(
                                text = "Enable",
                                onClick = {
                                    if (tempPinInput.length == 4 && tempPinInput.all { it.isDigit() }) {
                                        onAppLockToggle(true, tempPinInput)
                                        showPinSetupDialog = false
                                        tempPinInput = ""
                                        pinSetupError = ""
                                    } else {
                                        pinSetupError = "Passcode must be exactly 4 digits."
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ThemeItem(val name: String, val desc: String, val badge: String)
