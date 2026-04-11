#!/bin/bash
set -e
echo "=== Cleaning build ==="
./gradlew clean "$@"
echo "=== Clean complete ==="
