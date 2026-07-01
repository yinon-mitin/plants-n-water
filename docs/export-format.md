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
- Offer merge or replace.
- Copy media files into app-private storage.
- Preserve original timestamps where possible.

## Privacy

Exports are local files created only by explicit user action. They may include personal location labels, notes, and photos, so users should store them carefully.
