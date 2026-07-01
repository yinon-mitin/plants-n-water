# F-Droid Notes

Plants N Water is designed for F-Droid distribution.

## Current Compatibility

- No Google Play Services dependency.
- No Firebase dependency.
- No proprietary analytics or crash reporting.
- No network dependency for core behavior.
- Source license: MIT.
- Gradle Kotlin DSL build.
- Dependencies are open-source AndroidX/Kotlin libraries.

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
- `CAMERA`: only needed when taking plant photos directly.
- `READ_MEDIA_IMAGES`: only needed when adding images from gallery.
