#!/usr/bin/env sh

set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.4.1
WRAPPER_DIR="$APP_HOME/.gradle-wrapper"
GRADLE_HOME="$WRAPPER_DIR/gradle-$GRADLE_VERSION"
GRADLE_ZIP="$WRAPPER_DIR/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if ! command -v java >/dev/null 2>&1; then
    echo "Java runtime not found. Install JDK 25 or newer and run ./gradlew again." >&2
    exit 1
fi

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    mkdir -p "$WRAPPER_DIR"

    if [ ! -f "$GRADLE_ZIP" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl --fail --location --silent --show-error "$GRADLE_URL" --output "$GRADLE_ZIP"
        elif command -v wget >/dev/null 2>&1; then
            wget --quiet "$GRADLE_URL" -O "$GRADLE_ZIP"
        else
            echo "Neither curl nor wget is available to download Gradle." >&2
            exit 1
        fi
    fi

    rm -rf "$GRADLE_HOME"
    unzip -q -o "$GRADLE_ZIP" -d "$WRAPPER_DIR"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
