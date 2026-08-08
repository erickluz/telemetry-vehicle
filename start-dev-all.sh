#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_CMD=${MAVEN_CMD:-}
MAVEN_ARGS="-Pstandalone spring-boot:run"
LOG_DIR="${SCRIPT_DIR}/.dev-logs"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
OBSERVABILITY_COMPOSE_FILE="${SCRIPT_DIR}/observability/docker-compose.yml"
MAVEN_DOCKER_IMAGE=${MAVEN_DOCKER_IMAGE:-maven:3.9.9-eclipse-temurin-17}
HOST_UID=${SUDO_UID:-$(id -u)}
HOST_GID=${SUDO_GID:-$(id -g)}
HOST_HOME=${HOME:-/tmp}

if [ -n "${SUDO_USER:-}" ] && command -v getent >/dev/null 2>&1; then
    sudo_home=$(getent passwd "$SUDO_USER" | cut -d: -f6)
    if [ -n "$sudo_home" ]; then
        HOST_HOME=$sudo_home
    fi
fi

MAVEN_CACHE_DIR=${MAVEN_CACHE_DIR:-${HOST_HOME}/.m2}

SERVICES="
vehicle-ingestion-service
telemetry-processor-service
telemetry-dlq-service
telemetry-load-generator-service
notification-service
vehicle-telemetry-dashboard
"

mkdir -p "$LOG_DIR"

if [ -z "$MAVEN_CMD" ]; then
    if [ -x "${SCRIPT_DIR}/mvnw" ]; then
        MAVEN_CMD="${SCRIPT_DIR}/mvnw"
    elif command -v mvn >/dev/null 2>&1; then
        MAVEN_CMD="mvn"
    else
        MAVEN_CMD="docker"
    fi
fi

if [ "$MAVEN_CMD" != "docker" ] && ! command -v "$MAVEN_CMD" >/dev/null 2>&1 && [ ! -x "$MAVEN_CMD" ]; then
    echo "Maven command not found: ${MAVEN_CMD}"
    echo "Install Maven, add it to PATH, or set MAVEN_CMD=/path/to/mvn."
    echo "If Docker is available, unset MAVEN_CMD to use the Docker Maven fallback."
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker was not found. Install Docker to start the infrastructure services."
    if [ "$MAVEN_CMD" = "docker" ]; then
        echo "Maven was not found either, so the Docker Maven fallback is not available."
    fi
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Docker is installed, but the daemon is not accessible from this shell."
    echo "Start Docker, run this script with a user that can access Docker, or configure the docker group."
    exit 1
fi

if [ "$MAVEN_CMD" = "docker" ]; then
    echo "Maven was not found in PATH; using Docker image ${MAVEN_DOCKER_IMAGE}."
    mkdir -p "$MAVEN_CACHE_DIR"
    if ! docker image inspect "$MAVEN_DOCKER_IMAGE" >/dev/null 2>&1; then
        echo "Pulling ${MAVEN_DOCKER_IMAGE}..."
        docker pull "$MAVEN_DOCKER_IMAGE"
    fi
fi

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

run_service() {
    service=$1
    pom_file=$2

    if [ "$MAVEN_CMD" = "docker" ]; then
        exec docker run --rm \
            --network host \
            --user "${HOST_UID}:${HOST_GID}" \
            -e MAVEN_CONFIG=/tmp/.m2 \
            -v "${SCRIPT_DIR}:/workspace" \
            -v "${MAVEN_CACHE_DIR}:/tmp/.m2" \
            -w "/workspace/${service}" \
            "$MAVEN_DOCKER_IMAGE" \
            mvn $MAVEN_ARGS -f "/workspace/${service}/pom.xml"
    fi

    exec "$MAVEN_CMD" $MAVEN_ARGS -f "$pom_file"
}

service_log_file() {
    service=$1
    log_file="${LOG_DIR}/${service}.log"

    if [ -e "$log_file" ] && [ ! -w "$log_file" ]; then
        user_name=${USER:-user}
        log_file="${LOG_DIR}/${service}.${user_name}.log"
        echo "Log file is not writable; using ${log_file}" >&2
    fi

    : >"$log_file"
    printf '%s\n' "$log_file"
}

for service in $SERVICES; do
    pom_file="${SCRIPT_DIR}/${service}/pom.xml"

    if [ ! -f "$pom_file" ]; then
        echo "Skipping ${service}: pom.xml not found at ${pom_file}"
        continue
    fi

    log_file=$(service_log_file "$service")
    echo "Starting ${service}; log: ${log_file}"
    run_service "$service" "$pom_file" >"$log_file" 2>&1 &
    pid=$!
    sleep 1

    if kill -0 "$pid" 2>/dev/null; then
        pids="${pids} ${pid}"
    else
        echo "Failed to start ${service}. Last log lines:"
        tail -n 20 "$log_file" || true
        exit 1
    fi
done

if [ -z "$pids" ]; then
    echo "No services were started."
    exit 1
fi

echo
echo "All development services were started."
if [ "$MAVEN_CMD" = "docker" ]; then
    echo "Maven command: Docker fallback (${MAVEN_DOCKER_IMAGE})"
else
    echo "Maven command: ${MAVEN_CMD}"
fi
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
