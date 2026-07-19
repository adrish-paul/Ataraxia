package com.example.ataraxia.features.focus.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun FocusPostSessionDialog(
    showNotesDialog: Boolean,
    pendingDurationMins: Int,
    pendingIsFlowMode: Boolean,
    pendingTargetMins: Int,
    selectedSpace: String,
    selectedIntention: FocusIntentionEntity,
    completionStatus: String, // "Completed" or "Cancelled"
    onNotesSaved: (notes: String, enjoyed: String, distracted: String, rating: Int, status: String) -> Unit
) {
    if (!showNotesDialog) return

    var rating by remember { mutableIntStateOf(0) }
    var notesText by remember { mutableStateOf("") }
    var enjoyedText by remember { mutableStateOf("") }
    var distractedText by remember { mutableStateOf("") }

    val congratulatoryMessages = remember {
        listOf(
            "You created space for meaningful work today.",
            "Every focused moment strengthens your attention.",
            "Quiet concentration is its own gentle reward.",
            "Slowing down helps build steady focus.",
            "Thank you for showing up for yourself today."
        )
    }
    val congratMessage = remember { congratulatoryMessages.random() }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { /* Force action on buttons */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        // Set window transparent background
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
                .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                .heightIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
            ) {
                // Headline
                Text(
                    text = if (completionStatus == "Completed") "Session Complete" else "Session Ended",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DesignTokens.TextPrimary,
                    textAlign = TextAlign.Center
                )

                // Congratulatory/Reflective subtitle
                Text(
                    text = congratMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = DesignTokens.TextSecondary,
                    textAlign = TextAlign.Center
                )

                // Session Summary Info Chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Intention", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${selectedIntention.icon} ${selectedIntention.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Duration", style = MaterialTheme.typography.labelSmall, color = DesignTokens.TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            val typeLabel = if (pendingIsFlowMode) "Flow" else "Timer"
                            Text(
                                text = "${pendingDurationMins}m ($typeLabel)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.15f))

                // Reflection: Rating Stars
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How focused did you feel?",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = DesignTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { index ->
                            val isSelected = index <= rating
                            val scale by animateFloatAsState(if (isSelected) 1.2f else 1.0f, label = "star_scale")
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$index Stars",
                                tint = if (isSelected) Color(0xFFFFC107) else DesignTokens.TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .scale(scale)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        rating = index
                                    }
                            )
                        }
                    }
                }

                // Reflection: What went well?
                Column {
                    Text(
                        text = "What went well today?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = DesignTokens.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PrimaryTextField(
                        value = enjoyedText,
                        onValueChange = { enjoyedText = it },
                        placeholder = "e.g. Cleared my inbox, remained calm"
                    )
                }

                // Reflection: What distracted you?
                Column {
                    Text(
                        text = "What distracted you?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = DesignTokens.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PrimaryTextField(
                        value = distractedText,
                        onValueChange = { distractedText = it },
                        placeholder = "e.g. Phone notifications, active workspace noise"
                    )
                }

                // Notes
                Column {
                    Text(
                        text = "Reflection Notes",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = DesignTokens.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PrimaryTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = "Type your thoughts, breakthroughs, or quiet moments...",
                        modifier = Modifier.height(80.dp)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaSecondaryButton(
                            text = "Skip",
                            onClick = {
                                // Single tap skip: saves immediately with empty reflection details
                                onNotesSaved("", "", "", 0, completionStatus)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaPrimaryButton(
                            text = "Save",
                            onClick = {
                                onNotesSaved(notesText, enjoyedText, distractedText, rating, completionStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}
