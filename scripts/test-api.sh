#!/bin/bash
set -e
BASE="http://localhost:8080/api/v1"

echo "=== GeoSentinel API Test ==="

echo "1. Register user..."
curl -s -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"analyst@test.com","password":"Test@1234","firstName":"Test","lastName":"Analyst","role":"RESEARCHER"}' | python3 -m json.tool

echo "2. Login..."
RESP=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"analyst@test.com","password":"Test@1234"}')
TOKEN=$(echo $RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "Token obtained: ${TOKEN:0:40}..."

echo "3. Get risk report for Bangladesh..."
curl -s -X POST $BASE/risk/report \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bangladesh","country":"Bangladesh","countryCode":"BD","lat":23.685,"lon":90.356}' | python3 -m json.tool

echo "4. Get global overview..."
curl -s $BASE/risk/overview -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo "5. Get active disasters..."
curl -s $BASE/disasters/active -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo "6. Get active alerts..."
curl -s $BASE/alerts -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo "7. Get climate summary..."
curl -s $BASE/climate/summary -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo ""
echo "=== All tests complete ==="
