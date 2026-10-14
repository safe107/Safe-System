#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
docker compose version >/dev/null
docker info >/dev/null
mkdir -p .local
if [ ! -f .local/dev.env ]; then
    umask 077
    db_password=$(od -An -N24 -tx1 /dev/urandom | tr -d ' \n')
    admin_password=$(od -An -N24 -tx1 /dev/urandom | tr -d ' \n')
    printf 'DATABASE_PASSWORD=%s\nADMIN_USERNAME=local-admin\nADMIN_PASSWORD=%s\nSAFE_PRODUCTION=false\nCOOKIE_SECURE=false\n' "$db_password" "$admin_password" > .local/dev.env
    unset db_password admin_password
fi
docker compose -p safe-system-local --env-file .local/dev.env -f compose.local.yml up --build -d --wait --wait-timeout 180
printf '\nSAFE System: http://localhost:%s/pt-BR\nPainel: http://localhost:%s/admin/login\nCredenciais: arquivo .local/dev.env (nao compartilhe).\n' "${SAFE_LOCAL_PORT:-5173}" "${SAFE_LOCAL_PORT:-5173}"
