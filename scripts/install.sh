#!/bin/bash
set -e
echo "=== Building and installing Fluidis on device ==="
echo "Note: Requires a running emulator or connected device"
./gradlew installDebug "$@"
echo "=== Installed successfully ==="
