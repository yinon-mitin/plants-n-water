# Architecture

## Overview

Plants N Water uses a compact MVVM architecture:

- `ui`: Compose screens, navigation, themes, and view models.
- `domain`: pure Kotlin schedule and reminder decisions.
- `data.local`: Room entities, DAO, database, and type converters.
- `data.repository`: repositories that expose Flow-based app data.
- `settings`: DataStore-backed app preferences.
- `notifications`: Android notification receiver, boot receiver, and scheduler.

The app is intentionally small and avoids dependency injection frameworks. `PlantsNWaterApplication` owns an `AppContainer` that wires repositories and services.

## Data Flow

Compose screen -> ViewModel -> Repository -> Room/DataStore

Room emits `Flow` updates. ViewModels convert database models into simple UI state.

## Reminder Strategy

The first implementation uses `AlarmManager` for local reminders because it works without Google Play Services and survives offline use. `BootCompletedReceiver` reschedules future reminders after reboot. Notification actions route back through `WateringReminderReceiver`.

Exact-alarm behavior varies by Android version and device policy. The app should degrade gracefully by using inexact alarms when exact scheduling is unavailable.

## Images

Photo metadata is stored in Room. Actual files should be copied into app-private storage under `plant-photos/` so exports can include them later without relying on external content URIs.

## Testing

Business logic is kept in `domain` so JVM tests can cover:

- Next watering date calculation
- Due/overdue classification
- Import/export validation
- Reminder scheduling decisions

## Extensibility

Future reminder types such as fertilizing, repotting, pruning, and sensor-driven watering should reuse the same event/schedule model rather than adding isolated one-off flows.
