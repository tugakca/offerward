# Offerward

Browse the newest remote developer jobs and track every application from "saved" to "offer", all offline-first on your phone.

> Work in progress. The MVP is being built in the open; see the roadmap below.

## Why

I built Offerward for my own job search. Job boards are good at listing jobs and bad at remembering what you did with them. Offerward pulls the newest remote developer jobs from [Himalayas](https://himalayas.app) and [Jobicy](https://jobicy.com), shows each job once even when it is posted on both, shows where each one hires from, lets you filter them on the device, and keeps a simple pipeline of your applications, interview dates and notes.

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
| `core:network` | One client per job board (Retrofit, kotlinx.serialization), translated into a common shape |
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
- [x] Applications: pipeline, notes, interview date
- [x] Second job board (Jobicy): parallel fetch, cross-board de-duplication, partial failure
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

**One broad query per board, every filter on the device.** The app fetches the 100 newest Himalayas jobs matching "developer" and up to 300 jobs from Jobicy's engineering category, from every country. Measured on the live APIs, those queries return about 95% and 85% engineering roles, while the unfiltered feeds are mostly non-tech. Text search and the Android / Senior / Worldwide chips are Room queries, so changing a filter costs no request. Each card shows where the company hires from, because "remote" often means "remote, but only from one country".

**Notes are owned by the text field while you type.** The notes draft is seeded once from Room and then lives in the screen state; every keystroke is saved, but the database echo never overwrites the field, so the cursor never jumps and no character is lost. Saves are conflated, so fast typing never queues stale writes.

**Most urgent first.** Applications are grouped Interview → Applied → Saved → Offer → Rejected, with the soonest interview on top. Setting an interview date moves the application to the Interview stage.

**MVI on every screen.** One immutable `UiState`, actions in through a single `onAction`, a pure `reduce(state, mutation)` function, and one-off effects (snackbars) through a `Channel` so they are shown once and not replayed on rotation. The reducer and the ViewModel are covered by plain JVM tests.

**Applications keep a snapshot of the job.** There is deliberately no foreign key from applications to jobs. Jobs leave the cache when they expire, and an application you are in the middle of must not disappear with them.

**Every job board behind one interface.** Each board is a `JobNetworkDataSource` that hides its own paging (page numbers on Himalayas, an opaque cursor on Jobicy), date format and quirks such as HTML entities in plain-text fields. Hilt collects them into a `Set`, so adding a board is one class and one `@IntoSet` line; the repository does not change. Boards are fetched in parallel, and one slow board does not delay the other.

**The same job on two boards is shown once.** Companies often post one job on several boards. Two listings are the same job when the company and title match after ignoring case, accents, punctuation and suffixes such as "Inc.". Himalayas' copy wins, because its Apply link usually goes straight to the employer. The key is deliberately strict: "Android Engineer" and "Senior Android Engineer" stay separate, since a missed duplicate costs less than a hidden job.

**One board down is not an outage.** If one board fails, its previously cached jobs are kept, the others update, and a snackbar names the board that could not be reached. Only when every board fails does the refresh count as failed.

**Schema changes are migrated, not destructive.** Adding the source column moved the database to version 2 with a Room auto-migration; jobs cached before it default to Himalayas. A migration test opens a real version 1 database with an application in it and checks that nothing is lost.

**Why Himalayas and not Remotive.** The plan started with Remotive's public API. When building the client, it turned out to return only a handful of jobs and to ignore its category and search filters. Himalayas offers thousands of remote jobs, a country filter and explicit permission to show its jobs in your own app.

## Data sources

Job listings come from the [Himalayas public API](https://himalayas.app/api) and the [Jobicy public API](https://jobi.cy/apidocs). Every job names its board and links back to it, and Apply buttons open the link from the feed unchanged, as both boards' terms require. Expired jobs, and Jobicy jobs that leave its 7-day feed, are never shown.

## License

[MIT](LICENSE)
