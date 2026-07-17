package com.example.ataraxia.features.breathe.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.components.AnimatedLotus
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.BreatheState
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BreatheActiveSession(
    pendingMethod: String,
    selectedMethod: String,
    onEndSession: (Int, String) -> Unit
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(BreatheState.REST) }
    var currentInstruction by remember { mutableStateOf("Prepare...") }
    var currentCounter by remember { mutableIntStateOf(2) }
    var completedCycles by remember { mutableIntStateOf(0) }

    fun requestEndSession() {
        onEndSession(elapsedSeconds, pendingMethod.ifEmpty { selectedMethod })
    }

    BackHandler(enabled = true) { requestEndSession() }

    LaunchedEffect(key1 = true) {
        val method = pendingMethod.ifEmpty { selectedMethod }

        val inhaleMs: Long = when (method) {
            "Box Breathing"       -> 4000L
            "4-7-8 Breathing"     -> 4000L
            "Triangle Breathing"  -> 4000L
            "Deep Calm"           -> 4000L
            "Cleansing Breath"    -> 2000L
            "Resonant Breathing"  -> 5500L
            "Calm Breathing"      -> 5000L
            else                  -> 4000L
        }
        val holdMs: Long = when (method) {
            "Box Breathing"       -> 4000L
            "4-7-8 Breathing"     -> 7000L
            "Triangle Breathing"  -> 4000L
            "Deep Calm"           -> 7000L
            else                  -> 0L
        }
        val exhaleMs: Long = when (method) {
            "Box Breathing"       -> 4000L
            "4-7-8 Breathing"     -> 8000L
            "Triangle Breathing"  -> 4000L
            "Deep Calm"           -> 8000L
            "Cleansing Breath"    -> 4000L
            "Resonant Breathing"  -> 5500L
            "Calm Breathing"      -> 5000L
            else                  -> 4000L
        }
        val pauseMs: Long = if (method == "Box Breathing") 4000L else 0L

        currentInstruction = "Prepare..."
        currentCounter = 2
        delay(2000.milliseconds)

        while (isActive) {
            // Inhale
            currentState = BreatheState.INHALE
            currentInstruction = "Breathe In..."
            val inhaleSteps = (inhaleMs / 1000).toInt()
            val inhaleTickMs = inhaleMs / inhaleSteps.coerceAtLeast(1)
            for (i in inhaleSteps downTo 1) { currentCounter = i; delay(inhaleTickMs.milliseconds); elapsedSeconds++ }

            // Hold
            if (holdMs > 0L) {
                currentState = BreatheState.HOLD
                currentInstruction = "Hold..."
                val holdSecs = (holdMs / 1000).toInt().coerceAtLeast(1)
                for (i in holdSecs downTo 1) { currentCounter = i; delay(1000.milliseconds); elapsedSeconds++ }
            }

            // Exhale
            currentState = BreatheState.EXHALE
            currentInstruction = "Breathe Out..."
            val exhaleSecs = (exhaleMs / 1000).toInt().coerceAtLeast(1)
            val exhaleTickMs = exhaleMs / exhaleSecs.coerceAtLeast(1)
            for (i in exhaleSecs downTo 1) { currentCounter = i; delay(exhaleTickMs.milliseconds); elapsedSeconds++ }

            // Pause
            if (pauseMs > 0L) {
                currentState = BreatheState.REST
                currentInstruction = "Rest..."
                val pauseSecs = (pauseMs / 1000).toInt().coerceAtLeast(1)
                for (i in pauseSecs downTo 1) { currentCounter = i; delay(1000.milliseconds); elapsedSeconds++ }
            }

            completedCycles++
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = AtaraxiaTheme.spacing.Space24)
            .padding(vertical = AtaraxiaTheme.spacing.Space16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { requestEndSession() }) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "End Session", tint = DesignTokens.TextPrimary)
            }
            Text(text = pendingMethod.ifEmpty { selectedMethod }, style = MaterialTheme.typography.labelLarge, color = DesignTokens.TextSecondary)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
            AnimatedLotus(breatheState = currentState, cycleDurationMs = 1500, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
            Text(text = currentInstruction, style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
            Text(text = "$currentCounter", style = MaterialTheme.typography.headlineLarge, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
            Text(text = "Cycles: $completedCycles", style = MaterialTheme.typography.labelLarge, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)
        }

        AtaraxiaSecondaryButton(text = "End Session", onClick = { requestEndSession() }, modifier = Modifier.padding(bottom = AtaraxiaTheme.spacing.Space16))
    }
}
