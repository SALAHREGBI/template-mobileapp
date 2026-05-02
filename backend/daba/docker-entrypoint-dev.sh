#!/usr/bin/env bash
set -euo pipefail

cd /app
# Normalise line endings when the repo is bind-mounted from Windows.
sed -i 's/\r$//' mvnw 2>/dev/null || true
chmod +x mvnw 2>/dev/null || true

# Polling compile works reliably with Docker Desktop bind mounts (including Windows hosts).
compile_poll_seconds="${COMPILE_POLL_SECONDS:-5}"
(
  while sleep "${compile_poll_seconds}"; do
    ./mvnw -q -DskipTests compile 2>/dev/null || true
  done
) &
exec ./mvnw spring-boot:run
