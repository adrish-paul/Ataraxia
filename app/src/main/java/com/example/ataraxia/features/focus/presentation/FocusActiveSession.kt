package com.example.ataraxia.features.focus.presentation

import android.app.NotificationManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FocusActiveSession(
    selectedSpace: String,
    focusMode: String,
    keepScreenAwake: Boolean,
    allowCalls: Boolean,
    onEndSession: (Int, Boolean) -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var accumulatedSeconds by remember { mutableIntStateOf(0) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    val view = LocalView.current
    DisposableEffect(keepScreenAwake) {
        if (keepScreenAwake) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(isPaused) {
        if (isPaused) {
            accumulatedSeconds += ((System.currentTimeMillis() - startTime) / 1000).toInt()
            elapsedSeconds = accumulatedSeconds
        } else {
            startTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (isActive) {
                delay(500.milliseconds)
                elapsedSeconds = accumulatedSeconds + ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }
    }

    LaunchedEffect(isPaused, allowCalls) {
        val enabled = !isPaused
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun requestEndSession() {
        onEndSession(elapsedSeconds, focusMode == "Flow")
    }

    BackHandler(enabled = true) { requestEndSession() }

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
    ) {
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
                IconButton(onClick = { requestEndSession() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                    text = if (isPaused) "Session Paused" else "Focus Active: ${String.format(java.util.Locale.US, "%02d:%02d", displayMins, displaySecs)}",
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
                        onClick = { requestEndSession() }
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
}
