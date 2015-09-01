/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.collector.dto;

public class PerformanceDto {
    long medianResponseTime;
    long averageResponseTime;
    long maximumResponseTime;
    boolean failed;

    public PerformanceDto(long averageResponseTime, long medianResponseTime,  long maximumResponseTime, boolean failed) {
        this.averageResponseTime = averageResponseTime;
        this.medianResponseTime = medianResponseTime;
        this.maximumResponseTime = maximumResponseTime;
        this.failed = failed;
    }

    public long getMedianResponseTime() {
        return medianResponseTime;
    }

    public long getAverageResponseTime() {
        return averageResponseTime;
    }

    public long getMaximumResponseTime() {
        return maximumResponseTime;
    }

    public boolean isFailed() {
        return failed;
    }
}
