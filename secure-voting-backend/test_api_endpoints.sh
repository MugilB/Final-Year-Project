#!/bin/bash

# Test script for API endpoints
echo "Testing API endpoints for voting chart..."

BASE_URL="http://localhost:8081/api"

echo "1. Testing basic connection..."
curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/elections" && echo " - Elections endpoint OK" || echo " - Elections endpoint FAILED"

echo "2. Testing candidates endpoint..."
curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/candidates" && echo " - All candidates endpoint OK" || echo " - All candidates endpoint FAILED"

echo "3. Testing candidates for election 1..."
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/candidates/election/1")
echo "$RESPONSE_CODE - Candidates for election 1"

if [ "$RESPONSE_CODE" = "200" ]; then
    echo "✅ SUCCESS: Candidates endpoint working"
    echo "Response:"
    curl -s "$BASE_URL/candidates/election/1" | head -c 200
    echo "..."
else
    echo "❌ ERROR: Candidates endpoint failed with code $RESPONSE_CODE"
    echo "Full response:"
    curl -s "$BASE_URL/candidates/election/1"
fi

echo ""
echo "4. Testing vote counts endpoint..."
VOTE_RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/candidates/election/1/vote-counts")
echo "$VOTE_RESPONSE_CODE - Vote counts for election 1"

if [ "$VOTE_RESPONSE_CODE" = "200" ]; then
    echo "✅ SUCCESS: Vote counts endpoint working"
    echo "Response:"
    curl -s "$BASE_URL/candidates/election/1/vote-counts"
else
    echo "❌ ERROR: Vote counts endpoint failed with code $VOTE_RESPONSE_CODE"
    echo "Full response:"
    curl -s "$BASE_URL/candidates/election/1/vote-counts"
fi

echo ""
echo "5. Testing approved candidates endpoint..."
APPROVED_RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/candidates/election/1/approved")
echo "$APPROVED_RESPONSE_CODE - Approved candidates for election 1"

if [ "$APPROVED_RESPONSE_CODE" = "200" ]; then
    echo "✅ SUCCESS: Approved candidates endpoint working"
    echo "Response:"
    curl -s "$BASE_URL/candidates/election/1/approved" | head -c 200
    echo "..."
else
    echo "❌ ERROR: Approved candidates endpoint failed with code $APPROVED_RESPONSE_CODE"
    echo "Full response:"
    curl -s "$BASE_URL/candidates/election/1/approved"
fi

echo ""
echo "=== SUMMARY ==="
echo "If all endpoints return 200, the API is working correctly."
echo "If any endpoint returns 500, check the backend server logs for errors."
