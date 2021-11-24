package com.tencent.wetest.plugin.model;

public class GroupInfo {
    private String groupId;
    private String groupName;
    private String cloudName;
    private int deviceNum;
    private int deviceType;
    private int cloudId;
    public GroupInfo(String groupName, String groupId, String cloudName,
                     int deviceNum, int deviceType, int cloudId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.cloudName = cloudName;
        this.deviceNum = deviceNum;
        this.deviceType = deviceType;
        this.cloudId = cloudId;
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

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}