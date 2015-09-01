/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.collector;

import com.intland.jenkins.PostBuildScript;
import com.intland.jenkins.util.TimeUtil;
import com.intland.jenkins.collector.dto.BuildDto;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;
import java.text.SimpleDateFormat;

public class BuildDataCollector {
    public static BuildDto collectBuildData(AbstractBuild<?, ?> build, long currentTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTime = build.getStartTimeInMillis();
        long duration = currentTime - startTime;

        String pluginVersion = Jenkins.getInstance().getPlugin(PostBuildScript.PLUGIN_SHORTNAME).getWrapper().getVersion();
        String projectUrl = Jenkins.getInstance().getRootUrl() + build.getProject().getUrl();
        String buildUrl = Jenkins.getInstance().getRootUrl() + build.getUrl();
        String buildDuration = TimeUtil.formatMillisIntoMinutesAndSeconds(duration);
        String formattedBuildTime = simpleDateFormat.format(currentTime);
        String buildNumber = String.valueOf(build.getNumber());
        String builtOn = build.getBuiltOn().getDisplayName();

        return new BuildDto(pluginVersion, projectUrl, buildUrl, buildDuration,
                formattedBuildTime, buildNumber, builtOn, startTime, duration);
    }
}
