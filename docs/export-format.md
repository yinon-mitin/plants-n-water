# Export and Import Format

## Container

Preferred format: ZIP archive.

```text
plants-n-water-backup.zip
├── manifest.json
├── data.json
└── media/
    └── plant-photos/
        └── <photo-id>.<extension>
```

JSON-only export is acceptable when the user excludes photos.

The app stores photo binaries in app-private storage and stores generated local photo references in Room. Export should copy only the photo files referenced by `plantPhotos` into `media/plant-photos/`; it should not depend on original camera or gallery URIs.

Current implementation exports ZIP backups through Android’s Storage Access Framework. Backups include plants, watering events, plant notes, photo metadata, and referenced photo files. Settings export is represented in the format but not fully populated yet.

## `manifest.json`

```json
{
  "app": "plants-n-water",
  "formatVersion": 1,
  "createdAt": "2026-07-01T12:00:00Z",
  "includesPhotos": true
}
```

## `data.json`

```json
{
  "plants": [],
  "wateringEvents": [],
  "plantPhotos": [],
  "plantNotes": [],
  "tags": [],
  "plantTags": [],
  "settings": {}
}
```

## Import Rules

- Validate `app` and `formatVersion` before reading records.
- Reject unknown future major versions.
- Validate plant IDs referenced by events, photos, notes, and tags.
- Never overwrite without confirmation.
- Current implementation uses explicit replace import: the user must confirm before existing local plants, events, notes, photo metadata, and photo files are replaced.
- Copy media files into app-private storage.
- Preserve original timestamps where possible.
- Avoid preserving unnecessary sensitive image metadata unless the user explicitly requests it in a future feature.

## Known Limitations

- Merge import is not implemented yet.
- Settings are not fully restored yet.
- Tags are reserved in the format but not exported/imported by the current UI.

## Privacy

Exports are local files created only by explicit user action. They may include personal location labels, notes, and photos, so users should store them carefully.
