#!/bin/bash
set -e
echo "=== Running unit tests ==="
./gradlew testDebugUnitTest "$@"
echo "=== Unit tests passed ==="
