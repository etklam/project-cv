#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/common.sh"

require_command mktemp

caprover_login
load_cluster_info

echo "Syncing app settings"
update_app_config "$(pdf_update_payload)"
update_app_config "$(backend_update_payload)"
update_app_config "$(frontend_update_payload)"

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "${WORK_DIR}"' EXIT

BACKEND_TAR="${WORK_DIR}/backend.tar"
PDF_TAR="${WORK_DIR}/pdf-renderer.tar"
FRONTEND_TAR="${WORK_DIR}/frontend.tar"
GIT_HASH="$(git_hash_or_fallback)"

echo "Packaging tarballs"
create_tarball "${REPO_ROOT}/backend" "${BACKEND_TAR}"
create_tarball "${REPO_ROOT}/services/pdf-renderer" "${PDF_TAR}"
create_tarball "${REPO_ROOT}/frontend" "${FRONTEND_TAR}"

echo "Submitting pdf renderer deploy"
deploy_tarball "${CAPROVER_PDF_APP}" "${PDF_TAR}" "${GIT_HASH}"

echo "Submitting backend deploy"
deploy_tarball "${CAPROVER_BACKEND_APP}" "${BACKEND_TAR}" "${GIT_HASH}"

echo "Submitting frontend deploy"
deploy_tarball "${CAPROVER_FRONTEND_APP}" "${FRONTEND_TAR}" "${GIT_HASH}"

echo "Deploys submitted in detached mode."
echo "Verify with ./scripts/caprover/verify.sh after the builds finish."
