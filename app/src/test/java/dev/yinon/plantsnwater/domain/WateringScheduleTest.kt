package dev.yinon.plantsnwater.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class WateringScheduleTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun nextWateringDateUsesLastWateredWhenPresent() {
        val created = Instant.parse("2026-07-01T08:00:00Z")
        val watered = Instant.parse("2026-07-03T10:00:00Z")

        val next = WateringSchedule.nextWateringDate(watered, created, 2, zone)

        assertEquals(LocalDate.parse("2026-07-05"), next)
    }

    @Test
    fun nextWateringDateFallsBackToCreatedDate() {
        val created = Instant.parse("2026-07-01T08:00:00Z")

        val next = WateringSchedule.nextWateringDate(null, created, 7, zone)

        assertEquals(LocalDate.parse("2026-07-08"), next)
    }

    @Test
    fun classifyReturnsOverdueDueAndUpcoming() {
        val clock = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), zone)

        assertEquals(WateringState.Overdue(2), WateringSchedule.classify(LocalDate.parse("2026-07-08"), clock))
        assertEquals(WateringState.DueToday, WateringSchedule.classify(LocalDate.parse("2026-07-10"), clock))
        assertEquals(WateringState.Upcoming(3), WateringSchedule.classify(LocalDate.parse("2026-07-13"), clock))
    }

    @Test(expected = IllegalArgumentException::class)
    fun intervalMustBePositive() {
        WateringSchedule.nextWateringDate(null, Instant.parse("2026-07-01T00:00:00Z"), 0, zone)
    }
}
