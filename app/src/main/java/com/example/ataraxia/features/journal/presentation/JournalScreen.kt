package com.example.ataraxia.features.journal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ataraxia.data.local.DailyPrompts
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.JournalCard
import com.example.ataraxia.ui.components.PrimaryTextField
import com.example.ataraxia.ui.components.ReflectionCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import androidx.compose.ui.platform.LocalConfiguration
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onWritingModeChanged: (Boolean) -> Unit,
    scrollToTopKey: Int = 0
) {
    val entries by viewModel.allEntries.collectAsState()

    var isWritingMode by remember { mutableStateOf(false) }

    var isFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<JournalEntryEntity?>(null) }

    val calendarInstance = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendarInstance.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH)) }

    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val listScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0 && !isWritingMode) listScrollState.animateScrollTo(0)
    }

    LaunchedEffect(isWritingMode) {
        onWritingModeChanged(isWritingMode)
    }

    val filteredEntries = remember(entries, searchQuery, isFavoritesOnly, selectedDayNum, currentMonth, currentYear) {
        entries.filter { entry ->
            val matchesQuery = searchQuery.isBlank() ||
                entry.title.contains(searchQuery, ignoreCase = true) ||
                entry.content.contains(searchQuery, ignoreCase = true) ||
                entry.tags.contains(searchQuery, ignoreCase = true)

            val matchesFavorite = !isFavoritesOnly || entry.isFavorite

            val matchesCalendar = if (selectedDayNum == null) {
                true
            } else {
                val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                entryCal.get(Calendar.YEAR) == currentYear &&
                entryCal.get(Calendar.MONTH) == currentMonth &&
                entryCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
            }

            matchesQuery && matchesFavorite && matchesCalendar
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DesignTokens.AppBackground)) {
        if (!isWritingMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .verticalScroll(listScrollState)
            ) {
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
                        Row {
                            IconButton(onClick = { isFavoritesOnly = !isFavoritesOnly }) {
                                Icon(
                                    imageVector = if (isFavoritesOnly) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Toggle Favorites Only",
                                    tint = if (isFavoritesOnly) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
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

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                val reflectionPrompt = remember { DailyPrompts.getTodayPrompt() }
                ReflectionCard(
                    prompt = reflectionPrompt,
                    onBeginWriting = { isWritingMode = true }
                )

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

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

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                if (filteredEntries.isEmpty()) {
                    EmptyState(
                        illustration = {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        },
                        title = if (selectedDayNum != null) "No entries today" else "No reflections logged yet",
                        subtitle = if (selectedDayNum != null) "You didn't write any entries on this day." else "Begin writing to start documenting your mindfulness journey."
                    )
                } else {
                    val locale = LocalConfiguration.current.locales[0]
                    val grouped = remember(filteredEntries, locale) {
                        val sdf = SimpleDateFormat("yyyyMMdd", locale)
                        filteredEntries.groupBy { sdf.format(Date(it.timestamp)) }.toSortedMap(reverseOrder())
                    }
                    val displaySdf = remember(locale) { SimpleDateFormat("MMMM d, yyyy", locale) }
                    val sdfTime = remember(locale) { SimpleDateFormat("h:mm a", locale) }
                    val sdfDate = remember(locale) { SimpleDateFormat("MMM dd, yyyy", locale) }

                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                        grouped.forEach { (dateKey, dayEntries) ->
                            val displayDate = try {
                                val y = dateKey.substring(0, 4).toInt()
                                val m = dateKey.substring(4, 6).toInt() - 1
                                val d = dateKey.substring(6, 8).toInt()
                                displaySdf.format(Calendar.getInstance().apply { set(y, m, d) }.time)
                            } catch (_: Exception) { dateKey }

                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextSecondary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            dayEntries.forEach { entry ->
                                val timeStr = sdfTime.format(Date(entry.timestamp))
                                val meta = if (entry.weatherContext.isNotBlank()) "${entry.weatherContext} • $timeStr" else timeStr
                                val dateStr = sdfDate.format(Date(entry.timestamp))
                                JournalCard(
                                    title = entry.title,
                                    snippet = entry.content,
                                    date = dateStr,
                                    mood = entry.mood,
                                    metadataContext = meta,
                                    onClick = { selectedEntryForDetail = entry }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(130.dp))
            }
        } else {
            JournalWritingView(
                onCancel = { isWritingMode = false },
                onSave = { title, content, mood, weather, fav, tags, imgs, voices ->
                    viewModel.addEntry(title, content, mood, weather, fav, tags, imgs, voices)
                    isWritingMode = false
                },
                showPhotoSourceDialog = showPhotoSourceDialog,
                onShowPhotoSourceChanged = { showPhotoSourceDialog = it }
            )
        }

        JournalDetailDialog(
            entry = selectedEntryForDetail,
            onDismiss = { selectedEntryForDetail = null },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDeleteEntry = { viewModel.deleteEntry(it) }
        )

        JournalCalendarDialog(
            showCalendarDialog = showCalendarDialog,
            calendarYear = currentYear,
            calendarMonth = currentMonth,
            selectedDayNum = selectedDayNum,
            entries = entries,
            onDismiss = { showCalendarDialog = false },
            onMonthChanged = { month, year ->
                currentMonth = month
                currentYear = year
                selectedDayNum = null
            },
            onDaySelected = { day ->
                selectedDayNum = day
                showCalendarDialog = false
            }
        )
    }
}
