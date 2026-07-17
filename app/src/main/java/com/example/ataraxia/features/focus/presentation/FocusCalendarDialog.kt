package com.example.ataraxia.features.focus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.DateFormatSymbols
import java.util.Calendar

@Composable
fun FocusCalendarDialog(
    showCalendarDialog: Boolean,
    calendarYear: Int,
    calendarMonth: Int,
    selectedDayNum: Int?,
    sessions: List<FocusSessionEntity>,
    onDismiss: () -> Unit,
    onMonthChanged: (Int, Int) -> Unit,
    onDaySelected: (Int?) -> Unit
) {
    if (showCalendarDialog) {
        Dialog(onDismissRequest = onDismiss) {
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
            LunafloraCard(modifier = Modifier.fillMaxWidth().padding(horizontal = AtaraxiaTheme.spacing.Space8)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (calendarMonth == 0) {
                                onMonthChanged(11, calendarYear - 1)
                            } else {
                                onMonthChanged(calendarMonth - 1, calendarYear)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                contentDescription = "Prev Month",
                                tint = DesignTokens.TextPrimary
                            )
                        }

                        val monthName = DateFormatSymbols().months[calendarMonth]
                        Text(
                            text = "$monthName $calendarYear",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )

                        IconButton(onClick = {
                            if (calendarMonth == 11) {
                                onMonthChanged(0, calendarYear + 1)
                            } else {
                                onMonthChanged(calendarMonth + 1, calendarYear)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = DesignTokens.TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, calendarYear)
                        set(Calendar.MONTH, calendarMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                    val cells = mutableListOf<Int?>()
                    repeat(startDayOfWeek - 1) {
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
                                    val daySessions = sessions.filter { session ->
                                        val sessionCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
                                        sessionCal.get(Calendar.YEAR) == calendarYear &&
                                        sessionCal.get(Calendar.MONTH) == calendarMonth &&
                                        sessionCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                    }

                                    val isSelected = selectedDayNum == dayNumber
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                } else if (daySessions.isNotEmpty()) {
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
                                                onDaySelected(if (isSelected) null else dayNumber)
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = if (isSelected || daySessions.isNotEmpty()) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                        )
                                        if (daySessions.isNotEmpty()) {
                                            Text(text = "🌸", fontSize = 10.sp)
                                        } else {
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                            if (row.size < 7) {
                                repeat(7 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
