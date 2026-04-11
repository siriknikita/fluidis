# Fluidis — Claude Code Project Guide

## What is this?

Fluidis is a personal hydration tracking Android app. It tracks water, tea, and coffee intake per day with daily goals, analytics, calendar history, and editable entries. Local-only — no backend, no auth, no sync.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Persistence**: Room (entries) + DataStore (preferences/settings)
- **DI**: Hilt
- **Navigation**: Type-safe Compose Navigation 2.9.0-alpha04 (`@Serializable` routes)
- **Charts**: Custom Canvas-based (no external chart library)
- **Dates**: kotlinx-datetime
- **Architecture**: MVVM with repository layer
- **Build**: Gradle 8.13, AGP 8.9.1, Kotlin 2.1.20, Compose BOM 2025.03.01
- **Min SDK**: 26 (Android 8.0) — gives full `java.time` without desugaring

## Build Commands

```bash
just build        # Debug build (APK copied to clipboard)
just test         # Unit tests
just test-android # Instrumented tests (needs emulator)
just install      # Build + install on device/emulator
just lint         # Lint check
just clean        # Clean build
just check        # Build + test + lint
```

Shell scripts are also available in `scripts/` as an alternative.

## Project Structure

```
app/src/main/java/com/fluidis/app/
├── FluidisApplication.kt          # @HiltAndroidApp entry point
├── MainActivity.kt                # @AndroidEntryPoint, sets up Compose
├── core/
│   ├── database/                  # Room DB, DAO
│   ├── datastore/                 # DataStore preferences wrapper
│   ├── model/                     # Entities, enums, data classes
│   ├── di/                        # Hilt modules
│   ├── theme/                     # Material 3 theme (fixed blue palette)
│   ├── navigation/                # NavHost, Routes
│   └── ui/                        # Shared scaffold
└── feature/
    ├── home/                      # Main tracking screen
    │   └── components/            # DrinkCard, DailySummary, CustomAmountDialog
    ├── statistics/                # Charts and analytics
    │   └── components/            # IntakeChart, AveragesCard, DrinkBreakdownCard
    ├── history/                   # Calendar and entry editing
    │   └── components/            # CalendarView, DayDetailSheet, EditEntryDialog
    └── settings/                  # App settings
        └── components/            # GoalSetting, ServingSizeSetting
```

## Architecture Conventions

- **One UiState per screen**: sealed interface with `Loading` and `Success` variants
- **StateFlow in ViewModel**: combined from DAO flows + settings, collected via `collectAsStateWithLifecycle()`
- **Events (one-shots)**: `Channel` → `Flow` pattern for snackbars, navigation events
- **DrinkType enum**: central source of drink metadata (key, display name, icon, color, default serving)
- **DB stores string keys** for drink types (not enum ordinals) — extensible without migration
- **Dates stored as ISO strings** in Room (e.g., "2026-04-11") — simple range queries

## Key Design Decisions

- **Navigation**: 3 bottom tabs (Home, Statistics, History) + Settings via top bar gear icon
- **Theme**: Fixed blue/water palette (#1976D2 primary), dark mode follows system
- **Progress**: Water drop fill indicator with smooth animation, gold/green overflow on goal met
- **Minus button**: Undo-style — removes last entry, shows snackbar with UNDO
- **Custom amount**: Long-press + opens dialog with number input
- **Charts**: Single chart area with dropdown to switch 4 modes (bar, stacked bar, line, area)
- **Calendar**: Color-coded dots (green = goal met, orange = below goal)
- **Analytics start date**: Auto-detected from first entry, overridable in settings

## Data Model

### Room Entity: `drink_entries`
- `id: Long` (PK, auto)
- `drinkType: String` ("water" / "tea" / "coffee")
- `amountMl: Int`
- `date: String` (ISO LocalDate, indexed)
- `createdAt: Long` (epoch millis)

### DataStore Keys
- `daily_goal_ml` (default: 2000)
- `water_serving_ml` (default: 500)
- `tea_serving_ml` (default: 350)
- `coffee_serving_ml` (default: 350)
- `analytics_start_date` (auto from first entry)
- `selected_chart_mode` (persisted)

## Defaults

- Daily goal: 2000 ml
- Water serving: 500 ml
- Tea serving: 350 ml
- Coffee serving: 350 ml

## Development Workflow

- Build after each important change to catch errors early
- Make granular, atomic commits — one logical unit per commit
- No commit approval needed — just commit when a unit is done
- Run `./scripts/build.sh` to verify compilation
- Run `./scripts/test.sh` to run unit tests

## Non-Goals (this version)

- No backend / cloud sync
- No authentication
- No push notifications / reminders
- No home screen widgets
- No streaks / badges / gamification
- No onboarding flow
