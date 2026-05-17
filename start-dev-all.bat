@echo off
setlocal

set "REPO_ROOT=%~dp0"
if "%REPO_ROOT:~-1%"=="\" set "REPO_ROOT=%REPO_ROOT:~0,-1%"

if not defined MAVEN_CMD set "MAVEN_CMD=D:\dev\sws\apache-maven-3.9.11\bin\mvn.cmd"
if not exist "%MAVEN_CMD%" set "MAVEN_CMD=mvn.cmd"

set "MAVEN_ARGS=-Pstandalone spring-boot:run"
set "COMPOSE_FILE=%REPO_ROOT%\docker-compose.yml"
set "OBSERVABILITY_COMPOSE_FILE=%REPO_ROOT%\observability\docker-compose.yml"

if not exist "%COMPOSE_FILE%" (
    echo docker-compose.yml not found at "%COMPOSE_FILE%"
    exit /b 1
)

if not exist "%OBSERVABILITY_COMPOSE_FILE%" (
    echo observability docker-compose.yml not found at "%OBSERVABILITY_COMPOSE_FILE%"
    exit /b 1
)

echo Starting Docker Compose services...
docker compose -f "%COMPOSE_FILE%" up -d
if errorlevel 1 exit /b 1

echo Starting observability services...
docker compose -f "%OBSERVABILITY_COMPOSE_FILE%" up -d
if errorlevel 1 exit /b 1

call :start_service vehicle-ingestion-service
call :start_service telemetry-processor-service
call :start_service telemetry-dlq-service
call :start_service telemetry-load-generator-service
call :start_service notification-service
call :start_service vehicle-telemetry-dashboard

echo.
echo All development services were started in separate windows.
echo Maven command: %MAVEN_CMD%
echo.
echo Observability:
echo   Prometheus: http://localhost:9090
echo   Grafana:    http://localhost:3000  ^(admin/admin^)
echo.
echo Actuator Prometheus endpoints:
echo   vehicle-ingestion-service:          http://localhost:8081/actuator/prometheus
echo   telemetry-processor-service:        http://localhost:8082/actuator/prometheus
echo   notification-service:               http://localhost:8083/actuator/prometheus
echo   vehicle-telemetry-dashboard:        http://localhost:8084/actuator/prometheus
echo   telemetry-dlq-service:              http://localhost:8085/actuator/prometheus
echo   telemetry-load-generator-service:   http://localhost:8086/actuator/prometheus
echo.
exit /b 0

:start_service
set "SERVICE_NAME=%~1"
set "POM_FILE=%REPO_ROOT%\%SERVICE_NAME%\pom.xml"

if not exist "%POM_FILE%" (
    echo Skipping %SERVICE_NAME%: pom.xml not found at "%POM_FILE%"
    exit /b 1
)

echo Starting %SERVICE_NAME%...
start "%SERVICE_NAME%" cmd /k ""%MAVEN_CMD%" %MAVEN_ARGS% -f "%POM_FILE%""
exit /b 0
