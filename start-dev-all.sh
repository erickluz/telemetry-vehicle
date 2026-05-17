#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_CMD=${MAVEN_CMD:-mvn}
MAVEN_ARGS="-Pstandalone spring-boot:run"
LOG_DIR="${SCRIPT_DIR}/.dev-logs"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
OBSERVABILITY_COMPOSE_FILE="${SCRIPT_DIR}/observability/docker-compose.yml"

SERVICES="
vehicle-ingestion-service
telemetry-processor-service
telemetry-dlq-service
telemetry-load-generator-service
notification-service
vehicle-telemetry-dashboard
"

mkdir -p "$LOG_DIR"

if [ ! -f "$COMPOSE_FILE" ]; then
    echo "docker-compose.yml not found at ${COMPOSE_FILE}"
    exit 1
fi

if [ ! -f "$OBSERVABILITY_COMPOSE_FILE" ]; then
    echo "observability docker-compose.yml not found at ${OBSERVABILITY_COMPOSE_FILE}"
    exit 1
fi

echo "Starting Docker Compose services..."
docker compose -f "$COMPOSE_FILE" up -d

echo "Starting observability services..."
docker compose -f "$OBSERVABILITY_COMPOSE_FILE" up -d

pids=""

stop_services() {
    if [ -n "$pids" ]; then
        echo "Stopping development services..."
        kill $pids 2>/dev/null || true
    fi
}

trap stop_services INT TERM EXIT

for service in $SERVICES; do
    pom_file="${SCRIPT_DIR}/${service}/pom.xml"

    if [ ! -f "$pom_file" ]; then
        echo "Skipping ${service}: pom.xml not found at ${pom_file}"
        continue
    fi

    log_file="${LOG_DIR}/${service}.log"
    echo "Starting ${service}; log: ${log_file}"
    "$MAVEN_CMD" $MAVEN_ARGS -f "$pom_file" >"$log_file" 2>&1 &
    pids="${pids} $!"
done

if [ -z "$pids" ]; then
    echo "No services were started."
    exit 1
fi

echo
echo "All development services were started."
echo "Observability:"
echo "  Prometheus: http://localhost:9090"
echo "  Grafana:    http://localhost:3000 (admin/admin)"
echo
echo "Actuator Prometheus endpoints:"
echo "  vehicle-ingestion-service:          http://localhost:8081/actuator/prometheus"
echo "  telemetry-processor-service:        http://localhost:8082/actuator/prometheus"
echo "  notification-service:               http://localhost:8083/actuator/prometheus"
echo "  vehicle-telemetry-dashboard:        http://localhost:8084/actuator/prometheus"
echo "  telemetry-dlq-service:              http://localhost:8085/actuator/prometheus"
echo "  telemetry-load-generator-service:   http://localhost:8086/actuator/prometheus"
echo
echo "Press Ctrl+C to stop them."
echo

wait
