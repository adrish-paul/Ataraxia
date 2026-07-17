package com.example.ataraxia.features.focus.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun FocusPostSessionDialog(
    showNotesDialog: Boolean,
    onNotesSaved: (String) -> Unit
) {
    if (showNotesDialog) {
        var sessionNotesText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { /* Force action on buttons */ }) {
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
                        placeholder = "Type your thoughts, breakthroughs, or quiet moments...",
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaSecondaryButton(
                                text = "Discard",
                                onClick = { onNotesSaved("") }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaPrimaryButton(
                                text = "Save Notes",
                                onClick = { onNotesSaved(sessionNotesText) }
                            )
                        }
                    }
                }
            }
        }
    }
}
