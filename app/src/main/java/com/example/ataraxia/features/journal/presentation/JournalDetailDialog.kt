package com.example.ataraxia.features.journal.presentation

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.simpleVerticalScrollbar
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.util.StorageHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun JournalDetailDialog(
    entry: JournalEntryEntity?,
    onDismiss: () -> Unit,
    onToggleFavorite: (JournalEntryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    if (entry == null) return

    var detailMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingDetailPath by remember { mutableStateOf<String?>(null) }

    DisposableEffect(entry) {
        onDispose {
            detailMediaPlayer?.release()
            detailMediaPlayer = null
        }
    }

    Dialog(
        onDismissRequest = {
            detailMediaPlayer?.release()
            detailMediaPlayer = null
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
                .fillMaxWidth(0.92f)
                .height(550.dp)
        ) {
            Column(
                modifier = Modifier.padding(AtaraxiaTheme.spacing.Space16).fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.mood,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            onToggleFavorite(entry)
                        }) {
                            Icon(
                                imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Toggle Favorite",
                                tint = if (entry.isFavorite) Color(0xFFFFB300) else DesignTokens.TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = {
                        detailMediaPlayer?.release()
                        detailMediaPlayer = null
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DesignTokens.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                val detailScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .simpleVerticalScrollbar(detailScrollState)
                        .verticalScroll(detailScrollState)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = entry.title.ifBlank { "Untitled Reflection" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                    val locale = LocalConfiguration.current.locales[0]
                    val sdf = remember(locale) { SimpleDateFormat("MMM dd, yyyy", locale) }
                    val formattedDate = remember(entry.timestamp, sdf) { sdf.format(Date(entry.timestamp)) }
                    Text(
                        text = formattedDate + if (entry.weatherContext.isNotBlank()) " • ${entry.weatherContext}" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (entry.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            entry.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "#$tag", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (entry.imagePath.isNotEmpty()) {
                        val imagePaths = entry.imagePath.split(",").filter { it.isNotBlank() }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            imagePaths.forEach { path ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                ) {
                                    val bitmapState = remember(path) { mutableStateOf<ImageBitmap?>(null) }
                                    LaunchedEffect(path) {
                                        withContext(Dispatchers.IO) {
                                            try {
                                                val file = File(path)
                                                if (file.exists()) {
                                                    val decoded = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                                                    bitmapState.value = decoded
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    val bitmap = bitmapState.value
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Attachment Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Image not found", color = DesignTokens.TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (entry.voicePath.isNotEmpty()) {
                        val voicePaths = entry.voicePath.split(",").filter { it.isNotBlank() }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            voicePaths.forEachIndexed { index, path ->
                                val isPlayingThis = playingDetailPath == path
                                LunafloraCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🎙️", style = MaterialTheme.typography.titleLarge)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Voice Note ${index + 1}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DesignTokens.TextPrimary
                                            )
                                        }
                                        IconButton(onClick = {
                                            if (isPlayingThis) {
                                                detailMediaPlayer?.stop()
                                                detailMediaPlayer?.release()
                                                detailMediaPlayer = null
                                                playingDetailPath = null
                                            } else {
                                                try {
                                                    detailMediaPlayer?.release()
                                                    detailMediaPlayer = MediaPlayer().apply {
                                                        setDataSource(path)
                                                        prepare()
                                                        start()
                                                        setOnCompletionListener {
                                                            playingDetailPath = null
                                                            release()
                                                            detailMediaPlayer = null
                                                        }
                                                    }
                                                    playingDetailPath = path
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (isPlayingThis) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                                contentDescription = "Playback recording",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DesignTokens.TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaSecondaryButton(
                            text = "Delete",
                            onClick = {
                                entry.imagePath.split(",").forEach { path ->
                                    StorageHelper.deleteFile(path)
                                }
                                entry.voicePath.split(",").forEach { path ->
                                    StorageHelper.deleteFile(path)
                                }
                                onDeleteEntry(entry.id)
                                onDismiss()
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaPrimaryButton(
                            text = "Close",
                            onClick = {
                                detailMediaPlayer?.release()
                                detailMediaPlayer = null
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
