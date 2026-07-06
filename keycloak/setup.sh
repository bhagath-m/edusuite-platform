#!/usr/bin/env bash
set -euo pipefail

# Start Keycloak with the EduSuite realm imported.
# This script aligns with the keycloak service in docker-compose.yml:
#   image: quay.io/keycloak/keycloak:25.0
#   command: start-dev --import-realm
#   admin: admin / admin
#   port: 8081 -> 8080

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
CONTAINER_NAME="edusuite-keycloak"
ADMIN_URL="http://localhost:8081"

check_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "Error: docker is not installed or not on PATH." >&2
    exit 1
  fi

  if ! docker info >/dev/null 2>&1; then
    echo "Error: docker daemon is not running or you lack permissions." >&2
    exit 1
  fi
}

start_keycloak() {
  echo "Starting Keycloak container '${CONTAINER_NAME}'..."

  # Prefer docker compose so the service stays consistent with docker-compose.yml.
  if docker compose version >/dev/null 2>&1 || docker-compose version >/dev/null 2>&1; then
    cd "${PROJECT_DIR}"
    docker compose up -d keycloak || docker-compose up -d keycloak
  else
    # Fallback to docker run when neither docker compose plugin nor legacy CLI is available.
    if docker ps -a --format '{{.Names}}' | grep -qx "${CONTAINER_NAME}"; then
      echo "Removing existing '${CONTAINER_NAME}' container..."
      docker rm -f "${CONTAINER_NAME}" >/dev/null
    fi

    docker run -d \
      --name "${CONTAINER_NAME}" \
      -p 8081:8080 \
      -e KEYCLOAK_ADMIN=admin \
      -e KEYCLOAK_ADMIN_PASSWORD=admin \
      -v "${PROJECT_DIR}/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json" \
      quay.io/keycloak/keycloak:25.0 \
      start-dev --import-realm
  fi
}

wait_for_keycloak() {
  echo -n "Waiting for Keycloak admin console at ${ADMIN_URL}"
  local attempts=0
  local max_attempts=60

  while [[ ${attempts} -lt ${max_attempts} ]]; do
    if curl -sf "${ADMIN_URL}" >/dev/null 2>&1; then
      echo
      echo "Keycloak is ready."
      return 0
    fi
    echo -n "."
    sleep 2
    attempts=$((attempts + 1))
  done

  echo
  echo "Error: Keycloak did not become ready within ~$((max_attempts * 2)) seconds." >&2
  echo "Check logs with: docker logs ${CONTAINER_NAME}" >&2
  return 1
}

print_usage() {
  cat <<EOF

EduSuite Keycloak is running.

  Admin console: ${ADMIN_URL}/admin
  Realm:         edusuite
  Admin user:    admin
  Admin pass:    admin

Client configuration:
  clientId: edusuite-app
  publicClient: true
  redirectUris: http://localhost:5173/*, http://localhost:8080/*

Useful commands:
  docker logs -f ${CONTAINER_NAME}
  docker stop ${CONTAINER_NAME}
  docker start ${CONTAINER_NAME}

EOF
}

main() {
  check_docker
  start_keycloak
  wait_for_keycloak
  print_usage
}

main "$@"
