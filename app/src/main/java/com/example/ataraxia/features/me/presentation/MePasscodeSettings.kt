package com.example.ataraxia.features.me.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun MePasscodeSettings(
    showPinSetupDialog: Boolean,
    onDismiss: () -> Unit,
    onAppLockToggle: (Boolean, String) -> Unit
) {
    if (showPinSetupDialog) {
        var tempPinInput by remember { mutableStateOf("") }
        var pinSetupError by remember { mutableStateOf("") }

        Dialog(onDismissRequest = onDismiss) {
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
                                    onDismiss()
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaPrimaryButton(
                                text = "Enable",
                                onClick = {
                                    if (tempPinInput.length == 4 && tempPinInput.all { it.isDigit() }) {
                                        onAppLockToggle(true, tempPinInput)
                                        onDismiss()
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
