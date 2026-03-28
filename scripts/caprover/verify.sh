#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

caprover_login
load_cluster_info

BACKEND_URL="$(backend_public_url)"
FRONTEND_URL="$(frontend_public_url)"

echo "Checking backend health: ${BACKEND_URL}/actuator/health"
curl -fsS "${BACKEND_URL}/actuator/health"
echo

echo "Checking frontend API proxy: ${FRONTEND_URL}/api/v1/templates"
curl -fsS "${FRONTEND_URL}/api/v1/templates"
echo
