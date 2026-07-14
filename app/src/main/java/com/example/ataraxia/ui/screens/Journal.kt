package com.example.ataraxia.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.CalendarMonth
import com.example.ataraxia.ui.components.EmptyState
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.JournalCard
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.MoodChip
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.ReflectionCard
import com.example.ataraxia.ui.components.SectionHeader
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.util.StorageHelper
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    name: String,
    profileImage: String,
    entries: List<JournalEntryEntity>,
    onAddEntry: (title: String, content: String, mood: String, weatherContext: String, isFavorite: Boolean, tags: String, imagePath: String, voicePath: String) -> Unit,
    onToggleFavorite: (JournalEntryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onWritingModeChanged: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    var isWritingMode by remember { mutableStateOf(false) }
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var entryMood by remember { mutableStateOf("🌸") }
    var entryTags by remember { mutableStateOf("") }

    // Media attachment states during composition
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    // Media player and recorder references
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Screen display configurations
    var isFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<JournalEntryEntity?>(null) }

    // Month Navigation states
    val calendarInstance = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendarInstance.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH)) }

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

    // Request permissions launcher for audio recorder
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

    // Photo picker launcher
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    // Detail popup voice note player state
    var isPlayingDetailVoice by remember { mutableStateOf(false) }
    var detailMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Clean up recorder/player resources
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaPlayer?.release()
            detailMediaPlayer?.release()
        }
    }

    // Notify NavGraph when writing mode updates
    LaunchedEffect(isWritingMode) {
        onWritingModeChanged(isWritingMode)
    }

    BackHandler(enabled = isWritingMode) {
        isWritingMode = false
        entryTitle = ""
        entryContent = ""
        entryTags = ""
        selectedImageUri = null
        recordedFile = null
    }

    fun startRecording() {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val file = StorageHelper.getVoiceRecordFile(context)
            recordedFile = file
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
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
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecordingAudio = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startPlayback(path: String, onFinished: () -> Unit) {
        try {
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

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DesignTokens.AppBackground)) {
        if (!isWritingMode) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .verticalScroll(scrollState)
            ) {
                // Top Bar with Search Toggle
                if (isSearching) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AtaraxiaTheme.spacing.Space12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search reflections...",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                        IconButton(onClick = { 
                            searchQuery = ""
                            isSearching = false 
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Search",
                                tint = DesignTokens.TextPrimary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AtaraxiaTheme.spacing.Space16),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = "Ataraxia Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                            Text(
                                text = "Journal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = DesignTokens.TextPrimary
                            )
                        }
                        IconButton(onClick = { isSearching = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Reflections",
                                tint = DesignTokens.TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                // Reflection Card (Remembered so it does not rotate on recompositions during this screen lifecycle)
                val reflectionPrompt = remember { com.example.ataraxia.data.local.DailyPrompts.getTodayPrompt() }
                ReflectionCard(
                    prompt = reflectionPrompt,
                    onBeginWriting = { isWritingMode = true }
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                // Category Filter Row with Calendar Popup Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (!isFavoritesOnly) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { isFavoritesOnly = false }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("All Notes", color = if (!isFavoritesOnly) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isFavoritesOnly) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { isFavoritesOnly = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("⭐ Favorites", color = if (isFavoritesOnly) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = { showCalendarDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "View Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Popup Calendar Dialog
                if (showCalendarDialog) {
                    Dialog(onDismissRequest = { showCalendarDialog = false }) {
                        LunafloraCard(modifier = Modifier.fillMaxWidth().padding(horizontal = AtaraxiaTheme.spacing.Space8)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        selectedDayNum = null
                                        if (currentMonth == 0) {
                                            currentMonth = 11
                                            currentYear--
                                        } else {
                                            currentMonth--
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowLeft,
                                            contentDescription = "Prev Month",
                                            tint = DesignTokens.TextPrimary
                                        )
                                    }

                                    val monthName = DateFormatSymbols().months[currentMonth]
                                    Text(
                                        text = "$monthName $currentYear",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = DesignTokens.TextPrimary
                                    )

                                    IconButton(onClick = {
                                        selectedDayNum = null
                                        if (currentMonth == 11) {
                                            currentMonth = 0
                                            currentYear++
                                        } else {
                                            currentMonth++
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowRight,
                                            contentDescription = "Next Month",
                                            tint = DesignTokens.TextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Day of the week labels
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                                    daysOfWeek.forEach { day ->
                                        Text(
                                            text = day,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = DesignTokens.TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Calculate month cells grid
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentYear)
                                    set(Calendar.MONTH, currentMonth)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                                val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                                val cells = mutableListOf<Int?>()
                                for (i in 1 until startDayOfWeek) {
                                    cells.add(null)
                                }
                                for (i in 1..daysInMonth) {
                                    cells.add(i)
                                }

                                val rows = cells.chunked(7)
                                rows.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        row.forEach { dayNumber ->
                                            if (dayNumber == null) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            } else {
                                                // Find journal entries on this day
                                                val dayEntries = entries.filter { entry ->
                                                    val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                                                    entryCal.get(Calendar.YEAR) == currentYear &&
                                                    entryCal.get(Calendar.MONTH) == currentMonth &&
                                                    entryCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                                }

                                                val isSelected = selectedDayNum == dayNumber
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                            } else if (dayEntries.isNotEmpty()) {
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                            } else {
                                                                Color.Transparent
                                                            }
                                                        )
                                                        .then(
                                                            if (isSelected) Modifier.border(
                                                                width = 1.5.dp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = RoundedCornerShape(8.dp)
                                                            ) else Modifier
                                                        )
                                                        .clickable {
                                                            selectedDayNum = if (selectedDayNum == dayNumber) null else dayNumber
                                                            showCalendarDialog = false
                                                        }
                                                        .padding(vertical = 6.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = dayNumber.toString(),
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                                        color = if (isSelected || dayEntries.isNotEmpty()) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                                    )
                                                    if (dayEntries.isNotEmpty()) {
                                                        Text(
                                                            text = "🌸",
                                                            fontSize = 10.sp
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                    }
                                                }
                                            }
                                        }
                                        // Pad incomplete rows
                                        if (row.size < 7) {
                                            for (j in 0 until (7 - row.size)) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                // Filter & Search Logic
                val filteredEntries = remember(entries, searchQuery, isFavoritesOnly) {
                    entries.filter { entry ->
                        val dateString = formatTimestamp(entry.timestamp)
                        val matchesQuery = searchQuery.isBlank() || 
                                entry.title.contains(searchQuery, ignoreCase = true) ||
                                entry.content.contains(searchQuery, ignoreCase = true) ||
                                entry.mood.contains(searchQuery, ignoreCase = true) ||
                                entry.tags.contains(searchQuery, ignoreCase = true) ||
                                dateString.contains(searchQuery, ignoreCase = true)
                        val matchesFavorite = !isFavoritesOnly || entry.isFavorite
                        matchesQuery && matchesFavorite
                    }
                }

                val displayedEntries = remember(filteredEntries, selectedDayNum, currentMonth, currentYear) {
                    if (selectedDayNum == null) {
                        filteredEntries
                    } else {
                        filteredEntries.filter { entry ->
                            val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                            entryCal.get(Calendar.YEAR) == currentYear &&
                            entryCal.get(Calendar.MONTH) == currentMonth &&
                            entryCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val monthName = DateFormatSymbols().months[currentMonth]
                    Column {
                        Text(
                            text = if (selectedDayNum != null) "Reflections: $monthName $selectedDayNum" else if (isFavoritesOnly) "Starred Reflections" else "Recent Reflections",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )
                        Text(
                            text = if (selectedDayNum != null) "Reflections on selected calendar day" else "Your story told in quiet notes.",
                            style = MaterialTheme.typography.labelMedium,
                            color = DesignTokens.TextSecondary
                        )
                    }
                    if (selectedDayNum != null) {
                        Text(
                            text = "Show All",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { selectedDayNum = null }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                if (displayedEntries.isEmpty()) {
                    EmptyState(
                        illustration = {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        },
                        title = if (selectedDayNum != null) "No reflections today" else "Your journal is empty",
                        subtitle = if (selectedDayNum != null) "Take a moment to write down a quiet reflection for this day." else "Write your first story or check in with yourself.",
                        actionLabel = if (selectedDayNum != null) "Begin Writing" else null,
                        onActionClick = if (selectedDayNum != null) { { isWritingMode = true } } else null
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                    ) {
                        displayedEntries.forEach { entry ->
                            val dateString = formatTimestamp(entry.timestamp)
                            val moodLabel = moodMap[entry.mood] ?: "Reflective"
                            JournalCard(
                                title = entry.title,
                                snippet = entry.content,
                                date = dateString,
                                mood = entry.mood,
                                metadataContext = moodLabel + (if (entry.isFavorite) " ⭐" else ""),
                                onClick = { selectedEntryForDetail = entry }
                            )
                        }
                    }
                }

                // Floating nav bar layout spacer
                Spacer(modifier = Modifier.height(130.dp))
            }
        } else {
            // Immersive Composition Layout
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        isWritingMode = false
                        entryTitle = ""
                        entryContent = ""
                        entryTags = ""
                        selectedImageUri = null
                        recordedFile = null
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = DesignTokens.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                    Text(
                        text = "New Reflection",
                        style = MaterialTheme.typography.headlineLarge,
                        color = DesignTokens.TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                PrimaryTextField(
                    value = entryTitle,
                    onValueChange = { entryTitle = it },
                    placeholder = "Give it a title (optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

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

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                // Custom Tag Fields
                Text(
                    text = "Reflective Tags (comma-separated)",
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                PrimaryTextField(
                    value = entryTags,
                    onValueChange = { entryTags = it },
                    placeholder = "e.g. Dreams, College, Gratitude",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                // Media Attachment Section
                Text(
                    text = "Local Offline Attachments",
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Attach Photo",
                            tint = MaterialTheme.colorScheme.primary
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
                            .size(50.dp)
                            .background(
                                if (isRecordingAudio) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = if (isRecordingAudio) "Stop Recording" else "Record Audio",
                            tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Previews of Image Attachment
                selectedImageUri?.let { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(MaterialTheme.shapes.medium)
                    ) {
                        val bitmap = remember(uri) {
                            try {
                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                    BitmapFactory.decodeStream(stream)
                                }?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Attachment preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Previews of Audio Attachment
                recordedFile?.let { audioFile ->
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
                                    text = "Voice Recording",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = DesignTokens.TextPrimary
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    if (isPlayingPreview) {
                                        stopPlayback()
                                        isPlayingPreview = false
                                    } else {
                                        startPlayback(audioFile.absolutePath) {
                                            isPlayingPreview = false
                                        }
                                        isPlayingPreview = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isPlayingPreview) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                        contentDescription = "Play recording",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = {
                                    if (isPlayingPreview) {
                                        stopPlayback()
                                        isPlayingPreview = false
                                    }
                                    StorageHelper.deleteFile(audioFile.absolutePath)
                                    recordedFile = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete recording", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mood Context",
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                
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

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaSecondaryButton(
                            text = "Cancel",
                            onClick = {
                                isWritingMode = false
                                entryTitle = ""
                                entryContent = ""
                                entryTags = ""
                                selectedImageUri = null
                                recordedFile = null
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaPrimaryButton(
                            text = "Save",
                            onClick = {
                                if (entryContent.isNotBlank()) {
                                    // Save photo file internally
                                    val savedImagePath = if (selectedImageUri != null) {
                                        StorageHelper.saveImageToInternalStorage(context, selectedImageUri!!)
                                    } else {
                                        ""
                                    }

                                    // Voice recording is already saved at recordedFile absolute path
                                    val voicePath = recordedFile?.absolutePath ?: ""

                                    val mappedLabel = moodMap[entryMood] ?: "Reflective"
                                    onAddEntry(
                                        entryTitle,
                                        entryContent,
                                        entryMood,
                                        mappedLabel,
                                        false, // Not favorite initially
                                        entryTags,
                                        savedImagePath,
                                        voicePath
                                    )
                                    isWritingMode = false
                                    entryTitle = ""
                                    entryContent = ""
                                    entryTags = ""
                                    selectedImageUri = null
                                    recordedFile = null
                                }
                            },
                            enabled = entryContent.isNotBlank()
                        )
                    }
                }
            }
        }

        // Selected Reflection detail Popup dialog
        selectedEntryForDetail?.let { entry ->
            Dialog(onDismissRequest = {
                // stop playback on dismiss
                detailMediaPlayer?.release()
                detailMediaPlayer = null
                isPlayingDetailVoice = false
                selectedEntryForDetail = null
            }) {
                LunafloraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                ) {
                    Column(
                        modifier = Modifier.padding(AtaraxiaTheme.spacing.Space16)
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
                                    selectedEntryForDetail = entry.copy(isFavorite = !entry.isFavorite)
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
                                isPlayingDetailVoice = false
                                selectedEntryForDetail = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = DesignTokens.TextSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                        
                        Text(
                            text = entry.title.ifBlank { "Untitled Reflection" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                        
                        Text(
                            text = formatTimestamp(entry.timestamp) + if (entry.weatherContext.isNotBlank()) " • ${entry.weatherContext}" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Render Tags if any
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

                        // Show photo if available
                        if (entry.imagePath.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            ) {
                                val bitmap = remember(entry.imagePath) {
                                    try {
                                        val file = File(entry.imagePath)
                                        if (file.exists()) {
                                            BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
                                        } else null
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Attachment Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Text("Image not found", color = DesignTokens.TextSecondary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Playback voice note if available
                        if (entry.voicePath.isNotEmpty()) {
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
                                            text = "Voice Reflection",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DesignTokens.TextPrimary
                                        )
                                    }
                                    IconButton(onClick = {
                                        if (isPlayingDetailVoice) {
                                            detailMediaPlayer?.stop()
                                            detailMediaPlayer?.release()
                                            detailMediaPlayer = null
                                            isPlayingDetailVoice = false
                                        } else {
                                            try {
                                                detailMediaPlayer = MediaPlayer().apply {
                                                    setDataSource(entry.voicePath)
                                                    prepare()
                                                    start()
                                                    setOnCompletionListener {
                                                        isPlayingDetailVoice = false
                                                        release()
                                                        detailMediaPlayer = null
                                                    }
                                                }
                                                isPlayingDetailVoice = true
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isPlayingDetailVoice) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                            contentDescription = "Playback recording",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val detailScrollState = rememberScrollState()
                            Text(
                                text = entry.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = DesignTokens.TextSecondary,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(detailScrollState)
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
                                        // Delete files if any
                                        StorageHelper.deleteFile(entry.imagePath)
                                        StorageHelper.deleteFile(entry.voicePath)
                                        onDeleteEntry(entry.id)
                                        selectedEntryForDetail = null
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AtaraxiaPrimaryButton(
                                    text = "Close",
                                    onClick = {
                                        detailMediaPlayer?.release()
                                        detailMediaPlayer = null
                                        isPlayingDetailVoice = false
                                        selectedEntryForDetail = null
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

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
