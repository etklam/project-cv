#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

caprover_login
load_cluster_info
load_app_definitions

ensure_app "${CAPROVER_DB_APP}" false
ensure_app "${CAPROVER_PDF_APP}" false
ensure_app "${CAPROVER_BACKEND_APP}" false
ensure_app "${CAPROVER_FRONTEND_APP}" false

echo "Syncing app settings"
update_app_config "$(db_update_payload)"
update_app_config "$(pdf_update_payload)"
update_app_config "$(backend_update_payload)"
update_app_config "$(frontend_update_payload)"

echo "Deploying PostgreSQL definition"
deploy_captain_definition_content "${CAPROVER_DB_APP}" "$(db_captain_definition_json)" "$(git_hash_or_fallback)"

echo "Enabling SSL for ${CAPROVER_FRONTEND_APP}.${CAPROVER_ROOT_DOMAIN}"
enable_base_domain_ssl "${CAPROVER_FRONTEND_APP}" || true

echo "Bootstrap submitted."
echo "Next step: run ./scripts/caprover/deploy.sh"
