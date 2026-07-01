package dev.yinon.plantsnwater.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object WateringSchedule {
    fun nextWateringDate(
        lastWateredAt: Instant?,
        createdAt: Instant,
        intervalDays: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDate {
        require(intervalDays > 0) { "Watering interval must be positive." }
        val base = lastWateredAt ?: createdAt
        return base.atZone(zoneId).toLocalDate().plusDays(intervalDays.toLong())
    }

    fun classify(nextWateringDate: LocalDate, clock: Clock = Clock.systemDefaultZone()): WateringState {
        val today = LocalDate.now(clock)
        return when {
            nextWateringDate.isBefore(today) -> WateringState.Overdue(
                ChronoUnit.DAYS.between(nextWateringDate, today).toInt()
            )
            nextWateringDate == today -> WateringState.DueToday
            else -> WateringState.Upcoming(ChronoUnit.DAYS.between(today, nextWateringDate).toInt())
        }
    }

    fun postponedDate(from: LocalDate, days: Int): LocalDate {
        require(days > 0) { "Postpone days must be positive." }
        return from.plusDays(days.toLong())
    }
}

sealed interface WateringState {
    data object DueToday : WateringState
    data class Overdue(val days: Int) : WateringState
    data class Upcoming(val days: Int) : WateringState
}
