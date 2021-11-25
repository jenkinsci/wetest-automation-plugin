package com.tencent.wetest.plugin.model;

public class ProjectInfo {
    private String projectId;
    private String projectName;
    private String projectEnId;

    public ProjectInfo(String projectName, String projectId, String projectEnId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectEnId = projectEnId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectEnId() {
        return projectEnId;
    }
}