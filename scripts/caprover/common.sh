#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

ENV_FILE="${CAPROVER_ENV_FILE:-${REPO_ROOT}/.env.caprover.local}"
if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

: "${CAPROVER_URL:?CAPROVER_URL is required}"
: "${CAPROVER_PASSWORD:?CAPROVER_PASSWORD is required}"
: "${CAPROVER_POSTGRES_PASSWORD:?CAPROVER_POSTGRES_PASSWORD is required}"
: "${CAPROVER_JWT_SECRET:?CAPROVER_JWT_SECRET is required}"

CAPROVER_DB_APP="${CAPROVER_DB_APP:-project-cv-db}"
CAPROVER_BACKEND_APP="${CAPROVER_BACKEND_APP:-project-cv-backend}"
CAPROVER_PDF_APP="${CAPROVER_PDF_APP:-project-cv-pdf}"
CAPROVER_FRONTEND_APP="${CAPROVER_FRONTEND_APP:-project-cv-frontend}"
CAPROVER_POSTGRES_DB="${CAPROVER_POSTGRES_DB:-project_cv}"
CAPROVER_POSTGRES_USER="${CAPROVER_POSTGRES_USER:-postgres}"
CAPROVER_URL="${CAPROVER_URL%/}"

export CAPROVER_DB_APP
export CAPROVER_BACKEND_APP
export CAPROVER_PDF_APP
export CAPROVER_FRONTEND_APP
export CAPROVER_POSTGRES_DB
export CAPROVER_POSTGRES_USER
export CAPROVER_URL

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_command curl
require_command node
require_command tar

caprover_login() {
  CAPROVER_TOKEN="$(
    curl -fsS "${CAPROVER_URL}/api/v2/login" \
      -H 'x-namespace: captain' \
      -H 'content-type: application/json' \
      --data "$(node -e 'process.stdout.write(JSON.stringify({password: process.env.CAPROVER_PASSWORD}))')" |
      node -e 'let input=""; process.stdin.on("data", c => input += c); process.stdin.on("end", () => { const parsed = JSON.parse(input); if (!parsed.data || !parsed.data.token) process.exit(1); process.stdout.write(parsed.data.token); });'
  )"
  export CAPROVER_TOKEN
}

caprover_api() {
  local method="$1"
  local path="$2"
  local data="${3:-}"

  if [[ -n "${data}" ]]; then
    curl -fsS "${CAPROVER_URL}/api/v2${path}" \
      -X "${method}" \
      -H 'x-namespace: captain' \
      -H "x-captain-auth: ${CAPROVER_TOKEN}" \
      -H 'content-type: application/json' \
      --data "${data}"
  else
    curl -fsS "${CAPROVER_URL}/api/v2${path}" \
      -X "${method}" \
      -H 'x-namespace: captain' \
      -H "x-captain-auth: ${CAPROVER_TOKEN}"
  fi
}

load_cluster_info() {
  local info_json
  info_json="$(caprover_api GET /user/system/info)"
  CAPROVER_ROOT_DOMAIN="$(
    printf '%s' "${info_json}" |
      node -e 'let input=""; process.stdin.on("data", c => input += c); process.stdin.on("end", () => { const parsed = JSON.parse(input); const root = parsed.data && parsed.data.rootDomain; if (!root) process.exit(1); process.stdout.write(root); });'
  )"
  export CAPROVER_ROOT_DOMAIN
}

load_app_definitions() {
  APP_DEFINITIONS_JSON="$(caprover_api GET /user/apps/appDefinitions)"
  export APP_DEFINITIONS_JSON
}

app_exists() {
  local app_name="$1"
  APP_NAME="${app_name}" node -e '
    const payload = JSON.parse(process.env.APP_DEFINITIONS_JSON);
    const apps = payload.data && payload.data.appDefinitions ? payload.data.appDefinitions : [];
    process.exit(apps.some(app => app.appName === process.env.APP_NAME) ? 0 : 1);
  '
}

ensure_app() {
  local app_name="$1"
  local has_persistent_data="${2:-false}"

  if app_exists "${app_name}"; then
    echo "App exists: ${app_name}"
    return
  fi

  echo "Creating app: ${app_name}"
  caprover_api POST /user/apps/appDefinitions/register "$(
    APP_NAME="${app_name}" HAS_PERSISTENT_DATA="${has_persistent_data}" node -e '
      process.stdout.write(JSON.stringify({
        appName: process.env.APP_NAME,
        hasPersistentData: process.env.HAS_PERSISTENT_DATA === "true"
      }));
    '
  )" >/dev/null
}

update_app_config() {
  local payload="$1"
  caprover_api POST /user/apps/appDefinitions/update "${payload}" >/dev/null
}

enable_base_domain_ssl() {
  local app_name="$1"
  caprover_api POST /user/apps/appDefinitions/enablebasedomainssl "$(
    APP_NAME="${app_name}" node -e '
      process.stdout.write(JSON.stringify({ appName: process.env.APP_NAME }));
    '
  )" >/dev/null
}

deploy_captain_definition_content() {
  local app_name="$1"
  local captain_definition_json="$2"
  local git_hash="${3:-manual}"

  caprover_api POST "/user/apps/appData/${app_name}?detached=1" "$(
    APP_NAME="${app_name}" GIT_HASH="${git_hash}" CAPTAIN_DEFINITION_JSON="${captain_definition_json}" node -e '
      process.stdout.write(JSON.stringify({
        captainDefinitionContent: process.env.CAPTAIN_DEFINITION_JSON,
        gitHash: process.env.GIT_HASH
      }));
    '
  )" >/dev/null
}

create_tarball() {
  local source_dir="$1"
  local output_file="$2"
  tar -C "${source_dir}" -cf "${output_file}" .
}

deploy_tarball() {
  local app_name="$1"
  local tar_file="$2"
  local git_hash="${3:-manual}"

  curl -fsS "${CAPROVER_URL}/api/v2/user/apps/appData/${app_name}?detached=1" \
    -H 'x-namespace: captain' \
    -H "x-captain-auth: ${CAPROVER_TOKEN}" \
    -F "sourceFile=@${tar_file}" \
    -F "gitHash=${git_hash}" >/dev/null
}

git_hash_or_fallback() {
  if command -v git >/dev/null 2>&1; then
    git -C "${REPO_ROOT}" rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S
    return
  fi

  date +%Y%m%d%H%M%S
}

backend_public_url() {
  printf 'http://%s.%s' "${CAPROVER_BACKEND_APP}" "${CAPROVER_ROOT_DOMAIN}"
}

frontend_public_url() {
  printf 'https://%s.%s' "${CAPROVER_FRONTEND_APP}" "${CAPROVER_ROOT_DOMAIN}"
}

db_captain_definition_json() {
  node -e '
    const definition = {
      schemaVersion: 2,
      dockerCompose: {
        services: {
          postgres: {
            image: "postgres:16-alpine",
            volumes: [
              {
                source: "postgres-data",
                target: "/var/lib/postgresql/data",
                type: "volume"
              }
            ],
            environment: {
              POSTGRES_DB: process.env.CAPROVER_POSTGRES_DB,
              POSTGRES_USER: process.env.CAPROVER_POSTGRES_USER,
              POSTGRES_PASSWORD: process.env.CAPROVER_POSTGRES_PASSWORD
            },
            healthcheck: {
              test: ["CMD-SHELL", `pg_isready -U ${process.env.CAPROVER_POSTGRES_USER} -d ${process.env.CAPROVER_POSTGRES_DB}`],
              interval: "5s",
              timeout: "5s",
              retries: 20
            }
          }
        },
        volumes: {
          "postgres-data": {}
        }
      }
    };
    process.stdout.write(JSON.stringify(definition));
  '
}

db_update_payload() {
  node -e '
    process.stdout.write(JSON.stringify({
      appName: process.env.CAPROVER_DB_APP,
      notExposeAsWebApp: true,
      instanceCount: 1
    }));
  '
}

pdf_update_payload() {
  node -e '
    process.stdout.write(JSON.stringify({
      appName: process.env.CAPROVER_PDF_APP,
      notExposeAsWebApp: true,
      containerHttpPort: 3100,
      instanceCount: 1,
      envVars: [{ key: "PORT", value: "3100" }]
    }));
  '
}

backend_update_payload() {
  local frontend_url
  frontend_url="$(frontend_public_url)"
  FRONTEND_URL="${frontend_url}" node -e '
    process.stdout.write(JSON.stringify({
      appName: process.env.CAPROVER_BACKEND_APP,
      containerHttpPort: 8080,
      instanceCount: 1,
      envVars: [
        { key: "SPRING_DATASOURCE_URL", value: `jdbc:postgresql://srv-captain--${process.env.CAPROVER_DB_APP}:5432/${process.env.CAPROVER_POSTGRES_DB}` },
        { key: "SPRING_DATASOURCE_USERNAME", value: process.env.CAPROVER_POSTGRES_USER },
        { key: "SPRING_DATASOURCE_PASSWORD", value: process.env.CAPROVER_POSTGRES_PASSWORD },
        { key: "JWT_SECRET", value: process.env.CAPROVER_JWT_SECRET },
        { key: "APP_UPLOAD_PATH", value: "/app/data/uploads" },
        { key: "APP_EXPORT_RENDERER_BASE_URL", value: `http://srv-captain--${process.env.CAPROVER_PDF_APP}:3100` },
        { key: "APP_EXPORT_FRONTEND_BASE_URL", value: process.env.FRONTEND_URL }
      ]
    }));
  '
}

frontend_update_payload() {
  node -e '
    process.stdout.write(JSON.stringify({
      appName: process.env.CAPROVER_FRONTEND_APP,
      containerHttpPort: 80,
      instanceCount: 1
    }));
  '
}
