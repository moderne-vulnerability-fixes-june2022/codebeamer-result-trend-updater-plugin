/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.collector.dto;

public class ScmDto {
    private String repositoryLine;
    private String changes;

    public ScmDto(String repositoryLine, String changes) {
        this.repositoryLine = repositoryLine;
        this.changes = changes;
    }

    public String getRepositoryLine() {
        return repositoryLine;
    }

    public String getChanges() {
        return changes;
    }
}
