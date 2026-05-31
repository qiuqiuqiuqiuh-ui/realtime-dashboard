package com.dashboard.flink.function;

import com.dashboard.common.dto.AggregatedData;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * 窗口结果处理 - 附加窗口元数据
 *
 * 面试亮点：
 * 1. ProcessWindowFunction 可以获取窗口的起止时间
 * 2. 与 AggregateFunction 配合使用：先增量聚合，再附加元数据
 * 3. 这种组合模式兼顾性能和信息完整性
 *
 * 使用方式：
 * .aggregate(new MetricAggregateFunction(), new MetricWindowResultProcess())
 */
public class MetricWindowResultProcess
        extends ProcessWindowFunction<AggregatedData, AggregatedData, String, TimeWindow> {

    @Override
    public void process(String metricName,
                        Context context,
                        Iterable<AggregatedData> elements,
                        Collector<AggregatedData> out) {
        TimeWindow window = context.window();

        for (AggregatedData data : elements) {
            // 附加窗口时间信息
            data.setWindowStart(window.getStart());
            data.setWindowEnd(window.getEnd());
            out.collect(data);
        }
    }
}
