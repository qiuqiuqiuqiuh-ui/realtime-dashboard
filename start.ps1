$DOCKER = "D:\Docker\Docker\resources\bin\docker.exe"
Set-Location "d:\AI开发\realtime-dashboard"

Write-Host "=== Realtime Dashboard Start ===" -ForegroundColor Cyan

# 1. Start Docker services
Write-Host "[1/5] Starting Kafka + Redis..." -ForegroundColor Yellow
& $DOCKER compose up -d

# 2. Wait
Write-Host "[2/5] Waiting 30s..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 3. Build
Write-Host "[3/5] Building..." -ForegroundColor Yellow
& "D:\maven\apache-maven-3.9.6\bin\mvn.cmd" clean package -DskipTests -q 2>$null
if (-not $?) {
    & "D:\maven\maven-local\..\apache-maven-3.9.6\bin\mvn.cmd" clean package -DskipTests -q 2>$null
}

# 4. Start Gateway
Write-Host "[4/5] Starting Gateway..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location 'd:\AI开发\realtime-dashboard'; java -jar dashboard-gateway/target/dashboard-gateway-1.0.0.jar"

Start-Sleep -Seconds 5

# 5. Start Collector
Write-Host "[5/5] Starting Collector..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location 'd:\AI开发\realtime-dashboard'; java -jar dashboard-consumer/target/dashboard-consumer-1.0.0.jar"

Write-Host "=== All Started ===" -ForegroundColor Green
Write-Host "Dashboard: dashboard-front/index.html"
Write-Host "Swagger:   http://localhost:8080/swagger-ui.html"
