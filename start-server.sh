#!/bin/bash

# Load environment variables from env.properties file
if [ -f "env.properties" ]; then
    echo "Loading environment variables from env.properties..."
    # Source the properties file directly
    set -a  # automatically export all variables
    source "env.properties"
    set +a
else
    echo "Warning: env.properties file not found. Using system environment variables."
fi

# Check if required environment variables are set
if [ -z "$API_KEY" ]; then
    echo "Error: API_KEY environment variable is not set"
    exit 1
fi

if [ -z "$NOTION_API_TOKEN" ]; then
    echo "Error: NOTION_API_TOKEN environment variable is not set"
    exit 1
fi

if [ -z "$NOTION_DATABASE_ID" ]; then
    echo "Error: NOTION_DATABASE_ID environment variable is not set"
    exit 1
fi

echo "Environment variables loaded successfully!"
echo "API_KEY: ${API_KEY:0:8}..."
echo "ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY:0:8}..."
echo "NOTION_API_TOKEN: ${NOTION_API_TOKEN:0:8}..."
echo "NOTION_DATABASE_ID: ${NOTION_DATABASE_ID:0:8}..."
echo "PORT: ${PORT:-8080}"

# Build and run the application
echo "Building and starting the server..."
./gradlew run
