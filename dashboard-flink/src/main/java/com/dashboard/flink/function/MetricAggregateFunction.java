package com.dashboard.flink.function;

import com.dashboard.common.dto.AggregatedData;
import com.dashboard.common.dto.MetricData;
import org.apache.flink.api.common.functions.AggregateFunction;

/**
 * 增量聚合函数
 *
 * 面试亮点：
 * 1. AggregateFunction 是增量聚合，窗口内每来一条数据就计算一次
 * 2. 比 WindowFunction (全量聚合) 性能更高，不需要缓存所有数据
 * 3. 累加器 (Accumulator) 在窗口内持续更新
 *
 * 对比：
 * - AggregateFunction: 增量计算，内存 O(1)
 * - WindowFunction: 全量计算，内存 O(n) (需缓存窗口内所有数据)
 * - ProcessWindowFunction: 全量 + 窗口元数据 (如窗口起止时间)
 */
public class MetricAggregateFunction implements AggregateFunction<MetricData, AggregatedData, AggregatedData> {

    @Override
    public AggregatedData createAccumulator() {
        // 初始化累加器
        AggregatedData acc = new AggregatedData();
        acc.setMin(Double.MAX_VALUE);
        acc.setMax(Double.MIN_VALUE);
        return acc;
    }

    @Override
    public AggregatedData add(MetricData data, AggregatedData acc) {
        // 每来一条数据，增量更新累加器
        acc.setMetricName(data.getMetricName());
        acc.setSum(acc.getSum() + data.getValue());
        acc.setCount(acc.getCount() + 1);
        acc.setAvg(acc.getSum() / acc.getCount());
        acc.setMax(Math.max(acc.getMax(), data.getValue()));
        acc.setMin(Math.min(acc.getMin(), data.getValue()));
        return acc;
    }

    @Override
    public AggregatedData getResult(AggregatedData acc) {
        // 窗口触发时返回最终结果
        return acc;
    }

    @Override
    public AggregatedData merge(AggregatedData a, AggregatedData b) {
        // Session Window 合并两个累加器 (滑动窗口不会调用)
        a.setSum(a.getSum() + b.getSum());
        a.setCount(a.getCount() + b.getCount());
        a.setAvg(a.getSum() / a.getCount());
        a.setMax(Math.max(a.getMax(), b.getMax()));
        a.setMin(Math.min(a.getMin(), b.getMin()));
        return a;
    }
}
