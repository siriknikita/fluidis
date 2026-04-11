# Fluidis

A personal hydration tracking Android app. Track water, tea, and coffee intake with daily goals, analytics, calendar history, and editable entries.

## Features

- **Quick tracking** — Tap to add default servings, long-press for custom amounts
- **Water drop progress** — Animated fill indicator with goal-reached celebration
- **Statistics** — Daily intake charts (bar, stacked, line, area), averages, drink breakdown
- **Calendar history** — Browse past days with color-coded goal indicators, edit/delete entries
- **Settings** — Configurable daily goal, serving sizes, analytics start date
- **Dark mode** — Follows system setting

## Tech Stack

- Kotlin, Jetpack Compose, Material 3
- Room (persistence), DataStore (preferences)
- Hilt (DI), type-safe Compose Navigation
- MVVM architecture with repository layer

## Build & Run (macOS)

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable) or just the command-line SDK tools
- JDK 17+
- Android SDK with API 35

### Build

```bash
./gradlew assembleDebug
```

Or use the convenience scripts:

```bash
./scripts/build.sh        # Debug build
./scripts/test.sh         # Unit tests
./scripts/install.sh      # Build + install on device/emulator
./scripts/lint.sh         # Lint check
./scripts/clean.sh        # Clean build
```

### Run

1. Start an Android emulator (API 26+) or connect a device
2. Run `./scripts/install.sh`
3. Open "Fluidis" from the app drawer

## Project Structure

```
app/src/main/java/com/fluidis/app/
├── core/          # Database, DataStore, DI, theme, navigation
└── feature/       # Home, Statistics, History, Settings screens
```

## Defaults

| Setting | Default |
|---------|---------|
| Daily goal | 2000 ml |
| Water serving | 500 ml |
| Tea serving | 350 ml |
| Coffee serving | 350 ml |

## Extending

- **Add drink types**: Add entry to `DrinkType` enum — DB stores string keys, no migration needed
- **CSV export**: DAO has range queries; format and write via `MediaStore`
- **Backup/restore**: Copy Room `.db` file via `FileProvider`

## License

MIT
