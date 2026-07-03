#!/bin/bash
#
# Publish a GitHub release for Fluidis.
#
# Builds the release + debug APKs, tags the current commit, and creates a
# GitHub release with both APKs attached. Release notes are generated from
# the commits since the previous tag.
#
# Usage:
#   ./scripts/release.sh              # version read from app/build.gradle.kts
#   ./scripts/release.sh 1.2.0        # override the version
#   ./scripts/release.sh --draft      # create the release as a draft
#
# Requirements: gh (authenticated), a git remote named "origin".

set -euo pipefail

cd "$(dirname "$0")/.."

DRAFT=""
VERSION=""
for arg in "$@"; do
    case "$arg" in
        --draft) DRAFT="--draft" ;;
        *)       VERSION="$arg" ;;
    esac
done

# — Version ————————————————————————————————————————
if [ -z "$VERSION" ]; then
    VERSION="$(grep -m1 'versionName' app/build.gradle.kts | sed -E 's/.*"([^"]+)".*/\1/')"
fi
if [ -z "$VERSION" ]; then
    echo "ERROR: could not determine version. Pass it explicitly: ./scripts/release.sh 1.0.0" >&2
    exit 1
fi
TAG="v$VERSION"
echo "=== Releasing Fluidis $TAG ==="

# — Preflight ——————————————————————————————————————
command -v gh >/dev/null || { echo "ERROR: gh CLI not found. Install it: https://cli.github.com" >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "ERROR: gh is not authenticated. Run: gh auth login" >&2; exit 1; }

if [ -n "$(git status --porcelain)" ]; then
    echo "ERROR: working tree is dirty. Commit or stash changes before releasing." >&2
    exit 1
fi

if git rev-parse "$TAG" >/dev/null 2>&1 || gh release view "$TAG" >/dev/null 2>&1; then
    echo "ERROR: $TAG already exists (as a git tag or GitHub release)." >&2
    exit 1
fi

# The gradle-wrapper.jar is gitignored; regenerate it if missing.
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "--- gradle-wrapper.jar missing, regenerating ---"
    command -v gradle >/dev/null || { echo "ERROR: gradle-wrapper.jar is missing and system gradle is not installed." >&2; exit 1; }
    gradle wrapper --gradle-version 8.13
fi

# — Build ——————————————————————————————————————————
echo "--- Building debug + release APKs ---"
./gradlew assembleDebug assembleRelease

RELEASE_APK="app/build/outputs/apk/release/Fluidis-release.apk"
DEBUG_APK="app/build/outputs/apk/debug/Fluidis-debug.apk"
[ -f "$RELEASE_APK" ] || { echo "ERROR: $RELEASE_APK not found after build." >&2; exit 1; }
[ -f "$DEBUG_APK" ]   || { echo "ERROR: $DEBUG_APK not found after build." >&2; exit 1; }

# — Release notes ——————————————————————————————————
PREV_TAG="$(git describe --tags --abbrev=0 2>/dev/null || true)"
NOTES_FILE="$(mktemp)"
trap 'rm -f "$NOTES_FILE"' EXIT
{
    echo "## Changes"
    echo
    if [ -n "$PREV_TAG" ]; then
        git log --no-merges --pretty='- %s' "$PREV_TAG..HEAD"
    else
        git log --no-merges --pretty='- %s'
    fi
    echo
    echo "## Install"
    echo
    echo "Download **Fluidis-release.apk** (recommended, R8-optimized) and install on Android 8.0+ (min SDK 26). **Fluidis-debug.apk** is attached for debugging."
    echo
    echo "> Both APKs are signed with a debug keystore; Android may warn about installing from an unknown source."
} > "$NOTES_FILE"

# — Tag + publish ——————————————————————————————————
echo "--- Tagging $TAG ---"
git tag -a "$TAG" -m "Fluidis $TAG"
git push origin "$TAG"

# --latest only applies to a published release, not a draft.
LATEST="--latest"
[ -n "$DRAFT" ] && LATEST=""

echo "--- Creating GitHub release ---"
gh release create "$TAG" $DRAFT $LATEST \
    --title "Fluidis $TAG" \
    --notes-file "$NOTES_FILE" \
    "$RELEASE_APK#Fluidis-release.apk (recommended)" \
    "$DEBUG_APK#Fluidis-debug.apk"

echo "=== Released Fluidis $TAG ==="
gh release view "$TAG" --json url --jq '.url'
