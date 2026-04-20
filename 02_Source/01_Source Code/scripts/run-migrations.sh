#!/usr/bin/env bash
# Run RAG migrations (V002-V004) against a MySQL database.
#
# Usage:
#   DB_HOST=localhost DB_PORT=3306 DB_USER=root DB_PASS=secret DB_NAME=inventory_management \
#     bash run-migrations.sh
#
# Or put these in backend/.env and source it first.

set -euo pipefail

: "${DB_HOST:?DB_HOST not set}"
: "${DB_PORT:=3306}"
: "${DB_USER:?DB_USER not set}"
: "${DB_PASS:?DB_PASS not set}"
: "${DB_NAME:?DB_NAME not set}"

MIGRATIONS_DIR="$(cd "$(dirname "$0")/../database/migrations" && pwd)"

if ! command -v mysql >/dev/null 2>&1; then
  echo "ERROR: mysql client not found. Install mysql-client first." >&2
  exit 1
fi

run_sql() {
  local file="$1"
  echo ">> Running $(basename "$file")"
  mysql \
    --host="$DB_HOST" --port="$DB_PORT" \
    --user="$DB_USER" --password="$DB_PASS" \
    --ssl-mode=REQUIRED \
    "$DB_NAME" < "$file"
}

run_sql "$MIGRATIONS_DIR/V002__add_rag_sync_indexes.sql"
run_sql "$MIGRATIONS_DIR/V003__create_rag_system_tables.sql"
run_sql "$MIGRATIONS_DIR/V004__create_rag_chat_sessions.sql"

echo
echo ">> Verifying rag_sync_state exists..."
mysql \
  --host="$DB_HOST" --port="$DB_PORT" \
  --user="$DB_USER" --password="$DB_PASS" \
  --ssl-mode=REQUIRED \
  "$DB_NAME" -e "SELECT COUNT(*) AS sync_state_rows FROM rag_sync_state;"

echo ">> Verifying rag_chat_session exists..."
mysql \
  --host="$DB_HOST" --port="$DB_PORT" \
  --user="$DB_USER" --password="$DB_PASS" \
  --ssl-mode=REQUIRED \
  "$DB_NAME" -e "SELECT COUNT(*) AS session_rows FROM rag_chat_session;"

echo
echo "Migrations complete."
