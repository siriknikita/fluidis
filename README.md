# Fluidis

A personal hydration tracking Android app built with Kotlin and Jetpack Compose. Track water, tea, and coffee intake with daily goals, visual analytics, calendar history, and editable entries. Fully local — no backend, no accounts, no sync.

## Features

- **Quick tracking** — Tap to add default servings, long-press for custom amounts, hold minus to rapid-remove
- **Water drop progress** — Animated Canvas-based fill indicator with gradient transitions (blue -> gold/green on goal met -> amber on upper bound exceeded)
- **Statistics** — Patterns rather than a second calendar: the period (week / month / all-time) against the one before it, a daily chart in four modes (bar, stacked by drink, line, area) with a 7-day rolling average, how the drink mix shifted, and your weekday habits across all history
- **Calendar history** — Month grid with a goal-progress ring on every logged day, an inline card breaking the selected day down by drink, a month summary (goal donut, average, total), and a slide-up panel for viewing/editing/adding entries
- **Goal ranges** — Set a daily goal with an optional upper bound for balanced hydration
- **Per-drink limits** — Configure maximum daily servings per drink type
- **Settings** — Configurable daily goal (single or range), serving sizes, daily limits, analytics start date
- **Dark mode** — Follows system setting with full Material 3 light/dark themes

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1.20 |
| UI | Jetpack Compose + Material 3 (BOM 2025.03.01) |
| Persistence | Room 2.7.0 (entries) + DataStore 1.1.4 (preferences) |
| DI | Hilt 2.55 |
| Navigation | Type-safe Compose Navigation 2.9.0-alpha04 (`@Serializable` routes) |
| Charts | Custom Canvas-based (no external chart library) |
| Dates | kotlinx-datetime 0.6.2 |
| Architecture | MVVM with repository layer |
| Build | Gradle 8.13, AGP 8.9.1 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## Build & Run

### Prerequisites

- JDK 17+
- Android SDK with API 35
- [just](https://github.com/casey/just) command runner (optional, or use the shell scripts)

### Commands

```bash
just build          # Debug APK (copied to clipboard on macOS)
just release        # Release APK (minified + shrunk, copied to clipboard)
just test           # Unit tests
just test-android   # Instrumented tests (needs emulator/device)
just install        # Build + install on connected device/emulator
just lint           # Lint check
just clean          # Clean build
just check          # Build + test + lint
just deploy         # Wireless ADB deploy
just connect        # Wireless ADB pairing
```

Shell scripts in `scripts/` are available as an alternative (`build.sh`, `test.sh`, `install.sh`, `lint.sh`, `clean.sh`).

### Run

1. Start an Android emulator (API 26+) or connect a device
2. Run `just install`
3. Open "Fluidis" from the app drawer

## Project Structure

```
app/src/main/java/com/fluidis/app/
├── FluidisApplication.kt              # @HiltAndroidApp entry point
├── MainActivity.kt                    # @AndroidEntryPoint, Compose setup
├── core/
│   ├── database/                      # Room DB, DAO (flow-based queries)
│   ├── datastore/                     # DataStore preferences wrapper
│   ├── model/                         # Entities, enums, data classes
│   ├── di/                            # Hilt modules (Database, DataStore)
│   ├── theme/                         # Material 3 theme, colors, typography
│   ├── navigation/                    # NavHost, @Serializable routes
│   └── ui/                            # Scaffold, constants, input validation
└── feature/
    ├── home/                          # Main tracking screen
    │   └── components/                # WaterDropIndicator, DrinkCard, CustomAmountDialog
    ├── statistics/                    # Charts and analytics
    │   └── components/                # TrendCard, IntakeChart, DrinkMixCard, WeekdayPatternCard
    ├── history/                       # Calendar and entry management
    │   └── components/                # CalendarView, DayDetailCard, MonthSummaryCard, DayDetailPanel
    └── settings/                      # App preferences
        └── components/                # GoalSettingDialog, ServingSizeSetting, MaxServingsDialog
```

## Defaults

| Setting | Default |
|---------|---------|
| Daily goal | 2000 ml |
| Water serving | 500 ml |
| Tea serving | 350 ml |
| Coffee serving | 350 ml |
| Max servings | Unlimited (0) |

## Extending

- **Add drink types**: Add entry to `DrinkType` enum — DB stores string keys, no migration needed
- **CSV export**: DAO has range queries; format and write via `MediaStore`
- **Backup/restore**: Copy Room `.db` file via `FileProvider`

## License

MIT
