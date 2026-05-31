# 测试限流功能
# 用法: .\test-ratelimit.ps1

$GatewayUrl = "http://localhost:8080/api/metric/report"

Write-Host "🧪 测试限流功能" -ForegroundColor Yellow
Write-Host "   发送 20 个快速请求 (限流阈值: 5000 QPS)"
Write-Host ""

$Success = 0
$Limited = 0

for ($i = 1; $i -le 20; $i++) {
    $Body = @{
        metricName = "qps"
        value = 1000
        tags = @{ region = "cn" }
        timestamp = 0
    } | ConvertTo-Json

    try {
        $Response = Invoke-WebRequest -Uri $GatewayUrl -Method Post -Body $Body -ContentType "application/json" -ErrorAction Stop
        if ($Response.StatusCode -eq 200) {
            $Success++
            Write-Host "请求 $i : ✅ 成功" -ForegroundColor Green
        }
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            $Limited++
            Write-Host "请求 $i : ⚠️ 被限流 (429)" -ForegroundColor Yellow
        } else {
            Write-Host "请求 $i : ❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "📊 测试结果:" -ForegroundColor Cyan
Write-Host "   成功: $Success"
Write-Host "   被限流: $Limited"
