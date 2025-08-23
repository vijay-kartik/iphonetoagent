#!/bin/bash

# Google Cloud Run Deployment Script
# This script builds, pushes, and deploys your Kotlin API Gateway to Google Cloud Run

set -e

# Configuration - Update these values
PROJECT_ID="iphonetoagent"
REGION="asia-south2"
SERVICE_NAME="notion-api-gateway"
IMAGE_NAME="gcr.io/$PROJECT_ID/$SERVICE_NAME"

echo "🚀 Starting Google Cloud Run deployment..."

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Error: gcloud CLI is not installed. Please install it first:"
    echo "   https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if docker is running
if ! docker info &> /dev/null; then
    echo "❌ Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Build and push image using Google Cloud Build
echo "🏗️  Building and pushing image using Google Cloud Build..."
gcloud builds submit --tag $IMAGE_NAME .

# Deploy to Cloud Run
echo "🚀 Deploying to Google Cloud Run..."
gcloud run deploy $SERVICE_NAME \
    --image $IMAGE_NAME \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --memory 512Mi \
    --cpu 1 \
    --max-instances 10 \
    --min-instances 0 \
    --port 8080 \
    --timeout 300 \
    --concurrency 80

# Get the service URL
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME --region=$REGION --format='value(status.url)')

echo ""
echo "🎉 Deployment successful!"
echo "🌐 Service URL: $SERVICE_URL"
echo ""
echo "📱 For iPhone Shortcuts, use this endpoint:"
echo "   $SERVICE_URL/api/ingest"
echo ""
echo "🔑 Don't forget to set environment variables in Cloud Run:"
echo "   - API_KEY"
echo "   - NOTION_API_TOKEN"
echo "   - NOTION_DATABASE_ID"
echo ""
echo "💡 To set environment variables, run:"
echo "   gcloud run services update $SERVICE_NAME --region=$REGION --set-env-vars=API_KEY=your-api-key,NOTION_API_TOKEN=your-token,NOTION_DATABASE_ID=your-db-id"
echo ""
echo "📊 To view logs:"
echo "   gcloud run services logs read $SERVICE_NAME --region=$REGION"
