#!/usr/bin/env bash
# Smoke-test the RAG chat endpoint with a handful of Vietnamese questions.
#
# Usage:
#   API_BASE=http://localhost:8080/api/v1 JWT=ey... bash smoke-test-rag.sh
#
# Obtain JWT either via the frontend (DevTools -> Network -> copy Authorization)
# or via Keycloak password grant:
#
#   curl -s -X POST "${KEYCLOAK_URL}/realms/ims/protocol/openid-connect/token" \
#     -d "client_id=ims-frontend" -d "grant_type=password" \
#     -d "username=..." -d "password=..." | jq -r .access_token

set -euo pipefail

: "${API_BASE:?API_BASE not set (e.g. http://localhost:8080/api/v1)}"
: "${JWT:?JWT not set}"

QUESTIONS=(
  "Còn bao nhiêu lô vật tư API còn hạn?"
  "Lô có ID LOT-001 hiện ở trạng thái nào?"
  "QC test nào fail trong tháng gần đây?"
  "Batch số PB-2026-04 dùng nguyên liệu nào?"
  "Vật tư nào sắp hết hạn trong 30 ngày tới?"
)

for q in "${QUESTIONS[@]}"; do
  echo "────────────────────────────────────────────"
  echo "Q: $q"
  start_ms=$(date +%s%3N)
  response=$(curl -sS -X POST "${API_BASE}/rag/chat" \
    -H "Authorization: Bearer ${JWT}" \
    -H "Content-Type: application/json" \
    -d "$(printf '{"question": %s}' "$(printf '%s' "$q" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))')")")
  end_ms=$(date +%s%3N)
  latency=$((end_ms - start_ms))
  echo "Latency: ${latency}ms"
  echo "Response:"
  echo "$response" | python3 -m json.tool || echo "$response"
  echo
done
