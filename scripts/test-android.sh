#!/bin/bash
set -e
echo "=== Running Android instrumented tests ==="
echo "Note: Requires a running emulator or connected device"
./gradlew connectedDebugAndroidTest "$@"
echo "=== Android tests passed ==="
