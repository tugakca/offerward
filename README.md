# Offerward

Browse the newest remote developer jobs and track every application from "saved" to "offer", all offline-first on your phone.

> Work in progress. The MVP is being built in the open; see the roadmap below.

## Why

I built Offerward for my own job search. Job boards are good at listing jobs and bad at remembering what you did with them. Offerward pulls the newest remote developer jobs from [Himalayas](https://himalayas.app), shows where each one hires from, lets you filter them on the device, and keeps a simple pipeline of your applications, interview dates and notes.

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
| `core:network` | Himalayas API client (Retrofit, kotlinx.serialization) |
| `core:database` | Room cache of jobs and the user's applications |
| `core:data` | Offline-first repositories, refresh policy, settings (DataStore) |
| `core:designsystem` | Theme and shared Compose components |
| `feature:discover` | Remote job list and job detail |
| `feature:applications` | Application pipeline, notes and interview dates |

An AI module lands with the job summary feature.

## Roadmap

- [x] Project skeleton, build-logic, CI
- [x] Himalayas API client and Room cache (offline-first, refresh policy)
- [x] Discover: list, search, filters, pull-to-refresh, job detail
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

## Design decisions

**Offline-first, Room is the single source of truth.** Screens only observe Room. The network only writes into the cache, so the app opens instantly and works without a connection. A failed refresh keeps the old jobs rather than showing an empty screen.

**A refresh policy instead of refreshing on every screen open.** The source updates its data daily, so automatic refreshes run at most every 6 hours and pull-to-refresh at most every 15 minutes. When pull-to-refresh is skipped, the app says when the next one is allowed.

**One broad query on the server, every filter on the device.** The app fetches the 100 newest jobs matching "developer" from every country. Measured on the live API, that query returns about 95% software roles, while the unfiltered feed is mostly non-tech. Text search and the Android / Senior / Worldwide chips are Room queries, so changing a filter costs no request. Each card shows where the company hires from, because "remote" often means "remote, but only from one country".

**MVI on every screen.** One immutable `UiState`, actions in through a single `onAction`, a pure `reduce(state, mutation)` function, and one-off effects (snackbars) through a `Channel` so they are shown once and not replayed on rotation. The reducer and the ViewModel are covered by plain JVM tests.

**Applications keep a snapshot of the job.** There is deliberately no foreign key from applications to jobs. Jobs leave the cache when they expire, and an application you are in the middle of must not disappear with them.

**Why Himalayas and not Remotive.** The plan started with Remotive's public API. When building the client, it turned out to return only a handful of jobs and to ignore its category and search filters. Himalayas offers thousands of remote jobs, a country filter and explicit permission to show its jobs in your own app.

## Data source

Job listings come from the [Himalayas public API](https://himalayas.app/api). Every job links back to Himalayas, and Apply buttons use the job's application link unchanged, as Himalayas' terms require. Expired jobs are never shown.

## License

[MIT](LICENSE)
