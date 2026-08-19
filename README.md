# crop-dryer-monitor-api
Crop Dryer Monitor API is an IoT backend that receives and stores environmental data from a crop drying system built with an Arduino Uno R4 WiFi. The Arduino measures temperature, humidity, and water level, controls a fan and buzzer, and periodically sends all readings and alert states to this REST API

## What this API does

- **Receives readings** from Arduino Uno R4 WiFi via `POST /api/readings`.
- **Persists data** using Spring Data JPA with H2 in-memory DB by default (easily switchable to PostgreSQL).
- **Exposes endpoints** to fetch recent readings, the latest reading, and alert readings.
- **Validates input** with range constraints and returns consistent JSON responses.
- **CORS enabled** and optional **API key** protection for the POST endpoint.
- **Sends SMS alerts** via Twilio when readings have WARN or HIGH alarm status or when raining (optional).
- **Rate limiting** to prevent abuse (IP-based or API key-based).
- **API documentation** via Swagger/OpenAPI at `/swagger-ui.html`.
- **Structured logging** with Logback for development and production profiles.

## Tech stack

- **Spring Boot** 3.x
- **Spring Web (REST)**
- **Spring Data JPA**
- **H2** (default) or **PostgreSQL** (prod)
- **Twilio** (optional, for SMS alerts)
- **Bucket4j** (for rate limiting)
- **SpringDoc OpenAPI** (for API documentation)
- **Logback** (for structured logging)

## How to build and run

Prerequisites: Java 17+ and Maven 3.9+

- Run in dev with H2 (default):

```
mvn spring-boot:run
```

- Or build a jar and run:

```
mvn clean package
java -jar target/crop-dryer-monitor-api-0.0.1-SNAPSHOT.jar
```

The server starts on `http://localhost:8080`.

Access Swagger UI at `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## REST endpoints

- **POST** `/api/readings` — create a reading (Arduino sends here)
- **GET** `/api/readings` — list recent readings (supports `limit` and `deviceId`)
- **GET** `/api/readings/latest` — latest reading (optionally by `deviceId`)
- **GET** `/api/alerts` — readings where `alarm` is `WARN` or `HIGH` (optional `deviceId`)

## Example requests (curl)

- Create a reading:

```
curl -X POST http://localhost:8080/api/readings \
  -H "Content-Type: application/json" \
  -H "X-API-Key: mysecret" \
  -d '{
    "temperature": 27.4,
    "humidity": 62,
    "water": 352,
    "fan_on": true,
    "rain": 700,
    "rain_status": "DRY",
    "alarm": "WARN",
    "device_id": "dryer-1"
  }'
```

- Get recent readings (default limit 50):

```
curl http://localhost:8080/api/readings
```

- Get recent readings for a device with limit 10:

```
curl "http://localhost:8080/api/readings?limit=10&deviceId=dryer-1"
```

- Get the latest reading (all devices or by device):

```
curl http://localhost:8080/api/readings/latest
curl "http://localhost:8080/api/readings/latest?deviceId=dryer-1"
```

- Get alerts (WARN/HIGH):

```
curl http://localhost:8080/api/alerts
curl "http://localhost:8080/api/alerts?deviceId=dryer-1"
```

## Request/response format

POST body expected from Arduino:

```
{
  "temperature": 27.4,
  "humidity": 62,
  "water": 352,
  "fan_on": true,
  "rain": 700,
  "rain_status": "DRY",
  "alarm": "WARN",
  "device_id": "dryer-1"
}
```

Successful response example (201):

```
{
  "status": "ok",
  "data": {
    "id": 1,
    "created_at": "2024-06-01T12:00:00Z",
    "temperature": 27.4,
    "humidity": 62.0,
    "water": 352,
    "fan_on": true,
    "rain": 700,
    "rain_status": "DRY",
    "alarm": "WARN",
    "device_id": "dryer-1"
  }
}
```

Error response example (400):

```
{
  "status": "error",
  "message": "Description of what went wrong"
}
```

## Configuration

All configuration is in `src/main/resources`.

- Database (default H2): `application.properties`

```
spring.datasource.url=jdbc:h2:mem:cropdryer
spring.jpa.hibernate.ddl-auto=create-drop
```

- Switch to PostgreSQL for production: `application-prod.properties` and run with profile:

```
java -Dspring.profiles.active=prod -jar target/crop-dryer-monitor-api-0.0.1-SNAPSHOT.jar
```

`application-prod.properties` uses environment variables if present:

```
spring.datasource.url=${JDBC_DATABASE_URL:jdbc:postgresql://localhost:5432/cropdryer}
spring.datasource.username=${JDBC_DATABASE_USERNAME:postgres}
spring.datasource.password=${JDBC_DATABASE_PASSWORD:postgres}
```

- CORS allowed origins:

```
cropdryer.cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

- Optional API key for POST `/api/readings` (leave blank to disable):

```
cropdryer.api.key=mysecret
```

- SMS alerts via Twilio (optional):

```
cropdryer.sms.enabled=true
cropdryer.sms.twilio.account-sid=your_account_sid
cropdryer.sms.twilio.auth-token=your_auth_token
cropdryer.sms.twilio.from-number=+1234567890
cropdryer.sms.twilio.to-numbers=+1234567890,+0987654321
```

SMS alerts are sent automatically when a reading with `alarm` set to `WARN` or `HIGH` is created, or when `raining` is `true`. Multiple recipient numbers can be specified as a comma-separated list.

- Rate limiting (prevents abuse):

```
cropdryer.rate-limit.enabled=true
cropdryer.rate-limit.capacity=100
cropdryer.rate-limit.refill-tokens=10
cropdryer.rate-limit.refill-duration=1
```

Rate limiting uses IP-based limiting for requests without API key, and API key-based limiting for authenticated requests. The configuration uses a token bucket algorithm with the specified capacity and refill rate.

## Arduino Uno R4 WiFi HTTP POST example

HTTP request sent by the Arduino:

```
POST /api/readings HTTP/1.1
Host: <server-host>:8080
Content-Type: application/json
X-API-Key: mysecret

{
  "temperature": 27.4,
  "humidity": 62,
  "water": 352,
  "fan_on": true,
  "rain": 700,
  "rain_status": "DRY",
  "alarm": "WARN",
  "device_id": "dryer-1"
}
```

Replace `<server-host>` with the server IP or hostname reachable by your Arduino.
