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
    private static final String TESTREPORT_DEFAULT_MARKUP = "[{JenkinsBuildTrends}]";
    private static final String PERFORMANCE_DEFAULT_MARKUP= "[{JenkinsPerformanceTrends}]";

    public static CodebeamerDto collectCodebeamerData(AbstractBuild<?, ?> build, BuildListener listener,
                                                      CodebeamerApiClient apiClient, long currentTime) throws IOException {
        String currentMarkupContent = apiClient.getWikiMarkup();

        String newMarkupContent;
        String newAttachmentContent;
        String attachmentName;

        BuildDto buildDto = BuildDataCollector.collectBuildData(build, currentTime);
        ScmDto scmDto = ScmDataCollector.collectScmData(build, apiClient);

        if (PluginUtil.isPerformancePluginInstalled() && build.getAction(PerformanceBuildAction.class) != null) {
            attachmentName = PERFORMANCE_ATTACHMENT_NAME;
            currentMarkupContent = insertChartIfNoPluginPresent(currentMarkupContent, PERFORMANCE_DEFAULT_MARKUP);

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
            currentMarkupContent = insertChartIfNoPluginPresent(currentMarkupContent, TESTREPORT_DEFAULT_MARKUP);

            TestResultDto testResultDto = TestResultCollector.collectTestResultData(build, listener);
            newAttachmentContent = CsvUtil.convertDtoToTestResultRow(buildDto, testResultDto, currentTime);

            newMarkupContent = new WikiMarkupBuilder()
                    .initWithTestReportTemplate()
                    .withBuildInfo(buildDto)
                    .withTestReportInfo(testResultDto)
                    .withScmInfo(scmDto)
                    .build();
        }

        StringBuilder markupBuilder = new StringBuilder(currentMarkupContent);
        markupBuilder.insert(markupBuilder.indexOf("}]") + 2, newMarkupContent);

        return new CodebeamerDto(markupBuilder.toString(), newAttachmentContent, attachmentName);
    }

    public static String insertChartIfNoPluginPresent(String currentMarkup, String defaultMarkup) {
        if (currentMarkup == null) {
            currentMarkup = "";
        }

        int index = currentMarkup.indexOf("}]");
        if (index == -1) {
            return defaultMarkup + currentMarkup;
        } else {
            return currentMarkup;
        }
    }
}
