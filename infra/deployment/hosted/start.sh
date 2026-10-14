#!/bin/bash
set -euo pipefail
# The ingress serves HTTPS. API and frontend share one origin and cookie scope.
java -jar /app/api.jar --server.address=127.0.0.1 --server.port=8081 &
api_pid=$!
caddy run --config /app/Caddyfile --adapter caddyfile &
proxy_pid=$!
cleanup() {
    kill "$api_pid" "$proxy_pid" 2>/dev/null || true
    wait "$api_pid" "$proxy_pid" 2>/dev/null || true
}
trap cleanup EXIT
trap 'exit 143' TERM
trap 'exit 130' INT
set +e
wait -n "$api_pid" "$proxy_pid"
status=$?
set -e
exit "$status"
