package com.tencent.wetest.plugin.model;

public class ProjectInfo {
    private String projectId;
    private String projectName;

    public ProjectInfo(String projectName, String projectId) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }
}