#!/bin/bash
set -e
echo "=== Running lint ==="
./gradlew lintDebug "$@"
echo "=== Lint complete ==="
echo "Report: app/build/reports/lint-results-debug.html"
