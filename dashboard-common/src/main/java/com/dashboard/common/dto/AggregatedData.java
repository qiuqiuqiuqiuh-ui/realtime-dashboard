package com.dashboard.common.dto;

import java.io.Serializable;

/**
 * 聚合数据 - 经过计算后的指标聚合结果
 */
public class AggregatedData implements Serializable {
    private String metricName;
    private double avg;
    private double max;
    private double min;
    private double sum;
    private long count;
    private long windowStart;  // 聚合窗口起始时间
    private long windowEnd;    // 聚合窗口结束时间

    public AggregatedData() {}

    public AggregatedData(String metricName, long windowStart, long windowEnd) {
        this.metricName = metricName;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.min = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
    }

    public void accumulate(double value) {
        this.sum += value;
        this.count++;
        this.avg = this.sum / this.count;
        if (value > this.max) this.max = value;
        if (value < this.min) this.min = value;
    }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public double getAvg() { return avg; }
    public void setAvg(double avg) { this.avg = avg; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }
    public double getSum() { return sum; }
    public void setSum(double sum) { this.sum = sum; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public long getWindowStart() { return windowStart; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }
    public long getWindowEnd() { return windowEnd; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }
}
