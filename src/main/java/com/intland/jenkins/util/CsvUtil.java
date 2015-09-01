/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.util;

import com.intland.jenkins.collector.dto.BuildDto;
import com.intland.jenkins.collector.dto.PerformanceDto;
import com.intland.jenkins.collector.dto.TestResultDto;

public class CsvUtil {
    public static String convertDtoToPerformanceRow(PerformanceDto performanceDto, long currentTime) {
        return String.format("%s;%s;%s;%s;\n", System.currentTimeMillis(), performanceDto.getAverageResponseTime(),
                performanceDto.getMedianResponseTime(), performanceDto.getMaximumResponseTime());
    }

    public static String convertDtoToTestResultRow(BuildDto buildDto, TestResultDto testResultDto, long currentTime) {
        return String.format("%s;%s;%s;%s;%s\n", currentTime, buildDto.getBuildDuration(),
                testResultDto.getTestDuration(), testResultDto.getFailCount(), testResultDto.getTotalCount());
    }
}
