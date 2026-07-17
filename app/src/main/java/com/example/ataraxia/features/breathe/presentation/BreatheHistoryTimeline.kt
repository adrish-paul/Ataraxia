package com.example.ataraxia.features.breathe.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun BreatheHistoryTimeline(sessions: List<BreatheSessionEntity>, onDelete: (Long) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("yyyyMMdd", locale) }
    val displaySdf = remember(locale) { SimpleDateFormat("MMMM d, yyyy", locale) }
    val grouped = remember(sessions, sdf) {
        sessions.groupBy { sdf.format(Date(it.timestamp)) }.toSortedMap(reverseOrder())
    }

    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
        grouped.forEach { (dateKey, daySessions) ->
            val displayDate = try {
                val y = dateKey.substring(0, 4).toInt()
                val m = dateKey.substring(4, 6).toInt() - 1
                val d = dateKey.substring(6, 8).toInt()
                displaySdf.format(Calendar.getInstance().apply { set(y, m, d) }.time)
            } catch (_: Exception) { dateKey }

            Text(text = displayDate, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextSecondary, modifier = Modifier.padding(vertical = 2.dp))

            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                daySessions.forEach { session ->
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                Column {
                                    Text(text = session.method, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = formatBreatheDuration(session.durationSeconds), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        if (session.mood.isNotEmpty()) {
                                            Text(text = "·", color = DesignTokens.TextSecondary, style = MaterialTheme.typography.labelMedium)
                                            Text(text = session.mood, style = MaterialTheme.typography.labelMedium, color = DesignTokens.TextSecondary)
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { onDelete(session.id) }) {
                                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = DesignTokens.TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatBreatheDuration(totalSeconds: Int): String = when {
    totalSeconds < 60 -> "< 1 Min"
    else -> { val m = totalSeconds / 60; val s = totalSeconds % 60; if (s == 0) "$m Min" else "$m Min $s Sec" }
}
