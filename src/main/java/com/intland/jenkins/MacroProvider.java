/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import java.io.IOException;
import java.util.List;

@Extension
public class MacroProvider extends DataBoundTokenMacro {
    public final String MACRO_NAME = "CODEBEAMER_WIKI_URI";

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String param) throws MacroEvaluationException, IOException, InterruptedException {
        List<PostBuildScript> codebeamerPlugins = build.getParent().getPublishersList().getAll(PostBuildScript.class);

        String result = "";
        if (codebeamerPlugins.size() > 0) {
            result = codebeamerPlugins.get(0).getWikiUri();
        }

        return result;
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals(MACRO_NAME);
    }
}
