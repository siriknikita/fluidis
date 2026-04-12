# Fluidis — task runner

# Default recipe: list available commands
default:
    @just --list

# Build debug APK (copies to clipboard on macOS)
build *args:
    #!/bin/bash
    set -e
    echo "=== Building Fluidis (debug) ==="
    ./gradlew assembleDebug {{args}}
    APK_PATH="$(pwd)/app/build/outputs/apk/debug/app-debug.apk"
    osascript -e "set the clipboard to POSIX file \"$APK_PATH\""
    echo "=== Build successful — APK copied to clipboard ==="

# Run unit tests
test *args:
    ./gradlew testDebugUnitTest {{args}}

# Run Android instrumented tests (requires emulator/device)
test-android *args:
    ./gradlew connectedDebugAndroidTest {{args}}

# Build and install on connected device/emulator
install *args:
    ./gradlew installDebug {{args}}

# Run lint check
lint *args:
    ./gradlew lintDebug {{args}}
    @echo "Report: app/build/reports/lint-results-debug.html"

# Clean build artifacts
clean *args:
    ./gradlew clean {{args}}

# Build release APK
release *args:
    ./gradlew assembleRelease {{args}}

# Connect to device over wireless ADB
connect *args:
    adeploy connect {{args}}

# Pair with device for wireless debugging (one-time setup)
pair addr code:
    adeploy pair {{addr}} {{code}}

# Build and install wirelessly (main dev command)
deploy *args:
    adeploy {{args}}

# Disconnect wireless ADB
disconnect:
    adeploy disconnect

# Run all checks (build + test + lint)
check: build test lint
