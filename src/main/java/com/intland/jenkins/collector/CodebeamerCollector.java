/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */
package com.intland.jenkins.collector;

import com.intland.jenkins.api.CodebeamerApiClient;
import com.intland.jenkins.collector.dto.*;
import com.intland.jenkins.util.CsvUtil;
import com.intland.jenkins.util.PluginUtil;
import com.intland.jenkins.util.WikiMarkupBuilder;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.performance.PerformanceBuildAction;

import java.io.IOException;

public class CodebeamerCollector {
    private static final String TESTREPORT_ATTACHMENT_NAME = "jenkinsbuildtrends.csv";
    private static final String PERFORMANCE_ATTACHMENT_NAME = "jenkinsperformancetrends.csv";

    public static CodebeamerDto collectCodebeamerData(AbstractBuild<?, ?> build, BuildListener listener,
                                                      CodebeamerApiClient apiClient, long currentTime) throws IOException {
        String newMarkupContent;
        String newAttachmentContent;
        String attachmentName;

        BuildDto buildDto = BuildDataCollector.collectBuildData(build, currentTime);
        ScmDto scmDto = ScmDataCollector.collectScmData(build, apiClient);

        if (PluginUtil.isPerformancePluginInstalled() && build.getAction(PerformanceBuildAction.class) != null) {
            attachmentName = PERFORMANCE_ATTACHMENT_NAME;
            PerformanceDto performanceDto = PerformanceDataCollector.collectPerformanceDto(build);
            newAttachmentContent = CsvUtil.convertDtoToPerformanceRow(performanceDto, currentTime);

            newMarkupContent = new WikiMarkupBuilder()
                    .initWithPerformanceTemplate()
                    .withBuildInfo(buildDto)
                    .withPerformanceInfo(performanceDto)
                    .withScmInfo(scmDto)
                    .build();
        } else {
            attachmentName = TESTREPORT_ATTACHMENT_NAME;
            TestResultDto testResultDto = TestResultCollector.collectTestResultData(build, listener);
            newAttachmentContent = CsvUtil.convertDtoToTestResultRow(buildDto, testResultDto, currentTime);

            newMarkupContent = new WikiMarkupBuilder()
                    .initWithTestReportTemplate()
                    .withBuildInfo(buildDto)
                    .withTestReportInfo(testResultDto)
                    .withScmInfo(scmDto)
                    .build();
        }

        return new CodebeamerDto(newMarkupContent, newAttachmentContent, attachmentName);
    }
}
