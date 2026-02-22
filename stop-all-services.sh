#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$ROOT_DIR/.run/pids"

SERVICES=(
  "api-gateway"
  "medical-record-service"
  "appointment-service"
  "patient-service"
  "eureka-server"
  "config-server"
)

if [[ ! -d "$PID_DIR" ]]; then
  echo "[INFO] PID directory not found: $PID_DIR"
  echo "[INFO] Nothing to stop."
  exit 0
fi

is_pid_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1
}

stop_pid() {
  local pid="$1"
  local label="$2"
  local timeout=20
  local waited=0

  if ! is_pid_running "$pid"; then
    echo "[INFO] $label PID $pid is not running"
    return 0
  fi

  echo "[STOP] $label (PID $pid)"
  kill "$pid" >/dev/null 2>&1 || true

  while (( waited < timeout )); do
    if ! is_pid_running "$pid"; then
      echo "[OK] $label stopped"
      return 0
    fi
    sleep 1
    waited=$((waited + 1))
  done

  echo "[WARN] $label did not stop after ${timeout}s, sending SIGKILL"
  kill -9 "$pid" >/dev/null 2>&1 || true
  sleep 1

  if is_pid_running "$pid"; then
    echo "[ERROR] Failed to stop $label (PID $pid)"
    return 1
  fi

  echo "[OK] $label stopped (SIGKILL)"
  return 0
}

main() {
  local failures=0

  echo "Stopping hospital microservices stack from: $ROOT_DIR"

  for service in "${SERVICES[@]}"; do
    local pid_file="$PID_DIR/${service}.pid"

    if [[ ! -f "$pid_file" ]]; then
      echo "[SKIP] $service (no PID file)"
      continue
    fi

    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"

    if [[ -z "$pid" ]]; then
      echo "[WARN] $service PID file is empty, removing: $pid_file"
      rm -f "$pid_file"
      continue
    fi

    if stop_pid "$pid" "$service"; then
      rm -f "$pid_file"
    else
      failures=$((failures + 1))
    fi
  done

  # Cleanup any stale PID files left for dead processes
  shopt -s nullglob
  for pid_file in "$PID_DIR"/*.pid; do
    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [[ -z "$pid" ]] || ! is_pid_running "$pid"; then
      rm -f "$pid_file"
    fi
  done
  shopt -u nullglob

  if (( failures > 0 )); then
    echo "[ERROR] Completed with $failures stop failure(s)."
    exit 1
  fi

  echo "[DONE] All tracked services stopped."
}

main "$@"
