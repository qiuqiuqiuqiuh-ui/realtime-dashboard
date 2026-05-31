$DOCKER = "D:\Docker\Docker\resources\bin\docker.exe"
& $DOCKER compose down
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Write-Host "All stopped" -ForegroundColor Green
