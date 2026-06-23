#!/usr/bin/env bash

echo "=========================================="
echo "    BẮT ĐẦU CHẠY BỘ TEST API CHO TRACK    "
echo "=========================================="

#thay id vo nha
SEASON_ID="d15111a7-7410-4773-a19d-59574b54e267"
EVENT_ID="5cd3aa28-9ef6-48c0-850d-c9780b667f54"
TRACK_ID="1ca8729b-27f7-4090-a0bd-5394969f2fe6"
MENTOR_ID="d4d5d42b-0d70-40cf-aa7e-2d694a2d4e1e"
TEAM_ID="123e4567-e89b-12d3-a456-426614174004"

# cho nay no tu lay token
echo "[*] Đang lấy token của Coordinator..."
TOKEN=$(./scripts/coord_login.sh | tr -d '\r')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "❌ Lỗi: Không lấy được token. Dừng test!"
    exit 1
fi
echo "✅ Lấy token thành công!"

#echo "=== 1. TEST: GET ALL TRACKS OF EVENT ==="
#curl -s -X GET "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks" \
#     -H "Accept: application/json" | jq .
#
#echo "=== 2. TEST: GET TRACK DETAIL ==="
#curl -s -X GET "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks/${TRACK_ID}" \
#     -H "Accept: application/json" | jq .
#
#echo "=== 3. TEST: CREATE TRACK ==="
#curl -s -X POST "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks" \
#     -H "Content-Type: application/json" \
#     -H "Authorization: Bearer ${TOKEN}" \
#     -d '{"name": "Software Engineering", "description": "Software Engineering test track"}' | jq .
#
#echo "=== 4. TEST: ASSIGN MENTOR ==="
#curl -s -X POST "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks/${TRACK_ID}/assign-mentor" \
#     -H "Content-Type: application/json" \
#     -H "Authorization: Bearer ${TOKEN}" \
#     -d '{
#           "mentor_id": "'"${MENTOR_ID}"'"
#         }' | jq .
#
#echo "=== 5. TEST: ASSIGN TEAM ==="
#curl -s -X POST "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks/${TRACK_ID}/assign-team" \
#     -H "Content-Type: application/json" \
#     -H "Authorization: Bearer ${TOKEN}" \
#     -d '{
#           "team_id": "'"${TEAM_ID}"'"
#         }' | jq .

echo "=== 6. TEST: UPDATE TRACK ==="
echo "------------------------------------------"
echo "CASE 1: Cập nhật CẢ tên lẫn mô tả"
echo "------------------------------------------"
curl -s -X PATCH "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks/${TRACK_ID}" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${TOKEN}" \
     -d '{
           "name": "Software Engineering 2.0",
           "description": "hihihihihihihihihihihi"
         }' | jq .

echo "------------------------------------------"
echo "CASE 2: Chỉ cập nhật MỖI TÊN"
echo "------------------------------------------"
curl -s -X PATCH "http://localhost:8080/api/v0/seasons/${SEASON_ID}/events/${EVENT_ID}/tracks/${TRACK_ID}" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${TOKEN}" \
     -d '{
           "name": "Software Engineering 3.0"
         }' | jq .