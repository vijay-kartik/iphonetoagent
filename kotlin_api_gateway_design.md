
# API DESIGN DOCUMENT

## Project Title  
**Kotlin API Gateway for Notion Integration via iPhone Shortcuts**

---

## 1. Overview

This gateway server will:
- Accept HTTP requests from iPhone Shortcuts with text or document data.
- Process the received data as required (e.g., transform, validate).
- Forward the processed data to Notion using the Notion API to create/update pages.
- Return a status or result to the iPhone client.

---

## 2. Architecture

- **Clients:**  
iPhone/iPad running Shortcuts app, triggers Shortcut sending HTTP request to API Gateway.

- **API Gateway:**  
Kotlin backend (Spring Boot/Ktor recommended), authenticates and validates incoming requests, handles processing logic, communicates with Notion API.

- **External:**  
Notion API (REST, authorized via Notion integration token).

---

## 3. Authentication & Security

- Each request from iPhone should contain a pre-shared API Key in a header (e.g., `X-API-Key`).
- API should validate this key before processing.
- HTTPS is mandatory.

---

## 4. API Endpoints

### 4.1 Receive Data Endpoint

- **URL:** `/api/ingest`
- **Method:** `POST`
- **Authentication:** API Key header
- **Content-Type:** `application/json` or `multipart/form-data` (if supporting files)

#### Request (for text):

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

#### Request (for file):

- Form-Data:
  - `file`: (binary document)
  - `title`: "Document Name"
  - `metadata`: (optional JSON string)

#### Response:

- `200 OK` on successful forward to Notion.
- `400 Bad Request` on validation failure.
- `401 Unauthorized` if API key missing/invalid.
- `500 Internal Server Error` on processing error.

##### Example Success Response

```json
{
  "status": "success",
  "notion_page_id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "message": "Data successfully sent to Notion."
}
```

---

## 5. Processing Logic

- Validate the payload.
- Optionally transform/summarize/clean up text.
- Prepare a Notion page or database item per requirements.
- Call Notion API `/v1/pages` (or other) using integration token.
- Log the request, response, and errors for auditing.

---

## 6. Notion API Integration

- Uses Notion “integration” token, stored securely (env vars or secret manager).
- Properly map incoming data to Notion page/database schema.
- Handle Notion API errors, with retries/appropriate messaging.

---

## 7. Error Handling

- Return informative error messages to client (iPhone).
- Log all failures with details.
- Support idempotency if required (request IDs).

---

## 8. Example iPhone Shortcut Flow

1. User triggers shortcut, entering or sharing content.
2. Shortcut executes HTTP POST to `/api/ingest` endpoint, passing the data & API key.
3. Gateway validates/processes, sends to Notion, and returns result.

---

## 9. Example Sequence Diagram Sketch

```
iPhone           API Gateway           Notion API
  |     POST          |                       |
  |------------------>|                       |
  |                   |   POST /v1/pages      |
  |                   |---------------------->|
  |                   |        response       |
  |                   |<--------------------- |
  |   response        |                       |
  |<------------------|                       |
```

---

## 10. Technology Stack

- **Backend:** Kotlin (Spring Boot or Ktor preferred)
- **HTTP Client:** Ktor, OkHttp, or Spring WebClient for Notion
- **Testing:** JUnit/Testcontainers
- **Deployment:** Docker-ready, deploy on cloud VM or container service (GCP, AWS, etc.)

---

## 11. Further Enhancements (Optional)

- OAuth for Notion on behalf of user.
- End-to-end encryption of sensitive data.
- Webhook for notifying iPhone of Notion updates.

---

## Appendix

- Notion API docs
- Example payload mapping
- Troubleshooting FAQs

---
