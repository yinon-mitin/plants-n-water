# Product Specification

## Goal

Plants N Water helps people remember when to water plants, track plant condition, and keep a local growth timeline.

## Users

- Beginner: owns a few plants, wants a quick reminder, and may not know plant-care terminology.
- Advanced user: manages many plants with custom locations, tags, watering routines, notes, and photos.

## Principles

- Beginner flow first: name, interval, done.
- Advanced fields are optional and collapsible.
- No account, no cloud dependency, no tracking.
- Daily use should be one tap: open Today, water plants, leave.
- Data should be portable.

## Core Screens

1. Today: overdue and due plants with quick watering actions.
2. Plants: active plants with status, location, tags, and next watering date.
3. Add/Edit Plant: fast required fields plus advanced metadata.
4. Plant Detail: schedule, history, notes, and photos.
5. Calendar: daily/weekly/monthly schedule overview.
6. Photo Timeline: local growth photos over time.
7. Settings: reminders, theme, list density, backups, privacy.
8. Import/Export: backup and restore controls.

## First Release Scope

- Local Room database.
- Plant CRUD.
- Watering schedule calculation.
- Watering events.
- Reminder scheduling foundation.
- Settings foundation.
- Export/import format documentation.
- Basic tests for domain logic.

## Out of Scope for 0.1

- Cloud sync.
- Plant care knowledge base.
- Fertilizing, pruning, repotting reminders.
- IoT watering devices.
- WebDAV backup.
- QR labels.
