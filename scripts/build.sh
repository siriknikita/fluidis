#!/bin/bash
set -e
echo "=== Building Fluidis (debug) ==="
./gradlew assembleDebug "$@"
echo "=== Build successful ==="
