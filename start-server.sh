#!/bin/bash

# Load environment variables from env.properties file
if [ -f "env.properties" ]; then
    echo "Loading environment variables from env.properties..."
    # Read each line, skip comments, and export variables
    while IFS= read -r line; do
        # Skip empty lines and comments
        if [[ ! -z "$line" && ! "$line" =~ ^[[:space:]]*# ]]; then
            export "$line"
        fi
    done < "env.properties"
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
echo "NOTION_API_TOKEN: ${NOTION_API_TOKEN:0:8}..."
echo "NOTION_DATABASE_ID: ${NOTION_DATABASE_ID:0:8}..."
echo "PORT: ${PORT:-8080}"

# Build and run the application
echo "Building and starting the server..."
./gradlew run
