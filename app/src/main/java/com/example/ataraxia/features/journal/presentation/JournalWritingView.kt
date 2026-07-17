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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import java.io.File

@Composable
fun JournalWritingView(
    onCancel: () -> Unit,
    onSave: (title: String, content: String, mood: String, weatherContext: String, isFavorite: Boolean, tags: String, imagePath: String, voicePath: String) -> Unit,
    showPhotoSourceDialog: Boolean,
    onShowPhotoSourceChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var entryMood by remember { mutableStateOf("🌸") }
    var entryTags by remember { mutableStateOf("") }

    var selectedImageUris by remember { mutableStateOf(emptyList<Uri>()) }
    var recordedFiles by remember { mutableStateOf(emptyList<File>()) }
    var currentRecordingFile by remember { mutableStateOf<File?>(null) }
    var isRecordingAudio by remember { mutableStateOf(false) }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val moodMap = remember {
        mapOf(
            "🌸" to "Peaceful",
            "🌱" to "Grateful",
            "🌙" to "Tired",
            "☁️" to "Anxious",
            "🔥" to "Energetic",
            "🍃" to "Reflective",
            "🧸" to "Comforted",
            "🌊" to "Calm"
        )
    }

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUris = selectedImageUris + it
        }
    }

    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            selectedImageUris = selectedImageUris + cameraPhotoUri!!
        }
    }

    fun createCameraPhotoUri(): Uri {
        val directory = File(context.cacheDir, "camera_photos").apply {
            if (!exists()) mkdirs()
        }
        val file = File(directory, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "com.example.ataraxia.fileprovider", file)
        cameraPhotoUri = uri
        return uri
    }

    fun startRecording() {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val file = StorageHelper.getVoiceRecordFile(context)
            currentRecordingFile = file
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecordingAudio = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.let { recorder ->
                try {
                    recorder.stop()
                    currentRecordingFile?.let {
                        recordedFiles = recordedFiles + it
                    }
                } catch (stopEx: RuntimeException) {
                    stopEx.printStackTrace()
                    currentRecordingFile?.let { StorageHelper.deleteFile(it.absolutePath) }
                }
                recorder.release()
            }
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
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AtaraxiaTheme.spacing.Space24)
            .verticalScroll(compositionScroll)
            .padding(vertical = AtaraxiaTheme.spacing.Space16)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AtaraxiaTheme.spacing.Space16),
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
            Text(
                text = "New Reflection",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = DesignTokens.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                PrimaryTextField(
                    value = entryTitle,
                    onValueChange = { entryTitle = it },
                    placeholder = "Give it a title (optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                PrimaryTextField(
                    value = entryContent,
                    onValueChange = { entryContent = it },
                    placeholder = "Write your thoughts down. The screen is yours...",
                    singleLine = false,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

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

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onShowPhotoSourceChanged(true) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Attach Photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isRecordingAudio) {
                                stopRecording()
                            } else {
                                startRecording()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isRecordingAudio) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = if (isRecordingAudio) "Stop Recording" else "Record Audio",
                            tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (selectedImageUris.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedImageUris.forEach { uri ->
                            Box(
                                modifier = Modifier
                                    .size(width = 120.dp, height = 120.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            ) {
                                val bitmapState = remember(uri) { mutableStateOf<ImageBitmap?>(null) }
                                LaunchedEffect(uri) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                                val decoded = BitmapFactory.decodeStream(stream)?.asImageBitmap()
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
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isRecordingAudio) {
                    LunafloraCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Recording Voice note...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Tap mic to finish",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignTokens.TextSecondary
                            )
                        }
                    }
                }

                if (!isRecordingAudio && recordedFiles.isNotEmpty()) {
                    var playingPreviewPath by remember { mutableStateOf<String?>(null) }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recordedFiles.forEachIndexed { index, audioFile ->
                            val isPlayingThis = playingPreviewPath == audioFile.absolutePath
                            LunafloraCard(modifier = Modifier.fillMaxWidth()) {
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
                                            style = MaterialTheme.typography.bodyLarge,
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
                                                contentDescription = "Play recording",
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
                                            Icon(Icons.Default.Close, contentDescription = "Delete recording", tint = MaterialTheme.colorScheme.error)
                                        }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
                    ) {
                        val listMoods = listOf(
                            "🌸" to "Peaceful",
                            "🌱" to "Grateful",
                            "🌙" to "Tired",
                            "☁️" to "Anxious",
                            "🔥" to "Energetic",
                            "🍃" to "Reflective",
                            "🧸" to "Comforted",
                            "🌊" to "Calm"
                        )
                        listMoods.forEach { (emoji, label) ->
                            MoodChip(
                                moodEmoji = emoji,
                                moodLabel = label,
                                isSelected = entryMood == emoji,
                                onClick = { entryMood = emoji }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaSecondaryButton(
                            text = "Cancel",
                            onClick = {
                                recordedFiles.forEach { StorageHelper.deleteFile(it.absolutePath) }
                                currentRecordingFile?.let { StorageHelper.deleteFile(it.absolutePath) }
                                onCancel()
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaPrimaryButton(
                            text = "Save",
                            onClick = {
                                if (entryContent.isNotBlank() || recordedFiles.isNotEmpty() || selectedImageUris.isNotEmpty()) {
                                    val savedImagePath = selectedImageUris.map { uri ->
                                        StorageHelper.saveImageToInternalStorage(context, uri)
                                    }.filter { it.isNotEmpty() }.joinToString(",")
                                    val voicePath = recordedFiles.map { it.absolutePath }.filter { it.isNotEmpty() }.joinToString(",")
                                    val mappedLabel = moodMap[entryMood] ?: "Reflective"
                                    onSave(
                                        entryTitle,
                                        entryContent,
                                        entryMood,
                                        mappedLabel,
                                        false,
                                        entryTags,
                                        savedImagePath,
                                        voicePath
                                    )
                                }
                            },
                            enabled = entryContent.isNotBlank() || recordedFiles.isNotEmpty() || selectedImageUris.isNotEmpty()
                        )
                    }
                }
            }
        }

        if (showPhotoSourceDialog) {
            Dialog(onDismissRequest = { onShowPhotoSourceChanged(false) }) {
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
                LunafloraCard(modifier = Modifier.fillMaxWidth().padding(horizontal = AtaraxiaTheme.spacing.Space16)) {
                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Add Photo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                            AtaraxiaPrimaryButton(
                                text = "📸 Take Photo (Camera)",
                                onClick = {
                                    onShowPhotoSourceChanged(false)
                                    try {
                                        val uri = createCameraPhotoUri()
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            )
                            AtaraxiaSecondaryButton(
                                text = "📁 Choose from Gallery",
                                onClick = {
                                    onShowPhotoSourceChanged(false)
                                    photoLauncher.launch("image/*")
                                }
                            )
                            AtaraxiaSecondaryButton(
                                text = "Cancel",
                                onClick = { onShowPhotoSourceChanged(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
