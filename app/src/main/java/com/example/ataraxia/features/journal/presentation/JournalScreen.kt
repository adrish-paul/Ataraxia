package com.example.ataraxia.features.journal.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.ataraxia.data.local.DailyPrompts
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.JournalCard
import com.example.ataraxia.ui.components.ScreenEnclosure
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ReflectionCard
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.verticalScroll
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Writing Template Definition
data class WritingTemplate(
    val name: String,
    val description: String,
    val titleTemplate: String,
    val contentTemplate: String,
    val defaultTags: String
)

val writingTemplates = listOf(
    WritingTemplate("Free Writing", "Clear canvas for spontaneous flow.", "", "", "Thoughts,Freeflow"),
    WritingTemplate("Morning Reflection", "Start your day with intent and clarity.", "Morning Clarity", "• Focus for today:\n• Looking forward to:\n• Current energy level (1-10):", "Morning,Intent"),
    WritingTemplate("Evening Reflection", "Unwind and reflect on your accomplishments.", "Evening Calm", "• Wins for today:\n• Things to let go of:\n• What brought me peace today:", "Evening,Reflect"),
    WritingTemplate("Gratitude Journal", "Cultivate appreciation for the little things.", "Today I am Grateful", "• 3 things I am grateful for today:\n  1. \n  2. \n  3. \n• A kind gesture I received or did:", "Gratitude,Joy"),
    WritingTemplate("Dream Journal", "Record and explore your subconscious.", "Dream Log", "• Dream narrative:\n• Feelings upon waking:\n• Colors/Symbols noticed:", "Subconscious,Dreams"),
    WritingTemplate("Weekly Reflection", "Review patterns and set paths for next week.", "Weekly Review", "• Main theme of the week:\n• Lessons learned:\n• Areas to improve next week:", "Weekly,Review")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onWritingModeChanged: (Boolean) -> Unit,
    initialPrompt: String = "",
    @Suppress("UNUSED_PARAMETER") scrollToTopKey: Int = 0,
    todayMood: String = "",
    onSaveTodayMood: (String) -> Unit = {}
) {
    val entries by viewModel.allEntries.collectAsState()
    val draft by viewModel.journalDraft.collectAsState()
    val insights by viewModel.insightsFlow.collectAsState()

    var isWritingMode by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<WritingTemplate?>(null) }

    var draftTitleToPreFill by remember { mutableStateOf("") }
    var draftContentToPreFill by remember { mutableStateOf("") }
    var draftMoodToPreFill by remember { mutableStateOf("") }
    var draftTagsToPreFill by remember { mutableStateOf("") }
    var draftPromptToPreFill by remember { mutableStateOf("") }

    LaunchedEffect(initialPrompt) {
        if (initialPrompt.isNotEmpty()) {
            isWritingMode = true
        }
    }

    // Intercept hardware back button when in writing editor mode
    BackHandler(enabled = isWritingMode) {
        isWritingMode = false
        selectedTemplate = null
        draftTitleToPreFill = ""
        draftContentToPreFill = ""
        draftMoodToPreFill = ""
        draftTagsToPreFill = ""
        draftPromptToPreFill = ""
        onWritingModeChanged(false)
    }

    var isFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<JournalEntryEntity?>(null) }

    val searchFocusRequester = remember { FocusRequester() }

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

    LaunchedEffect(isSearching) {
        if (isSearching) {
            delay(100.milliseconds) // slight delay to allow layout to settle
            try {
                searchFocusRequester.requestFocus()
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    // Advanced sorting states
    var activeSortMode by remember { mutableStateOf("Newest") } // "Newest", "Oldest", "RecentlyEdited"
    // Advanced filtering state
    var selectedMoodFilter by remember { mutableStateOf<String?>(null) }

    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }

    val calendarInstance = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendarInstance.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH)) }

    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isWritingMode) {
        onWritingModeChanged(isWritingMode)
    }

    // Perform sorting and filtering
    val filteredEntries = remember(entries, searchQuery, isFavoritesOnly, selectedDayNum, currentMonth, currentYear, activeSortMode, selectedMoodFilter) {
        val list = entries.filter { entry ->
            val moodLabel = moodMap[entry.mood] ?: entry.mood
            val matchesQuery = searchQuery.isBlank() ||
                entry.title.contains(searchQuery, ignoreCase = true) ||
                entry.content.contains(searchQuery, ignoreCase = true) ||
                entry.tags.contains(searchQuery, ignoreCase = true) ||
                entry.mood.contains(searchQuery, ignoreCase = true) ||
                moodLabel.contains(searchQuery, ignoreCase = true)

            val matchesFavorite = !isFavoritesOnly || entry.isFavorite

            val matchesMood = selectedMoodFilter == null || entry.mood.lowercase() == selectedMoodFilter!!.lowercase()

            val matchesCalendar = if (selectedDayNum == null) {
                true
            } else {
                val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                entryCal.get(Calendar.YEAR) == currentYear &&
                    entryCal.get(Calendar.MONTH) == currentMonth &&
                    entryCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
            }

            matchesQuery && matchesFavorite && matchesCalendar && matchesMood
        }

        when (activeSortMode) {
            "Oldest" -> list.sortedBy { it.timestamp }
            "RecentlyEdited" -> list.sortedByDescending { it.timestamp } // In an offline DB, recently created/modified matches highest timestamp
            else -> list.sortedByDescending { it.timestamp }
        }
    }

    val locale = LocalConfiguration.current.locales[0]
    val sdfTime = remember(locale) { SimpleDateFormat("h:mm a", locale) }
    val sdfDate = remember(locale) { SimpleDateFormat("MMM dd, yyyy", locale) }

    val groupedEntries = remember(filteredEntries, locale) {
        val format = SimpleDateFormat("MMMM yyyy", locale)
        filteredEntries.groupBy { entry ->
            val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
            format.format(entryCal.time)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DesignTokens.AppBackground)) {
        if (!isWritingMode) {
            ScreenEnclosure {
                // Header Bar with Pinned Icons
                if (isSearching) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search by title, text, tag, mood...",
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(searchFocusRequester)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Journal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                color = DesignTokens.TextPrimary
                            )
                        }
                        Row {
                            IconButton(onClick = { showInsightsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Analytics,
                                    contentDescription = "Writing Insights",
                                    tint = DesignTokens.TextPrimary
                                )
                            }
                            IconButton(onClick = { showTemplatesDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Begin Writing",
                                    tint = DesignTokens.TextPrimary
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
                }

                // Main LazyColumn Timeline for maximum performance and smooth scrolling
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                ) {
                    val activeDraft = draft
                    if (activeDraft != null) {
                        item {
                            LunafloraCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "📝 Unfinished draft found",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val snippet = activeDraft.content.take(60).ifBlank { activeDraft.title.take(30) }
                                    Text(
                                        text = "\"$snippet...\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignTokens.TextSecondary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TextButton(onClick = {
                                            draftTitleToPreFill = activeDraft.title
                                            draftContentToPreFill = activeDraft.content
                                            draftMoodToPreFill = activeDraft.mood
                                            draftTagsToPreFill = activeDraft.tags
                                            draftPromptToPreFill = activeDraft.prompt
                                            isWritingMode = true
                                        }) {
                                            Text("Continue", fontWeight = FontWeight.Bold)
                                        }
                                        TextButton(onClick = { viewModel.clearDraft() }) {
                                            Text("Discard", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Daily prompt card
                    item {
                        val reflectionPrompt = remember { DailyPrompts.getTodayPrompt() }
                        ReflectionCard(
                            prompt = reflectionPrompt,
                            onBeginWriting = {
                                showTemplatesDialog = true
                            }
                        )
                    }

                    // Sorting & Filtering Options Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val monthName = DateFormatSymbols().months[currentMonth]
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedDayNum != null) "Reflections: $monthName $selectedDayNum" else "Recent Reflections",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = if (selectedDayNum != null) "Journal entries on selected calendar day." else "Your written history.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignTokens.TextSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Favorite Toggle Button
                                IconButton(onClick = { isFavoritesOnly = !isFavoritesOnly }) {
                                    Icon(
                                        imageVector = if (isFavoritesOnly) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Toggle Favorites Only",
                                        tint = if (isFavoritesOnly) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
                                    )
                                }

                                // Sort Options Pop-up
                                Box {
                                    var showSortMenu by remember { mutableStateOf(false) }
                                    IconButton(onClick = { showSortMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.FilterList,
                                            contentDescription = "Sort Options",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Newest first") },
                                            onClick = {
                                                activeSortMode = "Newest"
                                                showSortMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Oldest first") },
                                            onClick = {
                                                activeSortMode = "Oldest"
                                                showSortMenu = false
                                            }
                                        )
                                    }
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
                                } else {
                                    IconButton(onClick = { showCalendarDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarMonth,
                                            contentDescription = "View Calendar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sorting mode indicators
                    if (activeSortMode != "Newest" || selectedMoodFilter != null) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                if (activeSortMode != "Newest") {
                                    SuggestionChip(
                                        onClick = { activeSortMode = "Newest" },
                                        label = { Text("Sort: $activeSortMode") }
                                    )
                                }
                                selectedMoodFilter?.let { mood ->
                                    SuggestionChip(
                                        onClick = { selectedMoodFilter = null },
                                        label = { Text("Mood: $mood ✕") }
                                    )
                                }
                            }
                        }
                    }

                    if (filteredEntries.isEmpty()) {
                        item {
                            EmptyState(
                                illustration = {
                                    Icon(
                                        imageVector = Icons.Outlined.Spa,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                },
                                title = if (selectedDayNum != null) "No entries today" else "No reflections found",
                                subtitle = if (selectedDayNum != null) "You didn't write any entries on this day." else "No records matched your search, tag, or mood criteria."
                            )
                        }
                    } else {
                        groupedEntries.forEach { (monthHeader, monthEntries) ->
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = monthHeader,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }

                            items(monthEntries, key = { it.id }) { entry ->
                                val timeStr = sdfTime.format(Date(entry.timestamp))
                                val meta = if (entry.weatherContext.isNotBlank()) "${entry.weatherContext} • $timeStr" else timeStr
                                val dateStr = sdfDate.format(Date(entry.timestamp))

                                // Real-time matched query highlights in timeline cards!
                                val highlightedTitle = buildHighlightedText(entry.title.ifBlank { "Untitled Reflection" }, searchQuery, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                val highlightedSnippet = buildHighlightedText(entry.content, searchQuery, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                    .let { it.subSequence(0, 120.coerceAtMost(it.length)) }

                                JournalCard(
                                    titleAnnotated = highlightedTitle,
                                    snippetAnnotated = highlightedSnippet,
                                    date = dateStr,
                                    mood = entry.mood,
                                    metadataContext = meta,
                                    onClick = { selectedEntryForDetail = entry }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            // Write Mode with Template & Draft parameters preloaded
            JournalWritingView(
                onCancel = {
                    isWritingMode = false
                    selectedTemplate = null
                    draftTitleToPreFill = ""
                    draftContentToPreFill = ""
                    draftMoodToPreFill = ""
                    draftTagsToPreFill = ""
                    draftPromptToPreFill = ""
                },
                onSave = { title, content, mood, weather, fav, tags, imgs, voices ->
                    viewModel.addEntry(title, content, mood, weather, fav, tags, imgs, voices)
                    isWritingMode = false
                    selectedTemplate = null
                    draftTitleToPreFill = ""
                    draftContentToPreFill = ""
                    draftMoodToPreFill = ""
                    draftTagsToPreFill = ""
                    draftPromptToPreFill = ""
                },
                showPhotoSourceDialog = showPhotoSourceDialog,
                onShowPhotoSourceChanged = { showPhotoSourceDialog = it },
                initialPrompt = draftPromptToPreFill.ifBlank { initialPrompt },
                todayMood = todayMood,
                onMoodChanged = onSaveTodayMood,
                initialTitle = draftTitleToPreFill,
                initialContent = draftContentToPreFill,
                initialMood = draftMoodToPreFill,
                initialTags = draftTagsToPreFill,
                onSaveDraft = { t, c, m, tg, p ->
                    viewModel.saveDraft(t, c, m, tg, p, System.currentTimeMillis())
                },
                onClearDraft = {
                    viewModel.clearDraft()
                }
            )
        }

        // Detail dialog with entries passing live state
        JournalDetailDialog(
            entry = selectedEntryForDetail,
            entries = entries,
            onDismiss = { selectedEntryForDetail = null },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDeleteEntry = { viewModel.deleteEntry(it) }
        )

        JournalCalendarDialog(
            showCalendarDialog = showCalendarDialog,
            onDismiss = { showCalendarDialog = false },
            entries = entries,
            calendarMonth = currentMonth,
            calendarYear = currentYear,
            onMonthChanged = { m, y ->
                currentMonth = m
                currentYear = y
            },
            selectedDayNum = selectedDayNum,
            onDaySelected = {
                selectedDayNum = it
                showCalendarDialog = false
            }
        )

        // Templates Selection Dialog
        if (showTemplatesDialog) {
            Dialog(onDismissRequest = { showTemplatesDialog = false }) {
                LunafloraCard(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(AtaraxiaTheme.spacing.Space8),
                        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Choose a Template",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            IconButton(onClick = { showTemplatesDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close templates menu",
                                    tint = DesignTokens.TextPrimary
                                )
                            }
                        }
                        Text(
                            text = "Templates structure your mindfulness reflection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignTokens.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            writingTemplates.forEach { template ->
                                LunafloraCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTemplate = template
                                            draftTitleToPreFill = template.titleTemplate
                                            draftContentToPreFill = template.contentTemplate
                                            draftTagsToPreFill = template.defaultTags
                                            draftPromptToPreFill = template.description
                                            showTemplatesDialog = false
                                            isWritingMode = true
                                        },
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                ) {
                                    Column {
                                        Text(template.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(template.description, style = MaterialTheme.typography.bodySmall, color = DesignTokens.TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Local Writing Insights Dialog
        if (showInsightsDialog) {
            Dialog(onDismissRequest = { showInsightsDialog = false }) {
                LunafloraCard(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(AtaraxiaTheme.spacing.Space8),
                        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                    ) {
                        Text(
                            text = "🌿 Journal Insights",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InsightRow("Total Reflections", "${insights.writingFrequency}")
                            InsightRow("Total Words", "${insights.totalWords}")
                            InsightRow("Avg. Entry Length", "${insights.averageEntryLength} words")
                            InsightRow("Favorite Time", insights.favoriteWritingTime)
                            InsightRow("Common Mood", insights.mostCommonMood)
                            if (insights.longestReflectionTitle.isNotEmpty()) {
                                InsightRow("Longest Reflection", "${insights.longestReflectionTitle} (${insights.longestReflectionWords} words)")
                            }
                        }

                        AtaraxiaPrimaryButton(
                            text = "Done",
                            onClick = { showInsightsDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = DesignTokens.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = DesignTokens.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun buildHighlightedText(text: String, query: String, color: Color): AnnotatedString {
    val builder = AnnotatedString.Builder()
    
    val matches = mutableListOf<IntRange>()
    if (query.isNotBlank()) {
        var pos = 0
        while (pos < text.length) {
            val idx = text.indexOf(query, pos, ignoreCase = true)
            if (idx == -1) break
            matches.add(idx until (idx + query.length))
            pos = idx + query.length
        }
    }

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
            val isHighlighted = matches.any { i in it }
            val weight = if (isBold) FontWeight.Bold else FontWeight.Normal
            val style = if (isItalic) FontStyle.Italic else FontStyle.Normal
            val dec = if (isUnderline) TextDecoration.Underline else TextDecoration.None
            val bg = if (isHighlighted) color else Color.Unspecified
            
            builder.pushStyle(SpanStyle(
                fontWeight = weight,
                fontStyle = style,
                textDecoration = dec,
                background = bg
            ))
            builder.append(text[i].toString())
            builder.pop()
            i++
        }
    }
    return builder.toAnnotatedString()
}
