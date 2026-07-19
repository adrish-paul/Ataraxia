package com.example.ataraxia.features.breathe.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.ui.components.AnimatedLotus
import com.example.ataraxia.ui.components.BreatheState
import com.example.ataraxia.ui.components.AtaraxiaAudioSelectorDialog
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BreatheActiveSession(
    selectedMethod: String,
    inhaleSeconds: Int,
    holdSeconds: Int,
    exhaleSeconds: Int,
    restSeconds: Int,
    targetDurationMinutes: Int,
    selectedSound: String,
    soundVolume: Float,
    hapticGuidanceEnabled: Boolean,
    onEndSession: (durationSeconds: Int, methodName: String, completed: Boolean) -> Unit
) {
    val context = LocalContext.current

    // Session states
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var currentPhase by remember { mutableStateOf(BreatheState.REST) }
    var phaseRemainingSeconds by remember { mutableIntStateOf(3) } // 3s Prep
    var isPrepPeriod by remember { mutableStateOf(true) }
    var completedCycles by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }
    var isTargetDurationReached by remember { mutableStateOf(false) }

    // On-the-fly ambient sound settings state
    var activeSound by remember { mutableStateOf(selectedSound) }
    var activeVolume by remember { mutableStateOf(soundVolume) }
    var showSoundDialog by remember { mutableStateOf(false) }

    val instructionText = when {
        isPrepPeriod -> "Prepare..."
        currentPhase == BreatheState.INHALE -> "Breathe In"
        currentPhase == BreatheState.HOLD -> "Hold"
        currentPhase == BreatheState.EXHALE -> "Breathe Out"
        currentPhase == BreatheState.REST -> "Rest"
        else -> ""
    }

    // Ambient Sound Playback
    val ambientEngine = remember { AmbientSoundEngine() }
    LaunchedEffect(activeSound) {
        if (activeSound == "None") {
            ambientEngine.stop()
        } else {
            ambientEngine.start(context, activeSound, activeVolume)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            ambientEngine.release()
        }
    }
    LaunchedEffect(activeVolume) {
        ambientEngine.setVolume(activeVolume)
    }
    LaunchedEffect(isPaused) {
        ambientEngine.setPaused(isPaused)
    }

    // Precise, drift-free timer state machine loop
    LaunchedEffect(isPaused) {
        if (isPaused) {
            return@LaunchedEffect
        }
        while (true) {
            delay(1000.milliseconds)
            
            if (isPrepPeriod) {
                phaseRemainingSeconds--
                if (phaseRemainingSeconds <= 0) {
                    isPrepPeriod = false
                    currentPhase = BreatheState.INHALE
                    phaseRemainingSeconds = inhaleSeconds
                    triggerHaptic(context, hapticGuidanceEnabled, 300L)
                }
                continue
            }

            elapsedSeconds++
            if (elapsedSeconds >= targetDurationMinutes * 60) {
                isTargetDurationReached = true
            }

            phaseRemainingSeconds--
            if (phaseRemainingSeconds <= 0) {
                // Transition to next phase
                when (currentPhase) {
                    BreatheState.INHALE -> {
                        if (holdSeconds > 0) {
                            currentPhase = BreatheState.HOLD
                            phaseRemainingSeconds = holdSeconds
                        } else {
                            currentPhase = BreatheState.EXHALE
                            phaseRemainingSeconds = exhaleSeconds
                        }
                    }
                    BreatheState.HOLD -> {
                        currentPhase = BreatheState.EXHALE
                        phaseRemainingSeconds = exhaleSeconds
                    }
                    BreatheState.EXHALE -> {
                        if (restSeconds > 0) {
                            currentPhase = BreatheState.REST
                            phaseRemainingSeconds = restSeconds
                        } else {
                            // Cycle completes here (No rest phase in rhythm)
                            if (isTargetDurationReached) {
                                onEndSession(elapsedSeconds, selectedMethod, true)
                                break
                            }
                            currentPhase = BreatheState.INHALE
                            phaseRemainingSeconds = inhaleSeconds
                            completedCycles++
                        }
                    }
                    BreatheState.REST -> {
                        // Cycle completes here
                        if (isTargetDurationReached) {
                            onEndSession(elapsedSeconds, selectedMethod, true)
                            break
                        }
                        currentPhase = BreatheState.INHALE
                        phaseRemainingSeconds = inhaleSeconds
                        completedCycles++
                    }
                }
                // Trigger vibration on every phase transition
                triggerHaptic(context, hapticGuidanceEnabled, 300L)
            }
        }
    }

    fun requestEndSession() {
        isPaused = true
        showExitConfirm = true
    }

    BackHandler(enabled = true) {
        requestEndSession()
    }

    // Calming themed gradient background cached via remember to avoid allocation
    val primaryColor = MaterialTheme.colorScheme.primary
    val appBackground = DesignTokens.AppBackground
    val gradientBackground = remember(primaryColor, appBackground) {
        Brush.verticalGradient(
            colors = listOf(
                appBackground,
                primaryColor.copy(alpha = 0.08f),
                appBackground
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AtaraxiaTheme.spacing.Space24)
            .padding(vertical = AtaraxiaTheme.spacing.Space16)
    ) {
        // Exit Dialog
        if (showExitConfirm) {
            AlertDialog(
                onDismissRequest = {
                    showExitConfirm = false
                    isPaused = false
                },
                title = { Text("End Session?", fontWeight = FontWeight.Bold, color = DesignTokens.TextPrimary) },
                text = { Text("You are doing wonderfully. Pause and stay a moment longer to anchor yourself.", color = DesignTokens.TextSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        showExitConfirm = false
                        onEndSession(elapsedSeconds, selectedMethod, false)
                    }) {
                        Text("Yes, End Session", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExitConfirm = false
                        isPaused = false
                    }) {
                        Text("No, Stay", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = DesignTokens.CardBackground,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Entire content stacked vertically to prevent overlaps under any screen height
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section (balanced center text with sound settings trigger)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // Balancing spacer
                Text(
                    text = selectedMethod,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = DesignTokens.TextSecondary
                )
                IconButton(
                    onClick = { showSoundDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = "Audio Settings",
                        tint = DesignTokens.TextPrimary
                    )
                }
            }

            // Main Breathing Space Column filling the remaining height
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedLotus(
                    breatheState = currentPhase,
                    cycleDurationMs = 1200,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                Text(
                    text = instructionText,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = "$phaseRemainingSeconds",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                // Capsule card for cycle and timer (left/right rounded, top/bottom flat)
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = DesignTokens.CardBackground),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cycles: $completedCycles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextSecondary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleMedium,
                            color = DesignTokens.TextSecondary
                        )
                        Text(
                            text = formatActiveTime(elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextSecondary
                        )
                    }
                }
            }

            // Volume Slider directly in session screen (only when a sound is selected)
            if (activeSound != "None") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AtaraxiaTheme.spacing.Space8),
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
                        value = activeVolume,
                        onValueChange = { activeVolume = it },
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
                        text = "${(activeVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Controls bar (Pause & Stop) at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AtaraxiaTheme.spacing.Space8),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume (Capsule shaped: left/right rounded, top/bottom flat)
                FloatingActionButton(
                    onClick = { isPaused = !isPaused },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.size(width = 80.dp, height = 56.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause"
                    )
                }

                // Stop Session (Capsule shaped: left/right rounded, top/bottom flat)
                FloatingActionButton(
                    onClick = { requestEndSession() },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.size(width = 80.dp, height = 56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = "Stop Session"
                    )
                }
            }
        }
    }

    // Audio Selector Dialog on-the-fly
    AtaraxiaAudioSelectorDialog(
        showDialog = showSoundDialog,
        onDismiss = { showSoundDialog = false },
        selectedSound = activeSound,
        onSoundSelected = { activeSound = it },
        soundVolume = activeVolume,
        onVolumeChanged = { activeVolume = it }
    )
}

private fun formatActiveTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    @Suppress("DefaultLocale")
    return String.format("%02d:%02d", mins, secs)
}

private fun triggerHaptic(context: android.content.Context, sessionEnabled: Boolean, ms: Long = 300L) {
    val isGlobalHapticsEnabled = context.getSharedPreferences("ataraxia_global_prefs", android.content.Context.MODE_PRIVATE).getBoolean("global_haptics", true)
    if (!sessionEnabled || !isGlobalHapticsEnabled) return
    try {
        val vibrator = context.getSystemService(android.os.Vibrator::class.java)
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(ms, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        }
    } catch (_: Exception) {}
}
