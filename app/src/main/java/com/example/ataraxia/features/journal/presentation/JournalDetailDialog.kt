package com.example.ataraxia.features.journal.presentation

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
    entries: List<JournalEntryEntity>,
    onDismiss: () -> Unit,
    onToggleFavorite: (JournalEntryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    if (entry == null) return

    val liveEntry = remember(entry, entries) {
        entries.firstOrNull { it.id == entry.id } ?: entry
    }

    var detailMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingDetailPath by remember { mutableStateOf<String?>(null) }
    var zoomImageIndex by remember { mutableStateOf<Int?>(null) }

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
                            text = liveEntry.mood,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            onToggleFavorite(liveEntry)
                        }) {
                            Icon(
                                imageVector = if (liveEntry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Toggle Favorite",
                                tint = if (liveEntry.isFavorite) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
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
                        text = liveEntry.title.ifBlank { "Untitled Reflection" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                    val locale = LocalConfiguration.current.locales[0]
                    val sdf = remember(locale) { SimpleDateFormat("MMM dd, yyyy", locale) }
                    val formattedDate = remember(liveEntry.timestamp, sdf) { sdf.format(Date(liveEntry.timestamp)) }
                    Text(
                        text = formattedDate + if (liveEntry.weatherContext.isNotBlank()) " • ${liveEntry.weatherContext}" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (liveEntry.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            liveEntry.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
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

                    if (liveEntry.imagePath.isNotEmpty()) {
                        val imagePaths = liveEntry.imagePath.split(",").filter { it.isNotBlank() }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            imagePaths.forEachIndexed { index, path ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable { zoomImageIndex = index }
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

                    if (liveEntry.voicePath.isNotEmpty()) {
                        val voicePaths = liveEntry.voicePath.split(",").filter { it.isNotBlank() }
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
                        text = parseMarkdown(liveEntry.content),
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
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
                                liveEntry.imagePath.split(",").forEach { path ->
                                    StorageHelper.deleteFile(path)
                                }
                                liveEntry.voicePath.split(",").forEach { path ->
                                    StorageHelper.deleteFile(path)
                                }
                                onDeleteEntry(liveEntry.id)
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

        zoomImageIndex?.let { startIndex ->
            val imagePaths = remember(liveEntry.imagePath) { liveEntry.imagePath.split(",").filter { it.isNotBlank() } }
            Dialog(
                onDismissRequest = { zoomImageIndex = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { imagePaths.size })
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val path = imagePaths[pageIndex]
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }

                        val bitmapState = remember(path) { mutableStateOf<ImageBitmap?>(null) }
                        LaunchedEffect(path) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val file = File(path)
                                    if (file.exists()) {
                                        bitmapState.value = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        offset = if (scale > 1f) offset + pan else Offset.Zero
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            bitmapState.value?.let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Zoomed Photo attachment",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { zoomImageIndex = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close zoom viewer",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun parseMarkdown(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = text.split("\n")
    lines.forEachIndexed { i, line ->
        if (line.startsWith("### ")) {
            val headerText = line.substring(4)
            builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary))
            builder.append(headerText)
            builder.pop()
        } else if (line.startsWith("- ") || line.startsWith("* ")) {
            val itemText = line.substring(2)
            builder.append("  •  ")
            appendInlineFormattedText(builder, itemText)
        } else if (line.matches("^\\d+\\.\\s.*".toRegex())) {
            val dotIdx = line.indexOf('.')
            val numberStr = line.substring(0, dotIdx + 1)
            val itemText = line.substring(dotIdx + 2)
            builder.append("  $numberStr  ")
            appendInlineFormattedText(builder, itemText)
        } else if (line.startsWith("> ")) {
            val quoteText = line.substring(2)
            builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = DesignTokens.TextSecondary.copy(alpha = 0.8f)))
            builder.append("“ $quoteText ”")
            builder.pop()
        } else if (line == "---") {
            builder.pushStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
            builder.append("────────────────────────────────")
            builder.pop()
        } else {
            appendInlineFormattedText(builder, line)
        }
        if (i < lines.size - 1) {
            builder.append("\n")
        }
    }
    return builder.toAnnotatedString()
}

private fun appendInlineFormattedText(builder: AnnotatedString.Builder, text: String) {
    var isBold = false
    var isItalic = false
    var isUnderline = false

    var i = 0
    while (i < text.length) {
        if (text.startsWith("**", i)) {
            isBold = !isBold
            i += 2
        } else if (text.startsWith("__", i)) {
            isUnderline = !isUnderline
            i += 2
        } else if (text.startsWith("*", i)) {
            isItalic = !isItalic
            i += 1
        } else {
            val weight = if (isBold) FontWeight.Bold else FontWeight.Normal
            val style = if (isItalic) FontStyle.Italic else FontStyle.Normal
            val dec = if (isUnderline) TextDecoration.Underline else TextDecoration.None

            builder.pushStyle(SpanStyle(fontWeight = weight, fontStyle = style, textDecoration = dec))
            builder.append(text[i].toString())
            builder.pop()
            i++
        }
    }
}
