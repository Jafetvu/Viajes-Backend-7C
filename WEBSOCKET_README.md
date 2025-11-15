# WebSocket Implementation Guide

## Overview

This backend implements a WebSocket system with WSS (WebSocket Secure) support for real-time communication between clients and drivers. The system includes:

- **Real-time trip updates**: Drivers and clients receive instant notifications about trip status changes
- **In-app notifications**: Users receive notifications with different types (INFO, WARN, ERROR, OK)
- **JWT Authentication**: WebSocket connections are secured with JWT tokens
- **Dual confirmation system**: Both driver and client must confirm trip start and completion

## Architecture

### Components

1. **WebSocket Configuration** (`WebSocketConfig.java`)
   - STOMP protocol over WebSocket
   - JWT authentication via handshake interceptor
   - SockJS fallback support
   - Supports both WS and WSS protocols

2. **WebSocket Controllers**
   - `TripWebSocketController`: Broadcasts trip updates
   - `NotificationWebSocketController`: Sends user notifications

3. **Notification System**
   - `Notification` entity: Stores notifications in database
   - `NotificationService`: Manages notification creation and WebSocket delivery
   - `NotificationController`: REST API for notification management

4. **Trip Status Flow**
   ```
   REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED
                ↓
            CANCELLED
   ```

## WebSocket Endpoints

### Connection Endpoint

```
ws://localhost:8080/ws?token=YOUR_JWT_TOKEN
```

For production with SSL:
```
wss://your-domain.com/ws?token=YOUR_JWT_TOKEN
```

### Subscription Topics

#### 1. New Trip Requests (Drivers only)
```
/topic/trips/new
```
All drivers receive notifications when a new trip is requested.

**Message Format:**
```json
{
  "tripId": 1,
  "status": "REQUESTED",
  "clientId": 123,
  "driverId": null,
  "origin": "Location A",
  "destination": "Location B",
  "fare": 50.0,
  "driverCompleted": false,
  "clientCompleted": false,
  "updatedAt": "2025-01-14T10:30:00",
  "message": "New trip request available"
}
```

#### 2. Personal Trip Updates
```
/user/queue/trips
```
Receive updates about your specific trips (both drivers and clients).

**Message Format:** Same as above

#### 3. Personal Notifications
```
/user/queue/notifications
```
Receive in-app notifications.

**Message Format:**
```json
{
  "id": 1,
  "userId": 123,
  "type": "INFO",
  "title": "Trip Accepted",
  "body": "A driver has accepted your trip request!",
  "createdAt": "14/01/2025 - 10:30",
  "isRead": false,
  "tripId": 1
}
```

#### 4. System-wide Notifications
```
/topic/notifications/system
```
Broadcast notifications for all users (maintenance, announcements, etc.).

## REST API Endpoints

### Notifications

#### Get All User Notifications
```
GET /api/notifications
Authorization: Bearer {token}
```

#### Get Unread Notifications
```
GET /api/notifications/unread
Authorization: Bearer {token}
```

#### Get Unread Count
```
GET /api/notifications/unread/count
Authorization: Bearer {token}
```

Response:
```json
{
  "count": 5
}
```

#### Mark as Read
```
PUT /api/notifications/{notificationId}/read
Authorization: Bearer {token}
```

#### Mark All as Read
```
PUT /api/notifications/read-all
Authorization: Bearer {token}
```

#### Delete Notification
```
DELETE /api/notifications/{notificationId}
Authorization: Bearer {token}
```

## SSL/TLS Configuration for WSS

### For Development (Self-Signed Certificate)

1. **Generate a self-signed certificate using keytool:**

```bash
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650 \
  -dname "CN=localhost, OU=Development, O=Viajes, L=City, ST=State, C=MX"
```

When prompted, enter a password (e.g., `changeit`)

2. **Place the keystore file:**
   - Move `keystore.p12` to `src/main/resources/`

3. **Update application.properties:**

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
server.port=8443
```

4. **Access your application:**
   - HTTPS: `https://localhost:8443`
   - WSS: `wss://localhost:8443/ws?token=YOUR_JWT_TOKEN`

**Note:** Browsers will show a security warning for self-signed certificates. This is normal for development.

### For Production (Let's Encrypt or Commercial Certificate)

1. **Obtain an SSL certificate** from Let's Encrypt or a commercial CA

2. **Convert certificate to PKCS12 format** (if needed):

```bash
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem \
  -out keystore.p12 -name tomcat
```

3. **Update application.properties with production certificate:**

```properties
server.ssl.enabled=true
server.ssl.key-store=file:/path/to/keystore.p12
server.ssl.key-store-password=your-secure-password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
server.port=8443
```

4. **Optional: Use environment variables for sensitive data:**

```properties
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
```

## Frontend Integration Example

### Using STOMP.js and SockJS

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Get JWT token from your auth system
const token = localStorage.getItem('jwt_token');

// Create WebSocket connection
const socket = new SockJS(`https://your-domain.com/ws?token=${token}`);
const stompClient = Stomp.over(socket);

// Connect to WebSocket
stompClient.connect({}, (frame) => {
  console.log('Connected: ' + frame);

  // Subscribe to personal notifications
  stompClient.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    console.log('New notification:', notification);
    // Update UI with notification
    showNotification(notification);
  });

  // Subscribe to personal trip updates
  stompClient.subscribe('/user/queue/trips', (message) => {
    const tripUpdate = JSON.parse(message.body);
    console.log('Trip update:', tripUpdate);
    // Update trip status in UI
    updateTripStatus(tripUpdate);
  });

  // For drivers: subscribe to new trip requests
  if (userRole === 'DRIVER') {
    stompClient.subscribe('/topic/trips/new', (message) => {
      const newTrip = JSON.parse(message.body);
      console.log('New trip available:', newTrip);
      // Show new trip request in driver UI
      showNewTripRequest(newTrip);
    });
  }
}, (error) => {
  console.error('WebSocket connection error:', error);
});

// Disconnect when component unmounts
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  console.log('Disconnected');
}
```

### React/Next.js Hook Example

```typescript
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export function useWebSocket(token: string) {
  const [stompClient, setStompClient] = useState<Client | null>(null);
  const [notifications, setNotifications] = useState([]);
  const [tripUpdates, setTripUpdates] = useState(null);

  useEffect(() => {
    const socket = new SockJS(`${process.env.NEXT_PUBLIC_WS_URL}?token=${token}`);
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('WebSocket Connected');

      // Subscribe to notifications
      client.subscribe('/user/queue/notifications', (message) => {
        const notification = JSON.parse(message.body);
        setNotifications((prev) => [notification, ...prev]);
      });

      // Subscribe to trip updates
      client.subscribe('/user/queue/trips', (message) => {
        const tripUpdate = JSON.parse(message.body);
        setTripUpdates(tripUpdate);
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
    };

    client.activate();
    setStompClient(client);

    return () => {
      client.deactivate();
    };
  }, [token]);

  return { notifications, tripUpdates };
}
```

## Notification Types

- **INFO**: General information notifications
- **WARN**: Warnings or alerts that need attention
- **ERROR**: Error notifications for failed operations
- **OK**: Success notifications for completed actions

## Trip State Transitions & Notifications

### 1. Client Requests Trip
- **State**: `REQUESTED`
- **Notification**: Sent to client (OK type)
- **WebSocket**: Broadcasted to all drivers on `/topic/trips/new`

### 2. Driver Accepts Trip
- **State**: `ACCEPTED`
- **Notification**: Sent to client (OK type)
- **WebSocket**: Sent to client on `/user/queue/trips`

### 3. Driver or Client Starts Trip
- **State**: `IN_PROGRESS`
- **Notification**: Sent to both parties (INFO type)
- **WebSocket**: Sent to both on `/user/queue/trips`

### 4. Both Confirm Completion
- **State**: `COMPLETED`
- **Notification**: Sent to both parties (OK type)
- **WebSocket**: Sent to both on `/user/queue/trips`

### 5. Trip Cancelled
- **State**: `CANCELLED`
- **Notification**: Sent to affected party (WARN type)
- **WebSocket**: Sent to affected party

## Security Considerations

1. **JWT Authentication**: All WebSocket connections must include a valid JWT token
2. **User Isolation**: Users can only receive notifications and trip updates meant for them
3. **HTTPS Required in Production**: Always use WSS (WebSocket Secure) in production
4. **Token Expiration**: Handle token refresh for long-lived WebSocket connections

## Testing WebSocket Connections

### Using Browser Console

```javascript
const socket = new WebSocket('ws://localhost:8080/ws?token=YOUR_JWT_TOKEN');
socket.onmessage = (event) => console.log('Received:', event.data);
socket.onopen = () => console.log('Connected');
socket.onerror = (error) => console.error('Error:', error);
```

### Using Postman or wscat

```bash
# Install wscat
npm install -g wscat

# Connect to WebSocket
wscat -c "ws://localhost:8080/ws?token=YOUR_JWT_TOKEN"
```

## Troubleshooting

### Connection Refused
- Check that the server is running on port 8080 (or 8443 for SSL)
- Verify firewall settings
- Check that WebSocket dependency is in pom.xml

### 401 Unauthorized
- Verify JWT token is valid and not expired
- Check that token is correctly passed in URL query parameter
- Ensure token format is: `?token=eyJhbGc...`

### SSL Certificate Errors
- For development: Accept the self-signed certificate in your browser
- For production: Ensure certificate is valid and not expired
- Check that certificate paths in application.properties are correct

### Messages Not Received
- Verify you're subscribed to the correct topic
- Check browser console for JavaScript errors
- Ensure STOMP connection is established before subscribing

## Database Schema

### Notification Table
```sql
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    title VARCHAR(100) NOT NULL,
    body VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME,
    trip_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (trip_id) REFERENCES trip(id)
);
```

## Performance Considerations

- **In-Memory Broker**: The current implementation uses Spring's simple in-memory message broker, suitable for university projects and small deployments
- **Scalability**: For production with multiple instances, consider using an external message broker like RabbitMQ or Redis
- **Database Queries**: Notifications are indexed by user_id for fast retrieval
- **WebSocket Connections**: Each connection is lightweight; Spring handles connection pooling

## Future Enhancements

- Message acknowledgment and delivery confirmation
- Offline message queuing
- Push notifications for mobile apps
- Message history and pagination
- Read receipts
- Typing indicators for chat functionality

## Support

For issues or questions about WebSocket implementation, please review the code in:
- `websocket/` package for WebSocket configuration
- `modules/notification/` package for notification logic
- `modules/trip/TripService.java` for trip state management
