/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.collector;

import com.intland.jenkins.collector.dto.PerformanceDto;
import hudson.model.AbstractBuild;
import hudson.plugins.performance.PerformanceBuildAction;
import hudson.plugins.performance.PerformanceReport;

import java.util.Map;

public class PerformanceDataCollector {
    public static PerformanceDto collectPerformanceDto(AbstractBuild<?, ?> build) {
        PerformanceBuildAction performanceBuildAction = build.getAction(PerformanceBuildAction.class);

        Map<String, PerformanceReport> reportMap = performanceBuildAction.getPerformanceReportMap().getPerformanceReportMap();
        long avg = 0l;
        long median = 0l;
        long maximum = 0l;
        boolean hasErrors = false;
        int count = 0;

        for (PerformanceReport performanceReport : reportMap.values()) {
            avg += performanceReport.getAverage();
            median += performanceReport.getMedian();
            maximum += performanceReport.getMax();

            if (performanceReport.countErrors() > 0) {
                hasErrors = true;
            }

            count++;
        }

        if (count > 0) {
            avg = avg / count;
            median = median / count;
            maximum = maximum / count;
        }

        return new PerformanceDto(avg, median,  maximum, hasErrors);
    }
}
