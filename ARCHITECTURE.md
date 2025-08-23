# API Architecture Documentation

## Overview

This Kotlin API Gateway has been refactored to follow clean architecture principles with proper separation of concerns. The codebase is now organized into layers for better maintainability, testability, and scalability.

## Architecture Layers

### 1. **Routes Layer** (`/routes/`)
- **`ApiRoutes.kt`** - Main entry point that delegates to route configuration
- **`config/RouteConfiguration.kt`** - Organizes all route definitions
- **`handlers/`** - Individual route handlers for each endpoint

### 2. **Business Service Layer** (`/services/business/`)
- **`IngestService.kt`** - Core business logic for content ingestion
- Contains the main logic for deciding whether to append or create pages

### 3. **External Service Layer** (`/services/`)
- **`notion/NotionService.kt`** - Notion API integration
- **`auth/AuthService.kt`** - Authentication and authorization
- **`config/ConfigService.kt`** - Configuration management

### 4. **Types Layer** (`/types/`)
- Data classes for requests, responses, and DTOs
- Serializable objects for API communication

## Route Handlers

### IngestRouteHandler
- Handles `/api/ingest` POST requests
- Validates API keys and request format
- Delegates business logic to `IngestService`
- Returns appropriate success/error responses

### HealthRouteHandler
- Handles `/api/health` GET requests  
- Simple health check endpoint
- Returns system status and timestamp

## Business Logic Flow

```
API Request → Route Handler → Business Service → External Service → Response
     ↓              ↓              ↓                  ↓              ↓
IngestRequest → IngestRouteHandler → IngestService → NotionService → IngestResponse
```

### Content Processing Logic:
1. **Search**: Check if page with title exists in Notion
2. **Decision**: 
   - If exists → Append content to existing page
   - If not exists → Create new page
3. **Response**: Return page ID and action taken

## Benefits of This Architecture

### 🔧 **Maintainability**
- Each component has a single responsibility
- Easy to locate and modify specific functionality
- Clear separation between routing, business logic, and external services

### 🧪 **Testability** 
- Business logic isolated in service classes
- Route handlers can be tested independently
- Mock services for unit testing

### 📈 **Scalability**
- Easy to add new endpoints without modifying existing code
- Business services can be reused across different routes
- Configuration centralized for easy management

### 🎯 **Readability**
- Clear naming conventions and structure
- Documented classes and methods
- Logical file organization

## File Structure

```
app/src/main/kotlin/org/example/
├── routes/
│   ├── ApiRoutes.kt                 # Main route entry point
│   ├── config/
│   │   └── RouteConfiguration.kt    # Route organization
│   └── handlers/
│       ├── IngestRouteHandler.kt    # Ingest endpoint logic
│       └── HealthRouteHandler.kt    # Health check logic
├── services/
│   ├── business/
│   │   └── IngestService.kt         # Core business logic
│   ├── auth/
│   │   └── AuthService.kt           # Authentication
│   ├── config/
│   │   └── ConfigService.kt         # Configuration
│   └── notion/
│       └── NotionService.kt         # Notion API client
└── types/
    └── types.kt                     # Data classes and DTOs
```

## Adding New Endpoints

To add a new endpoint:

1. **Create Handler**: Add new handler in `/routes/handlers/`
2. **Add Route**: Register route in `RouteConfiguration.kt`
3. **Business Logic**: Create service in `/services/business/` if needed
4. **Types**: Add request/response types in `types.kt`

Example:
```kotlin
// 1. Create AnalyticsRouteHandler.kt
class AnalyticsRouteHandler {
    suspend fun handleAnalyticsRequest(call: ApplicationCall) { ... }
}

// 2. Add to RouteConfiguration.kt
private fun Route.configureAnalyticsRoutes() {
    val handler = AnalyticsRouteHandler()
    get("/analytics") { handler.handleAnalyticsRequest(call) }
}
```

## Error Handling

- **Validation Errors**: Return 400 Bad Request
- **Authentication Errors**: Handled by AuthService
- **Business Logic Errors**: Return 500 Internal Server Error
- **Notion API Errors**: Propagated from NotionService

## Logging

- Each layer has its own logger
- Request/response logging in handlers
- Business logic logging in services
- Error logging with stack traces

This architecture provides a solid foundation for growing the API while maintaining code quality and developer productivity.