@echo off
echo Starting SkyEye Development Environment Setup...

echo.
echo Checking Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not installed or not running
    echo Please install Docker Desktop and make sure it's running
    pause
    exit /b 1
)

echo.
echo Checking Docker Compose...
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker Compose is not available
    echo Please make sure Docker Compose is installed
    pause
    exit /b 1
)

echo.
echo Starting infrastructure services...
docker-compose up -d postgres redis rabbitmq minio elasticsearch

echo.
echo Waiting for services to be ready...
timeout /t 30 /nobreak >nul

echo.
echo Checking service status...
docker-compose ps

echo.
echo Infrastructure services are starting up...
echo.
echo Service URLs:
echo - PostgreSQL: localhost:5432 (user: skyeye, password: skyeye123)
echo - Redis: localhost:6379
echo - RabbitMQ Management: http://localhost:15672 (user: skyeye, password: skyeye123)
echo - MinIO Console: http://localhost:9001 (user: skyeye, password: skyeye123)
echo - Elasticsearch: http://localhost:9200
echo.
echo You can now start the backend services:
echo   cd skyeye-backend
echo   mvn spring-boot:run -pl skyeye-gateway
echo   mvn spring-boot:run -pl skyeye-auth
echo.
echo And the frontend:
echo   cd skyeye-frontend
echo   npm run dev
echo.
pause