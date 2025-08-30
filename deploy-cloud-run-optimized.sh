#!/bin/bash

# Optimized Google Cloud Run Deployment Script
# This script uses advanced caching techniques to reduce build times significantly

set -e

# Configuration - Update these values
PROJECT_ID="iphonetoagent"
REGION="asia-south2"
SERVICE_NAME="notion-api-gateway"
IMAGE_NAME="gcr.io/$PROJECT_ID/$SERVICE_NAME"

echo "🚀 Starting optimized Google Cloud Run deployment..."

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

# Enable required APIs (run once)
echo "🔧 Ensuring required APIs are enabled..."
gcloud services enable cloudbuild.googleapis.com containerregistry.googleapis.com run.googleapis.com --project=$PROJECT_ID

# Method 1: Use Cloud Build with caching configuration
echo "🏗️  Building with Google Cloud Build (optimized with caching)..."

# Check if cloudbuild.yaml exists, if not create a basic one
if [ ! -f "cloudbuild.yaml" ]; then
    echo "📝 Creating cloudbuild.yaml for optimized builds..."
    cat > cloudbuild.yaml << 'EOF'
steps:
  # Build with layer caching
  - name: 'gcr.io/cloud-builders/docker'
    args:
      - 'build'
      - '-f'
      - 'Dockerfile.optimized'
      - '--cache-from'
      - 'gcr.io/$PROJECT_ID/notion-api-gateway:latest'
      - '-t'
      - 'gcr.io/$PROJECT_ID/notion-api-gateway:$BUILD_ID'
      - '-t' 
      - 'gcr.io/$PROJECT_ID/notion-api-gateway:latest'
      - '.'

options:
  machineType: 'E2_HIGHCPU_8'
  diskSizeGb: 100
  diskType: 'SSD'

images:
  - 'gcr.io/$PROJECT_ID/notion-api-gateway:$BUILD_ID'
  - 'gcr.io/$PROJECT_ID/notion-api-gateway:latest'

timeout: '1200s'
EOF
fi

# Submit build with configuration file
gcloud builds submit --config=cloudbuild.yaml --project=$PROJECT_ID

# Get the latest build ID for deployment
BUILD_ID=$(gcloud builds list --limit=1 --format='value(id)' --project=$PROJECT_ID)
VERSIONED_IMAGE="$IMAGE_NAME:$BUILD_ID"

echo "🚀 Deploying to Google Cloud Run with image: $VERSIONED_IMAGE"

# Deploy to Cloud Run with the versioned image
gcloud run deploy $SERVICE_NAME \
    --image $VERSIONED_IMAGE \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --memory 512Mi \
    --cpu 1 \
    --max-instances 10 \
    --min-instances 0 \
    --port 8080 \
    --timeout 300 \
    --concurrency 80 \
    --project=$PROJECT_ID

# Get the service URL
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME --region=$REGION --format='value(status.url)' --project=$PROJECT_ID)

echo ""
echo "🎉 Optimized deployment successful!"
echo "🌐 Service URL: $SERVICE_URL"
echo ""
echo "📱 For iPhone Shortcuts, use this endpoint:"
echo "   $SERVICE_URL/api/ingest"
echo ""
echo "💡 Build optimizations applied:"
echo "   ✅ Docker layer caching enabled"
echo "   ✅ Gradle build cache enabled"  
echo "   ✅ High-CPU build machine (E2_HIGHCPU_8)"
echo "   ✅ SSD storage for faster I/O"
echo "   ✅ Parallel Gradle builds"
echo ""
echo "🔑 Don't forget to set environment variables in Cloud Run:"
echo "   - API_KEY"
echo "   - NOTION_API_TOKEN" 
echo "   - NOTION_DATABASE_ID"
echo ""
echo "💡 To set environment variables, run:"
echo "   gcloud run services update $SERVICE_NAME --region=$REGION --project=$PROJECT_ID --set-env-vars=API_KEY=your-api-key,NOTION_API_TOKEN=your-token,NOTION_DATABASE_ID=your-db-id"
echo ""
echo "📊 To view logs:"
echo "   gcloud run services logs read $SERVICE_NAME --region=$REGION --project=$PROJECT_ID"
echo ""
echo "⏱️  Next deployments will be significantly faster due to caching!"