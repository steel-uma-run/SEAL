#!/usr/bin/env bash

echo "==========TEST: FINALIZE SEASON==========="

echo "[*] Đang lấy token của Coordinator..."
TOKEN=$(./scripts/coord_login.sh | tr -d '\r')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "❌ Lỗi: Không lấy được token. Dừng test!"
    exit 1
fi

SEASON_ID="d15111a7-7410-4773-a19d-59574b54e267"

curl -s -X POST "http://localhost:8080/api/v0/seasons/${SEASON_ID}/finalize" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${TOKEN}" | jq .