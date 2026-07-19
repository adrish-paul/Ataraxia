package com.example.ataraxia.features.journal.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.MoodChip
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.util.StorageHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun JournalWritingView(
    onCancel: () -> Unit,
    onSave: (title: String, content: String, mood: String, weatherContext: String, isFavorite: Boolean, tags: String, imagePath: String, voicePath: String) -> Unit,
    showPhotoSourceDialog: Boolean,
    onShowPhotoSourceChanged: (Boolean) -> Unit,
    initialPrompt: String = "",
    todayMood: String = "",
    onMoodChanged: (String) -> Unit = {},
    initialTitle: String = "",
    initialContent: String = "",
    initialMood: String = "",
    initialTags: String = "",
    onSaveDraft: (title: String, content: String, mood: String, tags: String, prompt: String) -> Unit = { _, _, _, _, _ -> },
    onClearDraft: () -> Unit = {}
) {
    val context = LocalContext.current
    var entryTitle by remember { mutableStateOf(initialTitle) }
    var entryContentValue by remember { mutableStateOf(TextFieldValue(initialContent)) }
    var entryTags by remember { mutableStateOf(initialTags) }

    var selectedImageUris by remember { mutableStateOf(emptyList<Uri>()) }
    var recordedFiles by remember { mutableStateOf(emptyList<File>()) }
    var currentRecordingFile by remember { mutableStateOf<File?>(null) }
    var isRecordingAudio by remember { mutableStateOf(false) }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var showFormattingBar by remember { mutableStateOf(false) }
    var zoomImageIndex by remember { mutableStateOf<Int?>(null) }

    val moodMap = remember {
        mapOf(
            "🌸" to "Peaceful",
            "🌱" to "Grateful",
            "🌊" to "Calm",
            "✨" to "Hopeful",
            "☀️" to "Joyful",
            "🧸" to "Comforted",
            "🍃" to "Reflective",
            "🔥" to "Energetic",
            "🌙" to "Tired",
            "☁️" to "Anxious",
            "🤯" to "Stressed",
            "😢" to "Sad",
            "💢" to "Angry",
            "🌀" to "Overwhelmed",
            "👤" to "Lonely",
            "💤" to "Sleepy"
        )
    }

    var entryMood by remember {
        mutableStateOf(
            moodMap.entries.firstOrNull { it.value.lowercase() == initialMood.lowercase() }?.key 
                ?: moodMap.entries.firstOrNull { it.value.lowercase() == todayMood.lowercase() }?.key 
                ?: "🌸"
        )
    }

    // Auto-save draft every 5 seconds when title/content changes
    LaunchedEffect(entryTitle, entryContentValue.text, entryMood, entryTags) {
        delay(5000.milliseconds)
        if (entryTitle.isNotBlank() || entryContentValue.text.isNotBlank()) {
            onSaveDraft(entryTitle, entryContentValue.text, moodMap[entryMood] ?: "Peaceful", entryTags, initialPrompt)
        }
    }

    // Formatting insertion helper
    fun applyFormatting(prefix: String, suffix: String = "") {
        val text = entryContentValue.text
        val selection = entryContentValue.selection
        val selectedText = text.substring(selection.start, selection.end)
        val newText = text.substring(0, selection.start) + prefix + selectedText + suffix + text.substring(selection.end)
        val newSelectionStart = selection.start + prefix.length
        val newSelectionEnd = newSelectionStart + selectedText.length
        entryContentValue = entryContentValue.copy(
            text = newText,
            selection = TextRange(newSelectionStart, newSelectionEnd)
        )
    }

    // Block formatting insertion helper (lists, quotes, dividers)
    fun applyBlockFormatting(prefix: String) {
        val text = entryContentValue.text
        val selection = entryContentValue.selection
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        val diff = prefix.length
        entryContentValue = entryContentValue.copy(
            text = newText,
            selection = TextRange(selection.start + diff, selection.end + diff)
        )
    }

    fun getStylesAtCursor(text: String, cursorIndex: Int): Triple<Boolean, Boolean, Boolean> {
        var isBold = false
        var isItalic = false
        var isUnderline = false

        var i = 0
        val limit = cursorIndex.coerceAtMost(text.length)
        while (i < limit) {
            if (i + 2 <= limit && text.startsWith("**", i)) {
                isBold = !isBold
                i += 2
            } else if (i + 2 <= limit && text.startsWith("__", i)) {
                isUnderline = !isUnderline
                i += 2
            } else if (i + 1 <= limit && text.startsWith("*", i)) {
                isItalic = !isItalic
                i += 1
            } else {
                i++
            }
        }
        return Triple(isBold, isItalic, isUnderline)
    }

    fun toggleStyleAtCursor(currentText: String, selection: TextRange, styleToToggle: String): TextFieldValue {
        val cursor = selection.start
        val marker = when (styleToToggle) {
            "bold" -> "**"
            "italic" -> "*"
            "underline" -> "__"
            else -> ""
        }
        if (marker.isEmpty()) return TextFieldValue(currentText, selection)

        val (bold, italic, underline) = getStylesAtCursor(currentText, cursor)
        val isActive = when (styleToToggle) {
            "bold" -> bold
            "italic" -> italic
            "underline" -> underline
            else -> false
        }

        if (isActive) {
            // Toggle style OFF
            val hasMarkerBefore = cursor >= marker.length && currentText.substring(cursor - marker.length, cursor) == marker
            val hasMarkerAfter = cursor + marker.length <= currentText.length && currentText.substring(cursor, cursor + marker.length) == marker

            if (hasMarkerBefore && hasMarkerAfter) {
                // Remove the empty tags around the cursor (e.g. **|**)
                val newText = currentText.substring(0, cursor - marker.length) + currentText.substring(cursor + marker.length)
                val newCursor = cursor - marker.length
                return TextFieldValue(text = newText, selection = TextRange(newCursor))
            }

            if (hasMarkerAfter) {
                // Jump the cursor past the closing tag
                val newCursor = cursor + marker.length
                return TextFieldValue(text = currentText, selection = TextRange(newCursor))
            }

            // Otherwise, insert marker to close it at the current position
            val newText = currentText.substring(0, cursor) + marker + currentText.substring(cursor)
            val newCursor = cursor + marker.length
            return TextFieldValue(text = newText, selection = TextRange(newCursor))
        } else {
            // Toggle style ON
            // Insert opening and closing tags at cursor, placing cursor inside
            val newText = currentText.substring(0, cursor) + marker + marker + currentText.substring(cursor)
            val newCursor = cursor + marker.length
            return TextFieldValue(text = newText, selection = TextRange(newCursor))
        }
    }

    fun handleContentValueChange(
        newValue: TextFieldValue,
        oldValue: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit
    ) {
        val selectionStart = newValue.selection.start
        if (newValue.text.length == oldValue.text.length + 1 &&
            selectionStart > 0 &&
            newValue.text[selectionStart - 1] == '\n'
        ) {
            val textBeforeCursor = newValue.text.substring(0, selectionStart - 1)
            val lastNewLineIndex = textBeforeCursor.lastIndexOf('\n')
            val lastLineStart = if (lastNewLineIndex == -1) 0 else lastNewLineIndex + 1
            val lastLine = textBeforeCursor.substring(lastLineStart)

            if (lastLine.startsWith("- ")) {
                if (lastLine.trim() == "-") {
                    val newText = newValue.text.substring(0, lastLineStart) + newValue.text.substring(selectionStart)
                    onValueChange(
                        newValue.copy(
                            text = newText,
                            selection = TextRange(lastLineStart)
                        )
                    )
                    return
                } else {
                    val prefix = "- "
                    val newText = newValue.text.substring(0, selectionStart) + prefix + newValue.text.substring(selectionStart)
                    onValueChange(
                        newValue.copy(
                            text = newText,
                            selection = TextRange(selectionStart + prefix.length)
                        )
                    )
                    return
                }
            }

            val numberRegex = Regex("^(\\d+)\\.\\s")
            val matchResult = numberRegex.find(lastLine)
            if (matchResult != null) {
                val numStr = matchResult.groupValues[1]
                val prefixLen = matchResult.value.length
                if (lastLine.length == prefixLen) {
                    val newText = newValue.text.substring(0, lastLineStart) + newValue.text.substring(selectionStart)
                    onValueChange(
                        newValue.copy(
                            text = newText,
                            selection = TextRange(lastLineStart)
                        )
                    )
                    return
                } else {
                    val nextNum = numStr.toIntOrNull()?.let { it + 1 } ?: 1
                    val prefix = "$nextNum. "
                    val newText = newValue.text.substring(0, selectionStart) + prefix + newValue.text.substring(selectionStart)
                    onValueChange(
                        newValue.copy(
                            text = newText,
                            selection = TextRange(selectionStart + prefix.length)
                        )
                    )
                    return
                }
            }
        }
        onValueChange(newValue)
    }

    // Permission and image setups
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { selectedImageUris = selectedImageUris + it }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = selectedImageUris + uris
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri = createPhotoUri(context)
            photoUri?.let { cameraLauncher.launch(it) }
        }
    }

    fun startRecording() {
        try {
            val audioFile = File(context.filesDir, "audio_${System.currentTimeMillis()}.mp3")
            currentRecordingFile = audioFile
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            isRecordingAudio = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val requestAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            currentRecordingFile?.let { recordedFiles = recordedFiles + it }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            currentRecordingFile = null
            isRecordingAudio = false
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    fun startPlayback(path: String, onFinished: () -> Unit) {
        stopPlayback()
        try {
            val file = File(path)
            if (!file.exists()) return
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    onFinished()
                    release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaPlayer?.release()
        }
    }

    val compositionScroll = rememberScrollState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(horizontal = AtaraxiaTheme.spacing.Space16)
            .verticalScroll(compositionScroll)
            .padding(vertical = AtaraxiaTheme.spacing.Space8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AtaraxiaTheme.spacing.Space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DesignTokens.CardBackground.copy(alpha = 0.8f))
                    .clickable {
                        recordedFiles.forEach { StorageHelper.deleteFile(it.absolutePath) }
                        currentRecordingFile?.let { StorageHelper.deleteFile(it.absolutePath) }
                        onClearDraft()
                        onCancel()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = DesignTokens.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "New Reflection",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary
                )
                if (initialPrompt.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = initialPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DesignTokens.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

        val currentStyles = remember(entryContentValue.text, entryContentValue.selection) {
            getStylesAtCursor(entryContentValue.text, entryContentValue.selection.start)
        }
        val isBold = currentStyles.first
        val isItalic = currentStyles.second
        val isUnderline = currentStyles.third

        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                PrimaryTextField(
                    value = entryTitle,
                    onValueChange = { entryTitle = it },
                    placeholder = "Give it a title (optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                if (showFormattingBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (entryContentValue.selection.start != entryContentValue.selection.end) {
                                    applyFormatting("**", "**")
                                } else {
                                    entryContentValue = toggleStyleAtCursor(entryContentValue.text, entryContentValue.selection, "bold")
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isBold) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FormatBold,
                                contentDescription = "Bold",
                                modifier = Modifier.size(18.dp),
                                tint = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                if (entryContentValue.selection.start != entryContentValue.selection.end) {
                                    applyFormatting("*", "*")
                                } else {
                                    entryContentValue = toggleStyleAtCursor(entryContentValue.text, entryContentValue.selection, "italic")
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isItalic) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FormatItalic,
                                contentDescription = "Italic",
                                modifier = Modifier.size(18.dp),
                                tint = if (isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                if (entryContentValue.selection.start != entryContentValue.selection.end) {
                                    applyFormatting("__", "__")
                                } else {
                                    entryContentValue = toggleStyleAtCursor(entryContentValue.text, entryContentValue.selection, "underline")
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isUnderline) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FormatUnderlined,
                                contentDescription = "Underline",
                                modifier = Modifier.size(18.dp),
                                tint = if (isUnderline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { applyBlockFormatting("- ") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.FormatListBulleted, contentDescription = "Bullet List", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { applyBlockFormatting("1. ") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.FormatListNumbered, contentDescription = "Numbered List", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { applyBlockFormatting("> ") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.FormatQuote, contentDescription = "Quote Block", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { applyBlockFormatting("\n---\n") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.HorizontalRule, contentDescription = "Divider", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Improved Typography Writing Text Field (increased line-height/size)
                PrimaryTextField(
                    value = entryContentValue,
                    onValueChange = { newValue ->
                        handleContentValueChange(newValue, entryContentValue) {
                            entryContentValue = it
                        }
                    },
                    placeholder = "Write your thoughts down. The screen is yours...",
                    singleLine = false,
                    maxLines = 15,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onShowPhotoSourceChanged(true) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Attach Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isRecordingAudio) {
                                    stopRecording()
                                } else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        startRecording()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isRecordingAudio) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = if (isRecordingAudio) "Stop Recording" else "Record Audio",
                                tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showFormattingBar = !showFormattingBar },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (showFormattingBar) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFormat,
                                contentDescription = "Formatting options",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    val wordCount = remember(entryContentValue.text) {
                        entryContentValue.text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    }
                    Text(
                        text = "$wordCount words",
                        style = MaterialTheme.typography.labelMedium,
                        color = DesignTokens.TextSecondary
                    )
                }

                if (isRecordingAudio) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LunafloraCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Recording Voice note...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Tap mic to finish",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                    Text(
                        text = "Reflective Tags (comma-separated)",
                        style = MaterialTheme.typography.labelLarge,
                        color = DesignTokens.TextSecondary
                    )
                    PrimaryTextField(
                        value = entryTags,
                        onValueChange = { entryTags = it },
                        placeholder = "e.g. Dreams, College, Gratitude",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Horizontal Attached Photos cards
                if (selectedImageUris.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedImageUris.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 110.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { zoomImageIndex = index }
                            ) {
                                val bitmapState = remember(uri) { mutableStateOf<ImageBitmap?>(null) }
                                LaunchedEffect(uri) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                                bitmapState.value = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                bitmapState.value?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Attachment preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { selectedImageUris = selectedImageUris.filter { it != uri } },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Refined voice attachments list
                if (!isRecordingAudio && recordedFiles.isNotEmpty()) {
                    var playingPreviewPath by remember { mutableStateOf<String?>(null) }
                    var playbackProgress by remember { mutableStateOf(0f) }

                    // Polling playback progress values
                    LaunchedEffect(playingPreviewPath) {
                        if (playingPreviewPath != null) {
                            while (mediaPlayer != null) {
                                try {
                                    val current = mediaPlayer?.currentPosition ?: 0
                                    val duration = mediaPlayer?.duration ?: 1
                                    playbackProgress = current.toFloat() / duration.toFloat()
                                } catch (_: Exception) {
                                    // ignore
                                }
                                delay(200.milliseconds)
                            }
                        } else {
                            playbackProgress = 0f
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recordedFiles.forEachIndexed { index, audioFile ->
                            val isPlayingThis = playingPreviewPath == audioFile.absolutePath
                            LunafloraCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🎙️", style = MaterialTheme.typography.titleLarge)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Voice Note ${index + 1}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = DesignTokens.TextPrimary
                                            )
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                if (isPlayingThis) {
                                                    stopPlayback()
                                                    playingPreviewPath = null
                                                } else {
                                                    startPlayback(audioFile.absolutePath) {
                                                        playingPreviewPath = null
                                                    }
                                                    playingPreviewPath = audioFile.absolutePath
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = if (isPlayingThis) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = {
                                                if (isPlayingThis) {
                                                    stopPlayback()
                                                    playingPreviewPath = null
                                                }
                                                StorageHelper.deleteFile(audioFile.absolutePath)
                                                recordedFiles = recordedFiles.filter { it != audioFile }
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    // Display audio playback progress cleanly
                                    if (isPlayingThis) {
                                        LinearProgressIndicator(
                                            progress = { playbackProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                    Text(
                        text = "Mood Context",
                        style = MaterialTheme.typography.labelLarge,
                        color = DesignTokens.TextSecondary
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val cardWidth = maxWidth
                        val spacing = 8.dp
                        val chipWidth = (cardWidth - (spacing * 2)) / 2.5f

                        val listMoods = listOf(
                            "🌸" to "Peaceful",
                            "🌱" to "Grateful",
                            "🌊" to "Calm",
                            "✨" to "Hopeful",
                            "☀️" to "Joyful",
                            "🧸" to "Comforted",
                            "🍃" to "Reflective",
                            "🔥" to "Energetic",
                            "🌙" to "Tired",
                            "☁️" to "Anxious",
                            "🤯" to "Stressed",
                            "😢" to "Sad",
                            "💢" to "Angry",
                            "🌀" to "Overwhelmed",
                            "👤" to "Lonely",
                            "💤" to "Sleepy"
                        )

                        val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = 10000 - (10000 % listMoods.size))

                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(108.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            items(20000) { globalIndex ->
                                val index = globalIndex % listMoods.size
                                val (emoji, label) = listMoods[index]
                                MoodChip(
                                    moodEmoji = emoji,
                                    moodLabel = label,
                                    isSelected = entryMood == emoji,
                                    modifier = Modifier.width(chipWidth),
                                    onClick = {
                                        entryMood = emoji
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AtaraxiaSecondaryButton(
                    text = "Discard",
                    onClick = {
                        recordedFiles.forEach { StorageHelper.deleteFile(it.absolutePath) }
                        currentRecordingFile?.let { StorageHelper.deleteFile(it.absolutePath) }
                        onClearDraft()
                        onCancel()
                    }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AtaraxiaPrimaryButton(
                    text = "Save",
                    onClick = {
                        val copiedPaths = selectedImageUris.map { uri ->
                            if (uri.scheme == "file" && uri.path?.contains("journal_images") == true) {
                                uri.path ?: ""
                            } else {
                                StorageHelper.saveImageToInternalStorage(context, uri)
                            }
                        }.filter { it.isNotEmpty() }
                        val imagesStr = copiedPaths.joinToString(",")
                        val voiceStr = recordedFiles.joinToString(",") { it.absolutePath }
                        onSave(
                            entryTitle,
                            entryContentValue.text,
                            moodMap[entryMood] ?: "Peaceful",
                            "",
                            false,
                            entryTags,
                            imagesStr,
                            voiceStr
                        )
                    }
                )
            }
        }
    }

    if (showPhotoSourceDialog) {
        Dialog(onDismissRequest = { onShowPhotoSourceChanged(false) }) {
            LunafloraCard(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AtaraxiaTheme.spacing.Space8),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "Attach Photo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LunafloraCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onShowPhotoSourceChanged(false)
                                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        photoUri = createPhotoUri(context)
                                        photoUri?.let { cameraLauncher.launch(it) }
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📸", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Take Photo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Use your camera to snap a picture.", style = MaterialTheme.typography.bodySmall, color = DesignTokens.TextSecondary)
                                }
                            }
                        }

                        LunafloraCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onShowPhotoSourceChanged(false)
                                    galleryLauncher.launch("image/*")
                                },
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🖼️", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Choose from Gallery", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Select one or more photos from your device.", style = MaterialTheme.typography.bodySmall, color = DesignTokens.TextSecondary)
                                }
                            }
                        }
                    }

                    AtaraxiaSecondaryButton(
                        text = "Cancel",
                        onClick = { onShowPhotoSourceChanged(false) }
                    )
                }
            }
        }
    }

    // Modal Full-Screen Swipeable and Pinch-to-Zoom Picture Dialog
    zoomImageIndex?.let { startIndex ->
        Dialog(
            onDismissRequest = { zoomImageIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { selectedImageUris.size })
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val pageUri = selectedImageUris[pageIndex]
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    val bitmapState = remember(pageUri) { mutableStateOf<ImageBitmap?>(null) }
                    LaunchedEffect(pageUri) {
                        withContext(Dispatchers.IO) {
                            try {
                                context.contentResolver.openInputStream(pageUri)?.use { stream ->
                                    bitmapState.value = BitmapFactory.decodeStream(stream)?.asImageBitmap()
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

                // Close Button
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

private fun createPhotoUri(context: android.content.Context): Uri? {
    return try {
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
        null
    }
}
