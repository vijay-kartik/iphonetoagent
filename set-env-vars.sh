#!/bin/bash

# Set Environment Variables for Google Cloud Run
# Run this after deploying to set your API keys and configuration

set -e

# Configuration - Update these values
PROJECT_ID="your-google-cloud-project-id"
REGION="us-central1"
SERVICE_NAME="notion-api-gateway"

# Load environment variables from env.properties
if [ -f "env.properties" ]; then
    echo "📋 Loading environment variables from env.properties..."
    source env.properties
    
    # Set environment variables in Cloud Run
    echo "🔑 Setting environment variables in Cloud Run..."
    gcloud run services update $SERVICE_NAME \
        --region=$REGION \
        --set-env-vars="API_KEY=$API_KEY,NOTION_API_TOKEN=$NOTION_API_TOKEN,NOTION_DATABASE_ID=$NOTION_DATABASE_ID,PORT=8080"
    
    echo "✅ Environment variables set successfully!"
    echo ""
    echo "📋 Current environment variables:"
    gcloud run services describe $SERVICE_NAME --region=$REGION --format='value(spec.template.spec.containers[0].env[].name,spec.template.spec.containers[0].env[].value)'
    
else
    echo "❌ Error: env.properties file not found!"
    echo "Please create env.properties with your configuration first."
    exit 1
fi

