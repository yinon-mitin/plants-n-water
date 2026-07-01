# Contributing

Thank you for improving Plants N Water.

## Development Principles

- Keep the app local-first and privacy-friendly.
- Avoid proprietary dependencies and SDKs.
- Prefer small, focused changes.
- Add tests for schedule logic, import/export validation, and reminder decisions.
- Use string resources for user-visible text.
- Keep advanced features discoverable without cluttering the beginner flow.

## Pull Requests

1. Open an issue for large feature work.
2. Keep pull requests focused.
3. Include screenshots for UI changes.
4. Run `./gradlew test assembleDebug` before requesting review.
5. Document data format or database changes.

## Dependency Policy

New dependencies should be:

- Open source
- Available from Maven Central or Google Maven
- Reasonably small
- Useful enough to justify maintenance and APK size impact
- Compatible with F-Droid builds
