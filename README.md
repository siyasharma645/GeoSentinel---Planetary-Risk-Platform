# GeoSentinel — AI-Powered Planetary Risk Intelligence Platform

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend :3000                      │
│        (Location Search → Risk Report → AI Chat)            │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼──────────────────────────────────┐
│              API Gateway :8080                               │
│   JWT Auth Filter · Rate Limiting · CORS · Routing          │
└──┬──────────┬──────────┬──────────┬──────────┬─────────────┘
   │          │          │          │          │
:8081      :8082      :8083      :8084      :8085
Auth      Risk       Disaster   Climate    Alert
Service   Engine     Service    Service    Service
   │          │          │          │          │
   └──────────┴──────────┴──────────┴──────────┘
                          │
        ┌─────────────────┼──────────────────┐
        │                 │                  │
  PostgreSQL:5432    Redis:6379        Kafka:9092
  (primary store)   (cache+rate)    (event streaming)
```

## Quick Start

```bash
# 1. Clone and start everything
git clone <repo-url> && cd geosentinel
./scripts/start.sh

# 2. Open the app
open http://localhost:3000

# 3. Run API tests
./scripts/test-api.sh
```

## Default Credentials
- **Admin:** admin@geosentinel.io / Admin@12345

## Services

| Service | Port | Swagger UI |
|---------|------|-----------|
| Frontend | 3000 | — |
| API Gateway | 8080 | — |
| Auth Service | 8081 | http://localhost:8081/swagger-ui.html |
| Risk Engine | 8082 | http://localhost:8082/swagger-ui.html |
| Disaster Service | 8083 | http://localhost:8083/swagger-ui.html |
| Climate Service | 8084 | http://localhost:8084/swagger-ui.html |
| Alert Service | 8085 | http://localhost:8085/swagger-ui.html |

## API Examples

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Pass@1234","firstName":"Jane","lastName":"Doe","role":"RESEARCHER"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Pass@1234"}'
```

### Get Risk Report for Any Location
```bash
curl -X POST http://localhost:8080/api/v1/risk/report \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bangladesh","country":"Bangladesh","countryCode":"BD","lat":23.685,"lon":90.356}'
```

### Get Active Disasters
```bash
curl http://localhost:8080/api/v1/disasters/active \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Disasters Near a Location
```bash
curl "http://localhost:8080/api/v1/disasters/near?lat=23.685&lon=90.356&radiusKm=500" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Global Risk Overview
```bash
curl http://localhost:8080/api/v1/risk/overview \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Kafka Topics

| Topic | Producer | Consumer |
|-------|----------|----------|
| geosentinel.disaster.created | disaster-service | alert-service |
| geosentinel.disaster.updated | disaster-service | alert-service |
| geosentinel.disaster.escalated | disaster-service | alert-service |

## Real Data Ingestion
- **Earthquakes:** USGS FDSN API — auto-ingests M4.5+ every 5 minutes
- **Climate:** Open-Meteo API — temperature, humidity, precipitation every 15 minutes for 6 high-risk regions
- **Risk Scoring:** Multi-factor INFORM-methodology scoring using geographic + country-level data

## Kubernetes Deployment
```bash
kubectl apply -f k8s/
kubectl get all -n geosentinel
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DB_URL | jdbc:postgresql://localhost:5432/geosentinel | PostgreSQL |
| DB_USER | geosentinel | DB username |
| DB_PASSWORD | gs_secret | DB password |
| REDIS_HOST | localhost | Redis host |
| REDIS_PASSWORD | gs_redis | Redis password |
| KAFKA_BOOTSTRAP | localhost:9092 | Kafka brokers |
| JWT_SECRET | (required, 256+ bits) | JWT signing key |
