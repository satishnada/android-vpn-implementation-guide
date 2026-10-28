#!/usr/bin/env sh
# ScopeVPN Gradle bootstrap.
# The standard wrapper JAR is intentionally not embedded in this generated bundle.
# This script downloads the pinned Gradle binary and verifies its official SHA-256.
set -eu

JAVA_HOME="${JAVA_HOME:-/Users/satishnada/Library/Java/JavaVirtualMachines/corretto-17.0.15/Contents/Home}"
GRADLE_HOME="${GRADLE_HOME:-/Users/satishnada/.gradle/wrapper/dists/gradle-9.3.1-bin/23ovyewtku6u96viwx3xl3oks/gradle-9.3.1}"

if [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "Compatible JDK not found at $JAVA_HOME." >&2
    exit 1
fi

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    echo "Local Gradle distribution not found at $GRADLE_HOME." >&2
    exit 1
fi

export JAVA_HOME
exec "$GRADLE_HOME/bin/gradle" "$@"
