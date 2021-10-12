package com.tencent.wetest.plugin.model;

public class GroupInfo {
    private String groupId;
    private String groupName;
    private String cloudName;
    private int deviceNum;

    public GroupInfo(String groupName, String groupId, String cloudName, int deviceNum) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.cloudName = cloudName;
        this.deviceNum = deviceNum;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCloudName() {
        return cloudName;
    }

    public int getDeviceNum() {
        return deviceNum;
    }
}