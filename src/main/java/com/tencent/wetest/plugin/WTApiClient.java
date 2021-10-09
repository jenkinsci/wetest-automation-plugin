package com.tencent.wetest.plugin;

import com.cloudtestapi.CTClient;
import com.cloudtestapi.common.Credential;
import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.common.profile.ClientProfile;
import com.cloudtestapi.common.profile.HttpProfile;
import com.cloudtestapi.test.models.CompatibilityTest;
import com.cloudtestapi.test.models.TestInfo;
import com.cloudtestapi.upload.models.App;
import com.cloudtestapi.upload.models.Script;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WTApiClient {
    Logger LOGGER = Logger.getLogger(WTApp.class.getSimpleName());

    public static final String VERSION = "V1.0.0-20211009";

    public static final String DEFAULT_CLOUD_TOOL = "cloud_test";
    public static final String DEFAULT_PROTOCOL_TYPE = HttpProfile.REQ_HTTP;

    private static final int DEFAULT_CLOUD_ID = 2;
    private static final String DEFAULT_FRAME_TYPE = "uitest";

    private String secretId;
    private String secretKey;
    private String hostUrl;
    private String toolPath;
    private String protocol;

    private Credential credential;
    private ClientProfile profile;
    private CTClient ctClient;

    public WTApiClient(String secretId, String secretKey, String hostUrl, String toolPath, String protocol) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.hostUrl = hostUrl;
        this.toolPath =  StringUtils.isBlank(toolPath) ? DEFAULT_CLOUD_TOOL : toolPath;
        this.protocol =  StringUtils.isBlank(protocol) ? DEFAULT_PROTOCOL_TYPE : protocol;
        try {
            initReqConfig();
        } catch (CloudTestSDKException e) {
            LOGGER.log(Level.SEVERE, "Start Test Failed : " + e);
        }
    }

    private void initReqConfig() throws CloudTestSDKException {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setRootDomain(hostUrl);
        httpProfile.setToolPath(toolPath);
        httpProfile.setProtocol(protocol);
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

    public String getToolPath() {
        return toolPath;
    }

    @Override
    public String toString() {
        return "{\nsecretId:" + secretId + ",\n" +
                "secretKey:" + secretKey + ",\n" +
                "hostUrl:" + hostUrl + "\n}";
    }

    TestInfo startTest(String projectId, int appId, int scriptId, String groupId, String timeout) {
        try {
            CompatibilityTest compatibilityTest = new CompatibilityTest();
            compatibilityTest.setAppId(appId);
            compatibilityTest.setScriptId(scriptId);
            compatibilityTest.setDeviceNumber(Integer.parseInt(groupId));
            compatibilityTest.setCloudIds(new int[]{DEFAULT_CLOUD_ID});
            compatibilityTest.setFrameType(DEFAULT_FRAME_TYPE);

            compatibilityTest.setMaxDeviceRunTime(Integer.parseInt(timeout));
            compatibilityTest.setMaxTestRunTime(Integer.parseInt(timeout));

            if (!StringUtils.isBlank(projectId)) {
                compatibilityTest.setProject(projectId);
            }

            return ctClient.test.startCompatibilityTest(compatibilityTest);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Start Test Failed : " + e.toString());
        }
        LOGGER.log(Level.INFO, "finish Test.");
        return null;
    }

    int uploadApp(String appPath) throws CloudTestSDKException {
        int appid = 0;
        try {
            appid = Integer.parseInt(appPath);
        } catch (NumberFormatException e) {
            App appResp = ctClient.upload.multiPartUploadApk(appPath);
            appid = appResp.appId;
        }
        return appid;
    }

    int uploadScript(String script) throws CloudTestSDKException {
        int appid = 0;
        try {
            appid = Integer.parseInt(script);
        } catch (NumberFormatException e) {
            Script scriptResp = ctClient.upload.multiPartUploadScript(script);
            appid = scriptResp.scriptId;
        }
        return appid;
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
