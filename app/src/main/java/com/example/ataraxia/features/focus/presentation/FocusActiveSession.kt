package com.example.ataraxia.features.focus.presentation

import android.app.NotificationManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import com.example.ataraxia.features.breathe.presentation.AmbientSoundEngine
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.AtaraxiaAudioSelectorDialog
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.graphics.toColorInt

@Composable
fun FocusActiveSession(
    selectedSpace: String,
    focusMode: String,
    targetMinutes: Int,
    selectedIntention: FocusIntentionEntity,
    keepScreenAwake: Boolean,
    allowCalls: Boolean,
    enableHalfwayReminder: Boolean,
    enableRemindersIntervalMins: Int, // 0 = disabled, otherwise 15, 30, etc.
    onEndSession: (elapsedMins: Int, isFlow: Boolean, status: String) -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var accumulatedSeconds by remember { mutableIntStateOf(0) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isSessionCompleted by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var wasPausedBeforeDialog by remember { mutableStateOf(false) }
    var isDimmedOverlayEnabled by remember { mutableStateOf(false) }

    // Ambient sound configurations
    var activeSound by remember { mutableStateOf("None") }
    var activeVolume by remember { mutableFloatStateOf(0.5f) }
    var showAudioDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    val ambientEngine = remember { AmbientSoundEngine() }

    val targetSeconds = remember(targetMinutes, focusMode) {
        if (focusMode == "Timer") targetMinutes * 60 else 0
    }

    val view = LocalView.current
    DisposableEffect(keepScreenAwake) {
        if (keepScreenAwake) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    // Clean release ambient audio engine
    DisposableEffect(Unit) {
        onDispose {
            ambientEngine.release()
        }
    }

    // Update ambient playback state based on pause and volume changes
    LaunchedEffect(activeSound, activeVolume, isPaused, isSessionCompleted) {
        if (isPaused || isSessionCompleted || activeSound == "None") {
            ambientEngine.setPaused(true)
        } else {
            ambientEngine.start(context, activeSound, activeVolume)
            ambientEngine.setPaused(false)
        }
    }

    // Timer logic utilizing system time to prevent drift
    LaunchedEffect(isPaused, isSessionCompleted) {
        if (isPaused) {
            accumulatedSeconds += ((System.currentTimeMillis() - startTime) / 1000).toInt()
            elapsedSeconds = accumulatedSeconds
        } else {
            startTime = System.currentTimeMillis()
        }
    }

    // Rhythmic timers ticks loop
    LaunchedEffect(isPaused, isSessionCompleted) {
        if (!isPaused && !isSessionCompleted) {
            while (isActive) {
                delay(300.milliseconds)
                val currentElapsed = accumulatedSeconds + ((System.currentTimeMillis() - startTime) / 1000).toInt()
                elapsedSeconds = currentElapsed

                // Countdown mode completion check
                if (focusMode == "Timer" && currentElapsed >= targetSeconds) {
                    isSessionCompleted = true
                    triggerHapticPulse(vibrator, longArrayOf(0, 300, 150, 300)) // completion vibration
                    delay(2000.milliseconds) // allow completion bloom animation to display
                    val totalMins = if (targetMinutes == 0) 1 else targetMinutes
                    onEndSession(totalMins, false, "Completed")
                    break
                }

                // Halfway reminder countdown check
                if (focusMode == "Timer" && enableHalfwayReminder && currentElapsed == targetSeconds / 2) {
                    triggerHapticPulse(vibrator, longArrayOf(0, 200))
                }

                // Flow mode interval reminder check
                if (focusMode == "Flow" && enableRemindersIntervalMins > 0 && currentElapsed > 0 && currentElapsed % (enableRemindersIntervalMins * 60) == 0) {
                    triggerHapticPulse(vibrator, longArrayOf(0, 150))
                }
            }
        }
    }

    // DND Interruption policies setup
    LaunchedEffect(isPaused, allowCalls, isSessionCompleted) {
        val enabled = !isPaused && !isSessionCompleted
        try {
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
        } catch (_: Exception) {}
    }

    // Reset DND on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            } catch (_: Exception) {}
        }
    }

    // Cancellation request confirmation handler
    fun requestEndSession() {
        wasPausedBeforeDialog = isPaused
        isPaused = true
        showCancelConfirmation = true
    }

    BackHandler(enabled = true) {
        requestEndSession()
    }

    // Animated visual flow gradient
    val transition = rememberInfiniteTransition(label = "pulse_flow")
    val pulseFactor by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val flowColor1 = Color(0xFFB9A7D6)
    val flowColor2 = Color(0xFF6F6CA8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val brush = Brush.radialGradient(
                    colors = listOf(
                        flowColor1.copy(alpha = pulseFactor * 0.12f),
                        flowColor2.copy(alpha = pulseFactor * 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.85f
                )
                drawRect(brush = brush)
            }
    ) {
        // Active Distraction-Free Column Layout
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
            // Distraction free topbar: audio settings button on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio configuration indicator
                IconButton(onClick = { showAudioDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = "Audio settings",
                        tint = if (activeSound != "None") MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
                    )
                }
            }

            // Central Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                if (isSessionCompleted) {
                    // Bloom Animation at Timer Completion
                    BloomVisualEffect(modifier = Modifier.size(160.dp))
                } else {
                    if (focusMode == "Timer") {
                        val remainingSecs = (targetSeconds - elapsedSeconds).coerceAtLeast(0)
                        FocusCountdownCircle(
                            remainingSeconds = remainingSecs,
                            totalSeconds = targetSeconds,
                            isPaused = isPaused,
                            modifier = Modifier.size(170.dp)
                        )
                    } else {
                        // Flow Mode dynamic pulse
                        FlowPulseVisualizer(
                            elapsedSeconds = elapsedSeconds,
                            isPaused = isPaused,
                            pulseFactor = pulseFactor,
                            modifier = Modifier.size(170.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

                // Workspace name & active Intention Badge
                Text(
                    text = selectedSpace,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = DesignTokens.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Color(selectedIntention.colorHex.toColorInt()).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = selectedIntention.icon, fontSize = 14.sp)
                    Text(
                        text = selectedIntention.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(selectedIntention.colorHex.toColorInt())
                    )
                }
            }

            // Bottom control bar (Pause & Stop) matching Breathe Session Screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AtaraxiaTheme.spacing.Space16),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

                // Dim Screen outlined casing button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = DesignTokens.TextSecondary.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .clickable { isDimmedOverlayEnabled = true }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dim Screen",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextSecondary
                    )
                }
            }
        }

        // Active Dimming overlay
        if (isDimmedOverlayEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isDimmedOverlayEnabled = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Workspace Dimmed",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap anywhere to light up screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Confirmation cancellation dialog
        if (showCancelConfirmation) {
            Dialog(onDismissRequest = {
                showCancelConfirmation = false
                isPaused = wasPausedBeforeDialog
            }) {
                LunafloraCard(modifier = Modifier.fillMaxWidth().padding(horizontal = AtaraxiaTheme.spacing.Space8)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "End Focus Session?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Do you want to exit your peaceful workspace? Your progress up to this point will be saved.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesignTokens.TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaSecondaryButton(
                                    text = "End",
                                    onClick = {
                                        showCancelConfirmation = false
                                        val totalMins = elapsedSeconds / 60
                                        val logMins = if (totalMins == 0 && elapsedSeconds >= 5) 1 else totalMins
                                        onEndSession(logMins, focusMode == "Flow", "Cancelled")
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaPrimaryButton(
                                    text = "Resume",
                                    onClick = {
                                        showCancelConfirmation = false
                                        isPaused = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Session Ambient Audio Settings Dialog
        AtaraxiaAudioSelectorDialog(
            showDialog = showAudioDialog,
            onDismiss = { showAudioDialog = false },
            selectedSound = activeSound,
            onSoundSelected = { activeSound = it },
            soundVolume = activeVolume,
            onVolumeChanged = { activeVolume = it }
        )
    }
}

@Composable
fun FocusCountdownCircle(
    remainingSeconds: Int,
    totalSeconds: Int,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    val primaryColor = MaterialTheme.colorScheme.primary
    val textPrimary = DesignTokens.TextPrimary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val radius = size.minDimension / 2f
            val strokeWidth = 8.dp.toPx()

            // Track background
            drawCircle(
                color = primaryColor.copy(alpha = 0.12f),
                radius = radius - strokeWidth / 2,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Sweep Arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayMins = remainingSeconds / 60
            val displaySecs = remainingSeconds % 60
            Text(
                text = String.format(java.util.Locale.US, "%02d:%02d", displayMins, displaySecs),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isPaused) "Paused" else "Remaining",
                style = MaterialTheme.typography.labelSmall,
                color = DesignTokens.TextSecondary
            )
        }
    }
}

@Composable
fun FlowPulseVisualizer(
    elapsedSeconds: Int,
    isPaused: Boolean,
    pulseFactor: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textPrimary = DesignTokens.TextPrimary

    val infiniteTransition = rememberInfiniteTransition(label = "lotus_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val radius = (size.minDimension / 2f) * (if (isPaused) 0.7f else pulseFactor)
            val strokeWidth = 3.dp.toPx()

            // Draw clean breathing lotus pattern
            val petals = 8
            for (i in 0 until petals) {
                val angle = rotationAngle + (i * (360f / petals))
                val angleRad = Math.toRadians(angle.toDouble())
                val endX = (center.x + radius * Math.cos(angleRad)).toFloat()
                val endY = (center.y + radius * Math.sin(angleRad)).toFloat()

                // Draw petal loop
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = radius * 0.45f,
                    center = Offset((center.x + endX) / 2, (center.y + endY) / 2),
                    style = Stroke(width = strokeWidth)
                )
            }

            // Draw center anchor
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayMins = elapsedSeconds / 60
            val displaySecs = elapsedSeconds % 60
            Text(
                text = String.format(java.util.Locale.US, "%02d:%02d", displayMins, displaySecs),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isPaused) "Paused" else "Flowing",
                style = MaterialTheme.typography.labelSmall,
                color = DesignTokens.TextSecondary
            )
        }
    }
}

@Composable
fun BloomVisualEffect(modifier: Modifier = Modifier) {
    val bloomTransition = rememberInfiniteTransition(label = "bloom")
    val sizeFactor by bloomTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bloom_size"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val maxRadius = size.minDimension / 2f
            
            // Pulsing blooming ring
            drawCircle(
                color = primaryColor.copy(alpha = (1f - sizeFactor) * 0.4f),
                radius = maxRadius * sizeFactor,
                center = center
            )
            
            drawCircle(
                color = primaryColor,
                radius = 12.dp.toPx(),
                center = center
            )
        }
        Text(
            text = "✨",
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun triggerHapticPulse(vibrator: Vibrator?, pattern: LongArray) {
    vibrator?.let {
        if (it.hasVibrator()) {
            try {
                it.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } catch (_: Exception) {}
        }
    }
}
