package com.example.ataraxia.features.focus.presentation

import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.roundToInt
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import androidx.core.content.edit

@Composable
fun FocusIntentionSelectorDialog(
    showDialog: Boolean,
    intentions: List<FocusIntentionEntity>,
    selectedIntention: FocusIntentionEntity,
    onDismiss: () -> Unit,
    onIntentionSelected: (FocusIntentionEntity) -> Unit,
    onAddCustomIntention: (name: String, icon: String, colorHex: String, description: String) -> Unit
) {
    if (!showDialog) return

    var isAddingCustom by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        // Remove standard dialog background padding shadows
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

        val context = LocalContext.current
        val savedOrder = remember {
            context.getSharedPreferences("ataraxia_prefs", android.content.Context.MODE_PRIVATE)
                .getString("focus_intentions_order", "") ?: ""
        }
        var orderedIntentions by remember(intentions, savedOrder) {
            mutableStateOf<List<FocusIntentionEntity>>(
                if (savedOrder.isBlank()) {
                    intentions
                } else {
                    val orderMap = savedOrder.split(",").mapIndexed { idx, name -> name to idx }.toMap()
                    intentions.sortedBy { orderMap[it.name] ?: 999 }
                }
            )
        }

        val itemBounds = remember { mutableStateMapOf<String, Rect>() }
        var draggedIndex by remember { mutableStateOf<Int?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }

        val infiniteTransition = rememberInfiniteTransition(label = "wobble")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = -1.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(120, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wobbleRotation"
        )

        LunafloraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                .heightIn(max = 520.dp)
        ) {
            if (isAddingCustom) {
                CustomIntentionForm(
                    onBack = { isAddingCustom = false },
                    onSave = { name, icon, color, desc ->
                        onAddCustomIntention(name, icon, color, desc)
                        isAddingCustom = false
                    }
                )
            } else {
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
                            text = "Focus Intention",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DesignTokens.TextSecondary
                            )
                        }
                    }

                    Text(
                        text = "Intentions help organize your session without scoring or tracking stats achievements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DesignTokens.TextSecondary
                    )

                    // Intention Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orderedIntentions.size, key = { orderedIntentions[it].name }) { index ->
                            val item = orderedIntentions[index]
                            val isSelected = item.name == selectedIntention.name
                            val isDraggingThis = draggedIndex == index
                            val currentIndexState = rememberUpdatedState(index)

                            val itemModifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .onGloballyPositioned { coords ->
                                    itemBounds[item.name] = coords.boundsInWindow()
                                }
                                .zIndex(if (isDraggingThis) 2f else 1f)
                                .then(
                                    if (isDraggingThis) {
                                        Modifier.offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                                    } else {
                                        Modifier
                                    }
                                )
                                .graphicsLayer {
                                    if (isDraggingThis) {
                                        rotationZ = rotationAngle
                                        scaleX = 1.06f
                                        scaleY = 1.06f
                                        alpha = 0.9f
                                    }
                                }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
                                )
                                .pointerInput(item.name) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedIndex = currentIndexState.value
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                            val currentIndex = draggedIndex
                                            if (currentIndex != null) {
                                                val currentItem = orderedIntentions[currentIndex]
                                                val currentBounds = itemBounds[currentItem.name]
                                                if (currentBounds != null) {
                                                    val currentCenterX = currentBounds.left + currentBounds.width / 2 + dragOffset.x
                                                    val currentCenterY = currentBounds.top + currentBounds.height / 2 + dragOffset.y
                                                    val targetEntry = itemBounds.entries.firstOrNull { (name, bounds) ->
                                                        name != currentItem.name && bounds.contains(Offset(currentCenterX, currentCenterY))
                                                    }
                                                    if (targetEntry != null) {
                                                        val targetName = targetEntry.key
                                                        val targetIndex = orderedIntentions.indexOfFirst { it.name == targetName }
                                                        if (targetIndex != -1) {
                                                            val listCopy = orderedIntentions.toMutableList()
                                                            val temp = listCopy[currentIndex]
                                                            listCopy[currentIndex] = listCopy[targetIndex]
                                                            listCopy[targetIndex] = temp
                                                            orderedIntentions = listCopy

                                                            context.getSharedPreferences("ataraxia_prefs", android.content.Context.MODE_PRIVATE)
                                                                .edit {
                                                                    putString(
                                                                        "focus_intentions_order",
                                                                        listCopy.joinToString(",") { it.name })
                                                                }

                                                            val oldBounds = currentBounds
                                                            val newBounds = targetEntry.value
                                                            dragOffset = Offset(
                                                                dragOffset.x + oldBounds.left - newBounds.left,
                                                                dragOffset.y + oldBounds.top - newBounds.top
                                                            )
                                                            draggedIndex = targetIndex
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggedIndex = null
                                        },
                                        onDragCancel = {
                                            draggedIndex = null
                                        }
                                    )
                                }
                                .clickable {
                                    if (draggedIndex == null) {
                                        onIntentionSelected(item)
                                        onDismiss()
                                    }
                                }
                                .padding(12.dp)

                            Box(
                                modifier = itemModifier
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = item.icon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    AtaraxiaSecondaryButton(
                        text = "+ Add Custom Intention",
                        onClick = { isAddingCustom = true }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomIntentionForm(
    onBack: () -> Unit,
    onSave: (name: String, icon: String, colorHex: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("✨") }
    var description by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Intention",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DesignTokens.TextPrimary
            )
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Back",
                    tint = DesignTokens.TextSecondary
                )
            }
        }

        Column {
            Text(
                text = "Intention Name",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DesignTokens.TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            PrimaryTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Study, Cooking, Yoga"
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Emoji Icon",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DesignTokens.TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            PrimaryTextField(
                value = icon,
                onValueChange = { icon = it.take(2) }, // Limit to 1-2 chars emoji
                placeholder = "✨"
            )
        }

        Column {
            Text(
                text = "Description",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DesignTokens.TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            PrimaryTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Brief explanation of this quiet workspace focus..."
            )
        }

        AtaraxiaPrimaryButton(
            text = "Save",
            onClick = {
                if (name.isNotBlank()) {
                    onSave(name, icon, "#B9A7D6", description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
