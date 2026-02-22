#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_DIR="$ROOT_DIR/.run"
LOG_DIR="$RUN_DIR/logs"
PID_DIR="$RUN_DIR/pids"

mkdir -p "$LOG_DIR" "$PID_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "[ERROR] Maven (mvn) is not installed or not in PATH."
  exit 1
fi

if ! command -v nc >/dev/null 2>&1; then
  echo "[ERROR] 'nc' (netcat) is required for port checks."
  exit 1
fi

# module|port|label|timeout_seconds
SERVICES=(
  "config-server|8888|Config Server|90"
  "eureka-server|8761|Eureka Server|90"
  "patient-service|8081|Patient Service|90"
  "appointment-service|8082|Appointment Service|90"
  "medical-record-service|8083|Medical Record Service|90"
  "api-gateway|8080|API Gateway|90"
)

is_pid_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1
}

is_port_open() {
  local port="$1"
  nc -z localhost "$port" >/dev/null 2>&1
}

wait_for_port() {
  local port="$1"
  local label="$2"
  local timeout="$3"
  local waited=0

  while (( waited < timeout )); do
    if is_port_open "$port"; then
      echo "[OK] $label is up on port $port"
      return 0
    fi
    sleep 1
    waited=$((waited + 1))
  done

  echo "[ERROR] $label did not open port $port within ${timeout}s"
  return 1
}

start_service() {
  local module="$1"
  local port="$2"
  local label="$3"
  local timeout="$4"
  local pid_file="$PID_DIR/${module}.pid"
  local log_file="$LOG_DIR/${module}.log"

  if is_port_open "$port"; then
    echo "[SKIP] $label already appears to be running (port $port is open)"
    return 0
  fi

  if [[ -f "$pid_file" ]]; then
    local existing_pid
    existing_pid="$(cat "$pid_file" 2>/dev/null || true)"
    if is_pid_running "$existing_pid"; then
      echo "[SKIP] $label already running with PID $existing_pid"
      return 0
    else
      rm -f "$pid_file"
    fi
  fi

  echo "[START] $label ($module) ..."
  (
    cd "$ROOT_DIR"
    nohup mvn -pl "$module" spring-boot:run > "$log_file" 2>&1 &
    echo $! > "$pid_file"
  )

  local pid
  pid="$(cat "$pid_file")"
  echo "[INFO] $label PID=$pid log=$log_file"

  if ! wait_for_port "$port" "$label" "$timeout"; then
    echo "[INFO] Last 40 log lines for $label:"
    tail -n 40 "$log_file" || true
    exit 1
  fi
}

print_summary() {
  echo
  echo "Services started."
  echo "Logs: $LOG_DIR"
  echo "PIDs: $PID_DIR"
  echo
  echo "Quick checks:"
  echo "  curl -i http://localhost:8888/actuator/health    # config-server (if actuator exposed)"
  echo "  curl -i http://localhost:8761                     # eureka UI"
  echo "  curl -i http://localhost:8080/actuator/health     # api-gateway"
  echo
  echo "Gateway examples:"
  echo "  curl -i http://localhost:8080/api/v1/patients"
  echo "  curl -i http://localhost:8080/api/v1/appointments"
  echo "  curl -i http://localhost:8080/api/v1/medical-records/patient/1"
}

main() {
  echo "Starting hospital microservices stack from: $ROOT_DIR"
  for svc in "${SERVICES[@]}"; do
    IFS='|' read -r module port label timeout <<< "$svc"
    start_service "$module" "$port" "$label" "$timeout"
  done
  print_summary
}

main "$@"
