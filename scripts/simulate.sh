#!/bin/bash
# 模拟数据上报脚本
# 用法: bash simulate.sh [QPS] [持续时间秒]

QPS=${1:-100}
DURATION=${2:-60}
GATEWAY_URL="http://localhost:8080/api/metric/report"

echo "🚀 开始模拟数据上报"
echo "   QPS: $QPS"
echo "   持续时间: ${DURATION}秒"
echo "   目标: $GATEWAY_URL"
echo ""

INTERVAL=$(echo "scale=4; 1 / $QPS" | bc)
END_TIME=$(($(date +%s) + DURATION))
COUNT=0
SUCCESS=0
FAIL=0

while [ $(date +%s) -lt $END_TIME ]; do
    # 随机生成 QPS 指标
    QPS_VALUE=$((800 + RANDOM % 2000))
    RT_VALUE=$((20 + RANDOM % 150))

    # 发送 QPS 数据
    curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL" \
        -H "Content-Type: application/json" \
        -d "{\"metricName\":\"qps\",\"value\":$QPS_VALUE,\"tags\":{\"region\":\"cn\",\"server\":\"node-1\"},\"timestamp\":0}" \
        | (read code; if [ "$code" = "200" ]; then SUCCESS=$((SUCCESS+1)); else FAIL=$((FAIL+1)); fi)

    COUNT=$((COUNT+1))

    # 每 100 次打印进度
    if [ $((COUNT % 100)) -eq 0 ]; then
        echo "已发送: $COUNT 条, 成功: $SUCCESS, 失败: $FAIL"
    fi

    sleep $INTERVAL
done

echo ""
echo "✅ 模拟完成!"
echo "   总发送: $COUNT 条"
echo "   成功: $SUCCESS 条"
echo "   失败: $FAIL 条"
