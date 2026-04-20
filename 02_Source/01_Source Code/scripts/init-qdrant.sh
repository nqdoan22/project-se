#!/usr/bin/env bash
# Create the Qdrant collection `ims_rag` (1536-dim, cosine) and payload indexes.
#
# Usage:
#   QDRANT_HOST=xxx.cloud.qdrant.io QDRANT_API_KEY=xxx bash init-qdrant.sh
#
# Re-running is safe: uses PUT which is idempotent; payload indexes return 4xx if
# already present and the script continues.

set -euo pipefail

: "${QDRANT_HOST:?QDRANT_HOST not set}"
: "${QDRANT_API_KEY:?QDRANT_API_KEY not set}"
: "${QDRANT_COLLECTION:=ims_rag}"
: "${QDRANT_REST_PORT:=6333}"

BASE_URL="https://${QDRANT_HOST}:${QDRANT_REST_PORT}"
AUTH="api-key: ${QDRANT_API_KEY}"

echo ">> Creating collection ${QDRANT_COLLECTION} (1536-dim, Cosine)..."
curl -sS -X PUT "${BASE_URL}/collections/${QDRANT_COLLECTION}" \
  -H "${AUTH}" \
  -H "Content-Type: application/json" \
  -d '{
    "vectors": { "size": 1536, "distance": "Cosine" },
    "optimizers_config": { "default_segment_number": 2 }
  }' | sed 's/^/   /'
echo

create_index() {
  local field="$1"
  local schema_type="$2"
  echo ">> Payload index on ${field} (${schema_type})..."
  curl -sS -X PUT "${BASE_URL}/collections/${QDRANT_COLLECTION}/index" \
    -H "${AUTH}" \
    -H "Content-Type: application/json" \
    -d "{\"field_name\": \"${field}\", \"field_schema\": \"${schema_type}\"}" \
    | sed 's/^/   /' || true
  echo
}

create_index "source_table"             "keyword"
create_index "source_pk"                "keyword"
create_index "lot_status"               "keyword"
create_index "result_status"            "keyword"
create_index "material_part_number"     "keyword"
create_index "material_id"              "keyword"
create_index "batch_status"             "keyword"
create_index "transaction_type"         "keyword"

echo
echo ">> Collection info:"
curl -sS -H "${AUTH}" "${BASE_URL}/collections/${QDRANT_COLLECTION}" | sed 's/^/   /'
echo
echo
echo "Qdrant init complete."
