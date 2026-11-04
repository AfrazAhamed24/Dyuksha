#!/bin/sh
# Maven wrapper script (Unix/macOS)
# Generated for Dyuksha — do not edit manually.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_WRAPPER_JAR="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"

# Download wrapper jar if missing
if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  WRAPPER_URL=$(grep wrapperUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
  echo "Downloading Maven wrapper from $WRAPPER_URL"
  mkdir -p "$SCRIPT_DIR/.mvn/wrapper"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "Error: curl or wget is required to download the Maven wrapper." >&2
    exit 1
  fi
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"
