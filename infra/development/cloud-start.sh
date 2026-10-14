#!/usr/bin/env bash
set -euo pipefail
cd /workspace/Safe-System
python3 infra/development/init-local.py
set -a
source .local/dev.env
set +a
if [ "${SAFE_PRODUCTION:-false}" != "false" ]; then
  echo "cloud-start is exclusively for development; production configuration is preserved." >&2
  exit 1
fi
if docker container inspect safe-postgres >/dev/null 2>&1; then
  docker start safe-postgres >/dev/null
else
  export POSTGRES_PASSWORD="$DATABASE_PASSWORD"
  docker run -d --name safe-postgres -e POSTGRES_DB=safe -e POSTGRES_USER=safe -e POSTGRES_PASSWORD -p 127.0.0.1:5432:5432 postgres:17 >/dev/null
fi
for attempt in $(seq 1 40); do
  if docker exec safe-postgres pg_isready -U safe -d safe >/dev/null 2>&1; then break; fi
  sleep 1
done
docker exec safe-postgres pg_isready -U safe -d safe
python3 - <<'PYDB'
import subprocess
result=subprocess.run(['docker','exec','safe-postgres','psql','-U','safe','-d','safe','-tAc',"SELECT 1 FROM pg_database WHERE datname='safe_test'"],check=True,capture_output=True,text=True)
if not result.stdout.strip():
    subprocess.run(['docker','exec','safe-postgres','psql','-U','safe','-d','safe','-c','CREATE DATABASE safe_test;'],check=True)
PYDB
if ! curl --silent --fail http://localhost:8080/api/v1/public/profile >/dev/null; then
  mkdir -p .local/runtime
  cp apps/api/target/safe-api-0.1.0.jar .local/runtime/safe-api.jar
  (
    cd apps/api
    nohup /workspace/tools/jdk-21/bin/java -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -jar ../../.local/runtime/safe-api.jar > ../../.local/api.log 2>&1 < /dev/null &
    echo $! > ../../.local/api.pid
  )
fi
if ! curl --silent --fail http://localhost:5173 >/dev/null; then
  (
    cd apps/web
    nohup npm run dev > ../../.local/web.log 2>&1 < /dev/null &
    echo $! > ../../.local/web.pid
  )
fi
python3 - <<'PY'
import json,time,urllib.request
for path in ['/actuator/health','/api/v1/public/profile','/api/v1/public/projects']:
    for attempt in range(40):
        try:
            with urllib.request.urlopen('http://localhost:8080'+path,timeout=3) as res: data=json.load(res)
            if path.endswith('health'): assert data['status']=='UP'
            if path.endswith('profile'): assert 'name' in data
            if path.endswith('projects'): assert isinstance(data['content'],list)
            print(path, 'verified')
            break
        except Exception:
            if attempt==39: raise
            time.sleep(1)
with urllib.request.urlopen('http://localhost:5173',timeout=5) as res: assert res.status==200
print('Web HTTP response verified. Credentials remain in .local/dev.env.')
PY
