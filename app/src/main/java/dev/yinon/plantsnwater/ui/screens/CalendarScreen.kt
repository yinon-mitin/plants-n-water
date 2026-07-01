package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.ui.CalendarViewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(onPlantClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: CalendarViewModel = viewModel(factory = PlantViewModelFactory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            ScreenColumn {
                SectionTitle(stringResource(R.string.calendar))
                if (state.scheduledByDate.isEmpty()) {
                    EmptyState(stringResource(R.string.no_schedule_yet), stringResource(R.string.no_schedule_yet_body))
                }
                CalendarMonth(
                    visibleMonth = state.visibleMonth,
                    selectedDate = state.selectedDate,
                    scheduledDates = state.scheduledByDate.keys,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                    onSelect = viewModel::selectDate
                )
                SectionTitle("${stringResource(R.string.selected_day)} · ${state.selectedDate.formatDate()}")
                if (state.selectedPlants.isEmpty()) {
                    Text(stringResource(R.string.no_tasks_for_day), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(state.selectedPlants, key = { it.id }) { plant ->
            ScreenColumn {
                PlantCard(plant = plant, onClick = { onPlantClick(plant.id) })
            }
        }
    }
}

@Composable
private fun CalendarMonth(
    visibleMonth: LocalDate,
    selectedDate: LocalDate,
    scheduledDates: Set<LocalDate>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit
) {
    val days = rememberMonthDays(visibleMonth)
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
                Text(
                    visibleMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy")),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onNext) { Text("›", style = MaterialTheme.typography.headlineSmall) }
            }
            WeekHeader()
            days.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        CalendarDayCell(
                            date = date,
                            visibleMonth = visibleMonth,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            hasTasks = scheduledDates.contains(date),
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHeader() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    visibleMonth: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = if (date.month == visibleMonth.month) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfMonth.toString(), color = textColor, style = MaterialTheme.typography.bodyMedium)
            if (hasTasks) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(3.dp)
                )
            }
        }
    }
}

private fun rememberMonthDays(month: LocalDate): List<LocalDate> {
    val first = month.withDayOfMonth(1)
    val startOffset = first.dayOfWeek.value - DayOfWeek.MONDAY.value
    val start = first.minusDays(startOffset.toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}
