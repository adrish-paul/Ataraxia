package com.example.ataraxia.features.breathe.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

data class MethodItem(
    val name: String,
    val desc: String,
    val pattern: String,
    val inhale: Int,
    val hold: Int,
    val exhale: Int,
    val rest: Int,
    val isCustom: Boolean = false
)

@Composable
fun BreatheMethodSelector(
    showMethodPopup: Boolean,
    selectedMethod: String,
    methods: List<MethodItem>,
    onDismiss: () -> Unit,
    onMethodSelected: (String) -> Unit,
    onSaveCustomStyle: (MethodItem) -> Unit,
    onEditCustomStyle: (String, MethodItem) -> Unit,
    onDeleteCustomStyle: (String) -> Unit
) {
    var showCustomStyleDialog by remember { mutableStateOf(false) }
    var editingStyle by remember { mutableStateOf<MethodItem?>(null) }

    if (showMethodPopup) {
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
                            text = "Select Breathing Method",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    editingStyle = null
                                    showCustomStyleDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Custom Style",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = DesignTokens.TextSecondary
                                )
                            }
                        }
                    }

                    // 5.5 rows scrollable box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(425.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
                        ) {
                            methods.forEach { method ->
                                val isSelected = selectedMethod == method.name
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onMethodSelected(method.name)
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
                                                imageVector = if (isSelected) Icons.Outlined.LocalFlorist else Icons.Outlined.Spa,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = method.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                    ),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                                )
                                                Text(
                                                    text = method.desc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = DesignTokens.TextSecondary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = method.pattern,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
                                            )

                                            if (method.isCustom) {
                                                var menuExpanded by remember { mutableStateOf(false) }
                                                Box {
                                                    IconButton(
                                                        onClick = { menuExpanded = true },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MoreVert,
                                                            contentDescription = "Options",
                                                            tint = DesignTokens.TextSecondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    DropdownMenu(
                                                        expanded = menuExpanded,
                                                        onDismissRequest = { menuExpanded = false },
                                                        modifier = Modifier.background(DesignTokens.CardBackground)
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Edit", color = DesignTokens.TextPrimary) },
                                                            onClick = {
                                                                menuExpanded = false
                                                                editingStyle = method
                                                                showCustomStyleDialog = true
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                                            onClick = {
                                                                menuExpanded = false
                                                                onDeleteCustomStyle(method.name)
                                                            }
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
            }
        }
    }

    // Dialog for creating/editing custom styles
    CustomBreathingStyleDialog(
        showDialog = showCustomStyleDialog,
        editingStyle = editingStyle,
        onDismiss = { showCustomStyleDialog = false },
        onSave = { savedStyle ->
            showCustomStyleDialog = false
            if (editingStyle != null) {
                onEditCustomStyle(editingStyle!!.name, savedStyle)
            } else {
                onSaveCustomStyle(savedStyle)
            }
        }
    )
}

@Composable
fun CustomBreathingStyleDialog(
    showDialog: Boolean,
    editingStyle: MethodItem?,
    onDismiss: () -> Unit,
    onSave: (MethodItem) -> Unit
) {
    if (showDialog) {
        var name by remember(editingStyle) { mutableStateOf(editingStyle?.name ?: "") }
        var inhale by remember(editingStyle) { mutableFloatStateOf(editingStyle?.inhale?.toFloat() ?: 4f) }
        var hold by remember(editingStyle) { mutableFloatStateOf(editingStyle?.hold?.toFloat() ?: 4f) }
        var exhale by remember(editingStyle) { mutableFloatStateOf(editingStyle?.exhale?.toFloat() ?: 4f) }
        var rest by remember(editingStyle) { mutableFloatStateOf(editingStyle?.rest?.toFloat() ?: 4f) }

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
            LunafloraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space8)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AtaraxiaTheme.spacing.Space8),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = if (editingStyle != null) "Edit Custom Style" else "Create Custom Style",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Style Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = DesignTokens.TextSecondary.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = DesignTokens.TextSecondary
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Breathe In: ${inhale.toInt()}s", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                        Slider(
                            value = inhale,
                            onValueChange = { inhale = it },
                            valueRange = 1f..15f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hold: ${hold.toInt()}s", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                        Slider(
                            value = hold,
                            onValueChange = { hold = it },
                            valueRange = 0f..15f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Breathe Out: ${exhale.toInt()}s", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                        Slider(
                            value = exhale,
                            onValueChange = { exhale = it },
                            valueRange = 0f..15f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Rest: ${rest.toInt()}s", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.TextPrimary)
                        Slider(
                            value = rest,
                            onValueChange = { rest = it },
                            valueRange = 0f..15f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignTokens.TextSecondary)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val pattern = buildString {
                                        append(inhale.toInt())
                                        if (hold.toInt() > 0 || exhale.toInt() > 0 || rest.toInt() > 0) {
                                            append(" - ")
                                            append(hold.toInt())
                                        }
                                        if (exhale.toInt() > 0 || rest.toInt() > 0) {
                                            append(" - ")
                                            append(exhale.toInt())
                                        }
                                        if (rest.toInt() > 0) {
                                            append(" - ")
                                            append(rest.toInt())
                                        }
                                    }
                                    onSave(
                                        MethodItem(
                                            name = name.trim(),
                                            desc = "Custom breathing pattern.",
                                            pattern = pattern,
                                            inhale = inhale.toInt(),
                                            hold = hold.toInt(),
                                            exhale = exhale.toInt(),
                                            rest = rest.toInt(),
                                            isCustom = true
                                        )
                                    )
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
