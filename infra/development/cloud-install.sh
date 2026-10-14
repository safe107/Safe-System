#!/usr/bin/env bash
set -euo pipefail
cd /workspace/Safe-System
mkdir -p /workspace/tools /workspace/.cache/{npm,maven}
if [ ! -x /workspace/tools/jdk-21/bin/javac ]; then
  container_id=$(docker create eclipse-temurin:21-jdk@sha256:f9bf7e9fe2f9bc4657537c1a7e20c768f0c3bafca41598263be4714783dd62eb)
  docker cp "$container_id":/opt/java/openjdk /workspace/tools/jdk-21
  docker rm "$container_id"
fi
export JAVA_HOME=/workspace/tools/jdk-21
export MAVEN_USER_HOME=/workspace/.cache/maven-user
export MAVEN_OPTS="${MAVEN_OPTS:-} -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
python3 - <<'PY'
import os
from urllib.parse import urlsplit
from pathlib import Path
from xml.sax.saxutils import escape
proxy = urlsplit(os.environ.get('HTTPS_PROXY', ''))
xml = '<settings><localRepository>/workspace/.cache/maven</localRepository>'
if proxy.hostname:
    if proxy.username:
        raise SystemExit('Authenticated proxy needs a supported Maven binding; no credential extraction is performed.')
    xml += f'<proxies><proxy><id>cloud</id><active>true</active><protocol>https</protocol><host>{escape(proxy.hostname)}</host><port>{proxy.port or 80}</port><nonProxyHosts>localhost|127.0.0.1</nonProxyHosts></proxy></proxies>'
Path('/workspace/tools/maven-settings.xml').write_text(xml + '</settings>')
PY
(cd apps/api && ./mvnw -s /workspace/tools/maven-settings.xml -B -ntp -DskipTests package)
(cd apps/web && npm ci --cache /workspace/.cache/npm --no-audit --no-fund && npm run build)
