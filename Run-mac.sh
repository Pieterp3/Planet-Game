#!/bin/bash
# Run Planet Game on macOS

# Ensure Java 17+ is installed
JAVA_CMD="java"
JAR_FILE="Planet Conquest.jar"

# Check if JAR exists in current directory
if [ ! -f "$JAR_FILE" ]; then
  echo "Error: $JAR_FILE not found in current directory."
  exit 1
fi

# Run the game
$JAVA_CMD -jar "$JAR_FILE"
