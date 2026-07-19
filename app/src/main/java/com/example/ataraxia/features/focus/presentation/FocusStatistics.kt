package com.example.ataraxia.features.focus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FocusStatistics(
    sessions: List<FocusSessionEntity>,
    modifier: Modifier = Modifier
) {
    val stats = remember(sessions) { calculateStats(sessions) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
    ) {
        // Combined Core Stats Card (Grid style inside a single card)
        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        title = "Total Focus",
                        value = formatMinutes(stats.totalFocusMinutes),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        title = "Completed",
                        value = "${stats.sessionsCompleted}",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.08f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        title = "Average Session",
                        value = "${stats.avgSessionMinutes}m",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        title = "Longest Session",
                        value = "${stats.longestSessionMinutes}m",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Combined Personal Insights Card (50/50 split rows)
        LunafloraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personal Insights",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DesignTokens.TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InsightItem(
                        label = "Favorite Intention",
                        value = stats.favoriteIntention,
                        modifier = Modifier.weight(1f)
                    )
                    InsightItem(
                        label = "Most Active Day",
                        value = stats.mostProductiveDay,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InsightItem(
                        label = "Peak Focus Hour",
                        value = stats.mostProductiveHour,
                        modifier = Modifier.weight(1f)
                    )
                    InsightItem(
                        label = "Current Streak",
                        value = if (stats.currentStreak == 1) "1 Day" else "${stats.currentStreak} Days",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Weekly Activity Chart Card
        WeeklyActivityChart(sessions = sessions)
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun InsightItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = DesignTokens.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WeeklyActivityChart(sessions: List<FocusSessionEntity>) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = configuration.locales[0]
    
    val dayLabels = remember(locale) {
        val firstDay = Calendar.getInstance().firstDayOfWeek
        val baseLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val shift = (firstDay - 1).coerceIn(0, 6)
        baseLabels.subList(shift, 7) + baseLabels.subList(0, shift)
    }
    
    val minutesPerDay = remember(sessions) {
        val cal = Calendar.getInstance()
        val currentWeekMin = IntArray(7)
        
        // Find start of current week
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = cal.timeInMillis
        
        // Count minutes across all sessions in this week (including cancelled ones)
        sessions.filter { it.timestamp >= weekStart }.forEach { session ->
            cal.timeInMillis = session.timestamp
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val firstDay = cal.firstDayOfWeek
            // Calculate chronologically correct offset from firstDayOfWeek (0 to 6)
            val offset = (dayOfWeek - firstDay + 7) % 7
            if (offset in 0..6) {
                currentWeekMin[offset] += session.durationMinutes
            }
        }
        currentWeekMin
    }

    val maxMinutes = remember(minutesPerDay) {
        minutesPerDay.maxOrNull()?.coerceAtLeast(1) ?: 1
    }

    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Weekly Practice",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DesignTokens.TextPrimary
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Bars area (aligned to the bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    minutesPerDay.forEachIndexed { index, minutes ->
                        val fillFraction = (minutes.toFloat() / maxMinutes).coerceIn(0.04f, 1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (minutes > 0) {
                                Text(
                                    text = "${minutes}m",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .height(60.dp * fillFraction)
                                    .width(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (minutes > 0) 1f else 0.15f))
                            )
                        }
                    }
                }

                // Row 2: Day Labels area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = DesignTokens.TextSecondary,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private data class CalculatedStats(
    val totalFocusMinutes: Int,
    val sessionsCompleted: Int,
    val avgSessionMinutes: Int,
    val longestSessionMinutes: Int,
    val favoriteIntention: String,
    val mostProductiveDay: String,
    val mostProductiveHour: String,
    val currentStreak: Int
)

private fun calculateStats(sessions: List<FocusSessionEntity>): CalculatedStats {
    val totalFocusMinutes = sessions.sumOf { it.durationMinutes }
    val sessionsCompleted = sessions.count { it.completionStatus == "Completed" }
    
    val avgSessionMinutes = if (sessions.isNotEmpty()) totalFocusMinutes / sessions.size else 0
    val longestSessionMinutes = sessions.maxOfOrNull { it.durationMinutes } ?: 0

    // Favorite Intention (calculated across all logged intentions with duration > 0)
    val favoriteIntention = sessions
        .filter { it.durationMinutes > 0 && it.intentionName.isNotBlank() }
        .groupBy { it.intentionName }
        .maxByOrNull { it.value.size }
        ?.key ?: "None"

    // Most Productive Weekday
    val cal = Calendar.getInstance()
    val weekdaySums = IntArray(7)
    sessions.forEach { session ->
        cal.timeInMillis = session.timestamp
        val day = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)
        if (day in 0..6) {
            weekdaySums[day] += session.durationMinutes
        }
    }
    val weekdays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    var maxDayIndex = 0
    var maxDaySum = 0
    weekdaySums.forEachIndexed { index, sum ->
        if (sum > maxDaySum) {
            maxDaySum = sum
            maxDayIndex = index
        }
    }
    val mostProductiveDay = if (maxDaySum > 0) weekdays[maxDayIndex] else "None"

    // Most Productive Hour
    val hourSums = IntArray(24)
    sessions.forEach { session ->
        cal.timeInMillis = session.timestamp
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour in 0..23) {
            hourSums[hour] += session.durationMinutes
        }
    }
    var maxHour = -1
    var maxHourSum = 0
    hourSums.forEachIndexed { hour, sum ->
        if (sum > maxHourSum) {
            maxHourSum = sum
            maxHour = hour
        }
    }
    val mostProductiveHour = if (maxHour != -1 && maxHourSum > 0) {
        val startHour = if (maxHour == 0) 12 else if (maxHour > 12) maxHour - 12 else maxHour
        val startAmPm = if (maxHour >= 12) "PM" else "AM"
        val nextHour = (maxHour + 1) % 24
        val endHour = if (nextHour == 0) 12 else if (nextHour > 12) nextHour - 12 else nextHour
        val endAmPm = if (nextHour >= 12) "PM" else "AM"
        "$startHour:00 $startAmPm - $endHour:00 $endAmPm"
    } else "None"

    val currentStreak = calculateFocusStreak(sessions)

    return CalculatedStats(
        totalFocusMinutes = totalFocusMinutes,
        sessionsCompleted = sessionsCompleted,
        avgSessionMinutes = avgSessionMinutes,
        longestSessionMinutes = longestSessionMinutes,
        favoriteIntention = favoriteIntention,
        mostProductiveDay = mostProductiveDay,
        mostProductiveHour = mostProductiveHour,
        currentStreak = currentStreak
    )
}

private fun calculateFocusStreak(sessions: List<FocusSessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val uniqueDays = sessions.map { session ->
        sdf.format(Date(session.timestamp)).toInt()
    }.distinct().sortedDescending()

    if (uniqueDays.isEmpty()) return 0

    val today = sdf.format(Date()).toInt()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.let { sdf.format(it.time).toInt() }

    val newestDay = uniqueDays.first()
    if (newestDay != today && newestDay != yesterday) {
        return 0
    }

    var streak = 1
    val currentDayCal = Calendar.getInstance().apply {
        val newestStr = newestDay.toString()
        set(Calendar.YEAR, newestStr.take(4).toInt())
        set(Calendar.MONTH, newestStr.substring(4, 6).toInt() - 1)
        set(Calendar.DAY_OF_MONTH, newestStr.takeLast(2).toInt())
    }

    for (i in 1 until uniqueDays.size) {
        currentDayCal.add(Calendar.DATE, -1)
        val expectedDay = sdf.format(currentDayCal.time).toInt()
        if (uniqueDays[i] == expectedDay) {
            streak++
        } else {
            break
        }
    }
    return streak
}

private fun formatMinutes(totalMinutes: Int): String {
    return when {
        totalMinutes == 0 -> "0m"
        totalMinutes < 60 -> "${totalMinutes}m"
        else -> "${totalMinutes / 60}h ${totalMinutes % 60}m"
    }
}
