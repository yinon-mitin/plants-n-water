# Plants N Water

Plants N Water is a lightweight, local-first Android app for tracking plants, watering schedules, growth photos, and reminders. It is designed for beginners who just need a reliable reminder for basil, and for advanced users managing many plants across rooms, shelves, balconies, and gardens.

## Project Status

This repository contains the initial serious open-source Android implementation. Version `0.1.0` focuses on the offline data model, Compose UI scaffolding, plant management, growth photo history, watering history, reminder scheduling infrastructure, settings, and documented backup format.

## Features

- Fast plant creation with only a name and watering interval required
- Plant list, today view, detail screen, calendar-style schedule, photo timeline, settings, and import/export screens
- Plant photo capture from camera and image selection from the Android photo picker
- Chronological photo timeline with optional notes, dates, and flexible growth stage labels
- Latest plant photo thumbnails in plant cards
- Room-backed local storage for plants, watering events, notes, photos, and tags
- Watering schedule calculation with overdue/upcoming state
- Mark as watered, skip, postpone, archive, and delete flows at the architecture level
- Local Android notifications without Google Play Services
- Reboot receiver for reminder rescheduling
- DataStore settings for notifications, theme, list density, and first day of week
- JSON/ZIP export format documented in `docs/export-format.md`
- No accounts, analytics, ads, tracking, cloud dependency, or proprietary SDKs

## Screenshots

Screenshots will be added before the first tagged release:

- Today
- Plant list
- Add plant
- Plant detail
- Calendar
- Photo timeline
- Settings

## Build

Requirements:

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35

Build from the command line:

```bash
./gradlew assembleDebug
```

If the Gradle wrapper JAR is not present in your checkout, install Gradle locally once and run:

```bash
gradle wrapper --gradle-version 8.10.2
./gradlew assembleDebug
```

## Privacy

Plants N Water is local-first and privacy-friendly:

- No account registration
- No analytics or crash reporting SDK
- No ads
- No hidden network calls
- No cloud sync by default
- Photos and plant data stay on the device unless the user explicitly exports them
- Plant photos are copied into app-private storage and are never uploaded
- Camera permission is requested only when taking a new photo
- Gallery selection uses the Android photo picker and does not require broad media/storage permission
- EXIF/location extraction is not performed

## F-Droid Compatibility

The app is designed for F-Droid:

- Kotlin, Jetpack Compose, Room, DataStore, WorkManager, Coil, and AndroidX only
- No Firebase
- No Google Play Services
- No proprietary tracking or monetization SDKs
- Offline core functionality
- MIT licensed source
- Export/import format documented

See `docs/fdroid.md` for packaging notes.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- DataStore
- Coil Compose for lightweight local image thumbnail loading
- AlarmManager and WorkManager-compatible scheduling boundaries
- Coroutines and Flow
- MVVM with small repositories and testable domain logic
- Gradle Kotlin DSL

## Documentation

- Product specification: `docs/product-spec.md`
- Architecture: `docs/architecture.md`
- Export/import format: `docs/export-format.md`
- F-Droid notes: `docs/fdroid.md`
- Roadmap: `docs/roadmap.md`

## License

Plants N Water uses the MIT License. MIT keeps reuse simple for users, distributions, and contributors, including F-Droid packaging. The tradeoff is that it does not require downstream projects to publish modifications, unlike GPLv3 or AGPLv3.

## Contributing

Contributions are welcome. Please read `CONTRIBUTING.md` before opening a pull request.
