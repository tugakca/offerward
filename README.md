# Offerward

Find remote Android jobs and track every application from "saved" to "offer", all offline-first on your phone.

> Work in progress. The MVP is being built in the open; see the roadmap below.

## Why

I built Offerward for my own job search. Job boards are good at listing jobs and bad at remembering what you did with them. Offerward pulls remote jobs from [Remotive](https://remotive.com), lets you filter them on the device, and keeps a simple pipeline of your applications, interview dates and notes.

## Tech

- **UI:** Jetpack Compose, Material 3, Navigation 3
- **Architecture:** MVI-style unidirectional data flow (single immutable `UiState` per screen, actions in, pure reducer), offline-first data layer with Room as the single source of truth
- **DI:** Hilt
- **Build:** multi-module (`core:*`, `feature:*`), convention plugins in `build-logic`, version catalog
- **Quality:** unit tests, Spotless/ktlint, Android Lint, GitHub Actions CI

## Modules

| Module | Responsibility |
| --- | --- |
| `app` | Activity, navigation host, Hilt application |
| `core:model` | Plain Kotlin domain models |
| `core:designsystem` | Theme and shared Compose components |
| `feature:discover` | Remote job list and job detail |
| `feature:applications` | Application pipeline, notes and interview dates |

More `core` modules (network, database, data, AI) land as the features that need them are built.

## Roadmap

- [x] Project skeleton, build-logic, CI
- [ ] Remotive API client and Room cache (offline-first, rate-limit aware)
- [ ] Discover: list, search, filters, job detail
- [ ] Applications: pipeline, notes, interview date
- [ ] Release setup, Crashlytics, closed testing on Google Play
- [ ] On-device job summary with ML Kit GenAI (Gemini Nano), hidden on unsupported devices
- [ ] Interview reminders

## Building

Requires JDK 17 and the Android SDK (compileSdk 37).

```bash
./gradlew assembleDebug
./gradlew spotlessCheck lint testDebugUnitTest
```

## Data source

Job listings come from the [Remotive public API](https://github.com/remotive-com/remote-jobs-api). Every job links back to its Remotive page, as Remotive's terms require.

## License

[MIT](LICENSE)
