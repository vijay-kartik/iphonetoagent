# Kotlin API Gateway for Notion Integration

A Kotlin-based API Gateway that allows iPhone Shortcuts to send data to Notion via HTTP requests.

## Features

- **Secure API**: API key authentication for all requests
- **Notion Integration**: Automatically creates pages in your Notion database
- **iPhone Shortcuts Support**: Optimized for iPhone Shortcuts app
- **RESTful API**: Clean, simple HTTP endpoints
- **Comprehensive Logging**: Request/response logging for debugging
- **Error Handling**: Proper HTTP status codes and error messages

## API Endpoints

### POST /api/ingest
Sends data to Notion to create a new page.

**Headers:**
- `X-API-Key`: Your pre-shared API key
- `Content-Type`: `application/json`

**Request Body:**
```json
{
  "title": "Meeting Notes",
  "content": "Summary of today's meeting...",
  "metadata": {
    "source": "iPhoneShortcuts",
    "timestamp": "2025-08-23T14:15:00Z"
  }
}
```

**Response:**
```json
{
  "status": "success",
  "notion_page_id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "message": "Data successfully sent to Notion"
}
```

### GET /api/health
Health check endpoint to verify the service is running.

## Setup

### Prerequisites
- Java 21 or higher
- Gradle 9.0 or higher
- Notion account with integration token
- Notion database ID

### Environment Variables
Create a `.env` file or set these environment variables:

```bash
# API Security
export API_KEY="your-secure-api-key-here"

# Notion Configuration
export NOTION_API_TOKEN="your-notion-integration-token"
export NOTION_DATABASE_ID="your-notion-database-id"

# Server Configuration (optional)
export PORT=8080
```

### Running the Application

1. **Build the project:**
   ```bash
   ./gradlew build
   ```

2. **Run the application:**
   ```bash
   ./gradlew run
   ```

3. **Run tests:**
   ```bash
   ./gradlew test
   ```

The server will start on `http://localhost:8080`

## iPhone Shortcuts Setup

1. Open the Shortcuts app on your iPhone
2. Create a new shortcut
3. Add an "HTTP Request" action
4. Configure:
   - **URL**: `https://your-server.com/api/ingest`
   - **Method**: POST
   - **Headers**: 
     - `X-API-Key`: Your API key
     - `Content-Type`: `application/json`
   - **Body**: JSON with your data

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── org/example/
│   │       ├── Application.kt          # Main application entry point
│   │       ├── plugins/                # Ktor plugin configurations
│   │       ├── routes/                 # API route definitions
│   │       ├── services/               # Business logic services
│   │       └── types/                  # Data models and types
│   └── resources/
│       ├── application.conf            # Application configuration
│       └── logback.xml                 # Logging configuration
└── test/
    └── kotlin/
        └── org/example/
            └── ApiTest.kt              # API tests
```

## Configuration

The application uses TypeSafe Config for configuration management. Key configuration options:

- **Server**: Port, host, and deployment settings
- **Notion**: API token, database ID, and API version
- **Security**: API key for authentication
- **Logging**: Log levels and output configuration

## Security

- **API Key Authentication**: All requests require a valid API key
- **HTTPS Required**: Production deployments should use HTTPS
- **Input Validation**: All incoming data is validated
- **Error Handling**: Sensitive information is not exposed in error messages

## Logging

The application uses Logback for logging with:
- Console output for development
- File output for production (with log rotation)
- Structured logging for easy parsing
- Request/response logging for debugging

## Testing

Run the test suite with:
```bash
./gradlew test
```

Tests cover:
- API endpoint functionality
- Authentication validation
- Input validation
- Error handling

## Deployment

### Docker
```bash
./gradlew build
docker build -t notion-api-gateway .
docker run -p 8080:8080 notion-api-gateway
```

### Cloud Deployment
The application is designed to be deployed on:
- Google Cloud Platform
- Amazon Web Services
- Microsoft Azure
- Any container orchestration platform

## Troubleshooting

### Common Issues

1. **401 Unauthorized**: Check your API key in the `X-API-Key` header
2. **400 Bad Request**: Ensure title and content are not empty
3. **500 Internal Server Error**: Check Notion API token and database ID
4. **Connection Refused**: Verify the server is running and accessible

### Debug Mode
Enable debug logging by setting the log level to DEBUG in `logback.xml`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review the logs for error details
3. Open an issue on GitHub
