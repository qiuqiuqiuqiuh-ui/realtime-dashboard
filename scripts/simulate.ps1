# 模拟数据上报脚本 (PowerShell 版)
# 用法: .\simulate.ps1 -QPS 100 -Duration 60

param(
    [int]$QPS = 100,
    [int]$Duration = 60
)

$GatewayUrl = "http://localhost:8080/api/metric/report"
$Interval = [math]::Max(1, [math]::Floor(1000 / $QPS))
$EndTime = (Get-Date).AddSeconds($Duration)

Write-Host "🚀 开始模拟数据上报" -ForegroundColor Green
Write-Host "   QPS: $QPS"
Write-Host "   持续时间: ${Duration}秒"
Write-Host "   目标: $GatewayUrl"
Write-Host ""

$Count = 0
$Success = 0
$Fail = 0

while ((Get-Date) -lt $EndTime) {
    $QpsValue = 800 + (Get-Random -Minimum 0 -Maximum 2000)
    $RtValue = 20 + (Get-Random -Minimum 0 -Maximum 150)

    $Body = @{
        metricName = "qps"
        value = $QpsValue
        tags = @{ region = "cn"; server = "node-1" }
        timestamp = 0
    } | ConvertTo-Json

    try {
        $Response = Invoke-RestMethod -Uri $GatewayUrl -Method Post -Body $Body -ContentType "application/json" -ErrorAction Stop
        $Success++
    } catch {
        $Fail++
    }

    $Count++

    if ($Count % 100 -eq 0) {
        Write-Host "已发送: $Count 条, 成功: $Success, 失败: $Fail" -ForegroundColor Cyan
    }

    Start-Sleep -Milliseconds $Interval
}

Write-Host ""
Write-Host "✅ 模拟完成!" -ForegroundColor Green
Write-Host "   总发送: $Count 条"
Write-Host "   成功: $Success 条"
Write-Host "   失败: $Fail 条"
