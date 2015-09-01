/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.collector;

import com.intland.jenkins.util.TimeUtil;
import com.intland.jenkins.collector.dto.TestResultDto;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;

public class TestResultCollector {
    public static TestResultDto collectTestResultData(AbstractBuild<?, ?> build, BuildListener listener) {
        String formattedTestDuration = "";
        int totalCount = 0;
        int failCount = 0;
        String failedDifference = "";
        long testDuration = 0l;

        AbstractTestResultAction<?> action = build.getAction(AbstractTestResultAction.class);
        if (action != null) {
            TestResult testResult = (TestResult) action.getResult();
            TestResult lastTestResult = getPreviousTestResult(build);

            testDuration = new Float(testResult.getDuration() * 1000).longValue();
            formattedTestDuration = TimeUtil.formatMillisIntoMinutesAndSeconds(testDuration);
            totalCount = testResult.getTotalCount();
            failCount = testResult.getFailCount();

            if (lastTestResult != null) {
                failedDifference = failDifference(testResult, lastTestResult);
            } else {
                listener.getLogger().println("No previous build has been found with a test run");
            }
        } else {
            listener.getLogger().println("This build does not have a test run");
        }

        return new TestResultDto(formattedTestDuration, totalCount, failCount, failedDifference, testDuration);
    }

    private static TestResult getPreviousTestResult(AbstractBuild build) {
        TestResult result = null;

        int counter = build.getNumber();
        while (result == null && counter > 0) {
            counter--;
            Run candidateBuild = build.getParent().getBuild(String.valueOf(counter));

            if (candidateBuild == null) {
                continue;
            }

            AbstractTestResultAction candidateTestResultAction = candidateBuild.getAction(AbstractTestResultAction.class);
            if (candidateTestResultAction !=  null) {
                result = (TestResult) candidateTestResultAction.getResult();
                break;
            }
        }
        return result;
    }

    private static String failDifference(TestResult testResult1, TestResult testResult2) {
        if (testResult1 == null || testResult2 == null) {
            return " - ?";
        }

        String sign;
        if (testResult1.getFailCount() > testResult2.getFailCount()) {
            sign = "+";
        } else if (testResult1.getFailCount() < testResult2.getFailCount()) {
            sign = "-";
        } else {
            sign = "±";
        }

        return sign + Math.abs(testResult1.getFailCount() - testResult2.getFailCount());
    }
}
