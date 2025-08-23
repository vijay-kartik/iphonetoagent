# 🚀 Google Cloud Run Deployment Guide

This guide will help you deploy your Kotlin API Gateway to Google Cloud Run for production use with iPhone Shortcuts integration.

## 📋 Prerequisites

1. **Google Cloud Account** with billing enabled
2. **Google Cloud CLI (gcloud)** installed
3. **Docker** installed and running
4. **Git** repository with your code

## 🔧 Setup Steps

### 1. Install Google Cloud CLI

```bash
# macOS (using Homebrew)
brew install google-cloud-sdk

# Or download from:
# https://cloud.google.com/sdk/docs/install
```

### 2. Authenticate with Google Cloud

```bash
gcloud auth login
gcloud auth application-default login
```

### 3. Create a Google Cloud Project

```bash
# Create new project (or use existing)
gcloud projects create your-project-id --name="Notion API Gateway"

# Set as default project
gcloud config set project your-project-id
```

### 4. Enable Required APIs

```bash
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
```

## 🐳 Docker Build & Test

### Test Docker Build Locally

```bash
# Build the image
docker build -t notion-api-gateway .

# Test locally
docker run -p 8080:8080 \
  -e API_KEY=your-api-key \
  -e NOTION_API_TOKEN=your-token \
  -e NOTION_DATABASE_ID=your-db-id \
  notion-api-gateway

# Test the API
curl http://localhost:8080/api/health
```

## 🚀 Deploy to Google Cloud Run

### Option 1: Use the Deployment Script (Recommended)

1. **Update the project ID** in `deploy-cloud-run.sh`:
   ```bash
   PROJECT_ID="your-actual-project-id"
   ```

2. **Run the deployment script**:
   ```bash
   ./deploy-cloud-run.sh
   ```

### Option 2: Manual Deployment

```bash
# 1. Build and tag the image
docker build -t gcr.io/YOUR_PROJECT_ID/notion-api-gateway .

# 2. Push to Google Container Registry
docker push gcr.io/YOUR_PROJECT_ID/notion-api-gateway

# 3. Deploy to Cloud Run
gcloud run deploy notion-api-gateway \
  --image gcr.io/YOUR_PROJECT_ID/notion-api-gateway \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --max-instances 10 \
  --min-instances 0 \
  --port 8080 \
  --timeout 300 \
  --concurrency 80
```

## 🔑 Set Environment Variables

### Option 1: Use the Environment Script

```bash
# Update PROJECT_ID in set-env-vars.sh first
./set-env-vars.sh
```

### Option 2: Manual Setup

```bash
gcloud run services update notion-api-gateway \
  --region=us-central1 \
  --set-env-vars="API_KEY=your-api-key,NOTION_API_TOKEN=your-token,NOTION_DATABASE_ID=your-db-id,PORT=8080"
```

## 📱 iPhone Shortcuts Integration

After deployment, you'll get a URL like:
```
https://notion-api-gateway-xxxxx-uc.a.run.app
```

Use this endpoint in your iPhone Shortcuts:
```
POST https://notion-api-gateway-xxxxx-uc.a.run.app/api/ingest
Headers:
  X-API-Key: your-api-key
  Content-Type: application/json

Body:
{
  "title": "Your Title",
  "content": "Your Content"
}
```

## 📊 Monitoring & Logs

### View Service Status
```bash
gcloud run services describe notion-api-gateway --region=us-central1
```

### View Logs
```bash
gcloud run services logs read notion-api-gateway --region=us-central1
```

### View Metrics
```bash
# Open in browser
gcloud run services logs tail notion-api-gateway --region=us-central1
```

## 💰 Cost Optimization

### Free Tier
- **2 million requests/month** - FREE
- **360,000 vCPU-seconds/month** - FREE
- **180,000 GiB-seconds/month** - FREE

### Cost Control
- **Min instances**: 0 (scales to zero)
- **Max instances**: 10 (prevents runaway costs)
- **Memory**: 512Mi (optimal for API gateway)
- **CPU**: 1 vCPU (sufficient for most loads)

## 🔒 Security Considerations

1. **API Key**: Use strong, unique API keys
2. **HTTPS**: Automatically enabled by Cloud Run
3. **Authentication**: Consider adding OAuth for production
4. **Rate Limiting**: Implement if needed for abuse prevention

## 🚨 Troubleshooting

### Common Issues

1. **Build fails**: Check Dockerfile and .dockerignore
2. **Deployment fails**: Verify gcloud authentication and project setup
3. **Environment variables not working**: Check the set-env-vars script
4. **Cold start delays**: Normal for serverless (1-2 seconds)

### Debug Commands

```bash
# Check service status
gcloud run services list --region=us-central1

# View recent logs
gcloud run services logs read notion-api-gateway --region=us-central1 --limit=50

# Test the deployed service
curl https://your-service-url/api/health
```

## 🎉 Success!

Once deployed, your API Gateway will:
- ✅ **Scale automatically** based on demand
- ✅ **Cost $0** for light usage (within free tier)
- ✅ **Be globally available** via Google's CDN
- ✅ **Handle iPhone Shortcuts** requests efficiently
- ✅ **Integrate with Notion** seamlessly

## 📞 Support

- **Google Cloud Run Docs**: https://cloud.google.com/run/docs
- **Ktor Documentation**: https://ktor.io/docs/
- **Notion API Docs**: https://developers.notion.com/
