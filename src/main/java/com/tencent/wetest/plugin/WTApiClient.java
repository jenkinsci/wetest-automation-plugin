package com.tencent.wetest.plugin;

import com.cloudtestapi.CTClient;
import com.cloudtestapi.common.Credential;
import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.common.profile.ClientProfile;
import com.cloudtestapi.common.profile.HttpProfile;
import com.cloudtestapi.test.models.CompatibilityTest;
import com.cloudtestapi.test.models.TestInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WTApiClient {
    Logger LOGGER = Logger.getLogger(WTApp.class.getSimpleName());

    private static final int DEFAULT_CLOUD_ID = 2;
    private static final String DEFAULT_CLOUD_TOOL = "cloud_test";
    private static final String DEFAULT_FRAME_TYPE = "uitest";

    private String secretId;
    private String secretKey;
    private String hostUrl;

    private Credential credential;
    private ClientProfile profile;
    private CTClient ctClient;

    public WTApiClient(String secretId, String secretKey, String hostUrl) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.hostUrl = hostUrl;

        try {
            initReqConfig();
        } catch (CloudTestSDKException e) {
            LOGGER.log(Level.SEVERE, "Start Test Failed : " + e);
        }
    }

    private void initReqConfig() throws CloudTestSDKException {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setRootDomain(hostUrl);
        httpProfile.setToolPath(DEFAULT_CLOUD_TOOL);
        httpProfile.setProtocol(HttpProfile.REQ_HTTPS);
        profile = new ClientProfile(ClientProfile.SIGN_SHA256, httpProfile);
        credential = new Credential(secretId, secretKey);
        ctClient = new CTClient(credential, profile);
    }

    public String getSecretId() {
        return this.secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getHostUrl() {
        return this.hostUrl;
    }

    @Override
    public String toString() {
        return "{\nsecretId:" + secretId + ",\n" +
                "secretKey:" + secretKey + ",\n" +
                "hostUrl:" + hostUrl + "\n}";
    }

    TestInfo startTest(String projectId, String appPath, String scriptPath, String groupId, String timeout) {
        try {
            CompatibilityTest compatibilityTest = new CompatibilityTest();
            compatibilityTest.setProject(projectId);
            compatibilityTest.setAppId(Integer.parseInt(appPath));
            compatibilityTest.setDeviceNumber(Integer.parseInt(groupId));
            compatibilityTest.setCloudIds(new int[]{DEFAULT_CLOUD_ID});
            compatibilityTest.setScriptId(Integer.parseInt(scriptPath));
            compatibilityTest.setFrameType(DEFAULT_FRAME_TYPE);
            compatibilityTest.setMaxDeviceRunTime(Integer.parseInt(timeout));
            compatibilityTest.setMaxTestRunTime(Integer.parseInt(timeout));
            return ctClient.test.startCompatibilityTest(compatibilityTest);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Start Test Failed : " + e);
        }
        LOGGER.log(Level.INFO, "finish Test.");
        return null;
    }

    List<ProjectInfo> getProjectIds() {
        //TODO: get from web server

        List<ProjectInfo> projects = new ArrayList<>();
        projects.add(new ProjectInfo("测试项目1", "59Gq6okp"));
        projects.add(new ProjectInfo("测试项目2", "59Gq6okp"));
        return projects;
    }

    List<GroupInfo> getGroupIds() {
        //TODO: get from web server

        List<GroupInfo> groups = new ArrayList<>();
        groups.add(new GroupInfo("测试设备组1", "1"));
        groups.add(new GroupInfo("测试设备组2", "2"));
        groups.add(new GroupInfo("random5", "5"));
        return groups;
    }

    static class ProjectInfo {
        String project_id;
        String project_name;

        ProjectInfo(String project_name, String project_id) {
            this.project_id = project_id;
            this.project_name = project_name;
        }
    }

    static class GroupInfo {
        String group_id;
        String group_name;

        GroupInfo(String group_name, String group_id) {
            this.group_id = group_id;
            this.group_name = group_name;
        }
    }
}
