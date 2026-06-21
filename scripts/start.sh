#!/bin/bash
set -e
echo "Starting GeoSentinel..."
docker compose up -d --build
echo ""
echo "Waiting for services to be healthy..."
sleep 10
docker compose ps
echo ""
echo "GeoSentinel is running!"
echo "  Frontend:         http://localhost:3000"
echo "  API Gateway:      http://localhost:8080"
echo "  Auth Service:     http://localhost:8081/swagger-ui.html"
echo "  Risk Engine:      http://localhost:8082/swagger-ui.html"
echo "  Disaster Service: http://localhost:8083/swagger-ui.html"
echo "  Climate Service:  http://localhost:8084/swagger-ui.html"
echo "  Alert Service:    http://localhost:8085/swagger-ui.html"
echo ""
echo "Default admin: admin@geosentinel.io / Admin@12345"
