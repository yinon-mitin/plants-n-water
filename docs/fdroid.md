# F-Droid Notes

Plants N Water is designed for F-Droid distribution.

## Current Compatibility

- No Google Play Services dependency.
- No Firebase dependency.
- No proprietary analytics or crash reporting.
- No network dependency for core behavior.
- Source license: MIT.
- Gradle Kotlin DSL build.
- Dependencies are open-source AndroidX/Kotlin libraries plus Coil for image loading.
- Coil is Apache-2.0 licensed, F-Droid compatible, and does not require Google Play Services or proprietary network services.

## Packaging Notes

- Build flavor is not required for F-Droid because the default app contains no proprietary services.
- Keep `versionCode` monotonic.
- Avoid generated binary assets that cannot be reproduced.
- Document any future binary blobs or remove them.
- Do not add obfuscation that makes reproducibility harder unless there is a strong reason.

## Permissions

- `POST_NOTIFICATIONS`: Android 13+ reminder permission.
- `SCHEDULE_EXACT_ALARM`: used only for local reminder timing where allowed.
- `RECEIVE_BOOT_COMPLETED`: reschedules local reminders after reboot.
- `CAMERA`: requested only when taking plant photos directly.
- Gallery image selection uses the Android photo picker, so broad image/media storage permission is not requested.

## Photo Storage

- Plant photos are stored in app-private `files/plant-photos`.
- The Room database stores generated internal photo references and metadata, not public file paths.
- Photos are local-only and are included in the documented backup/export media folder when export support is implemented.
- The app does not upload photos, extract location metadata, run analytics, or depend on proprietary SDKs.
