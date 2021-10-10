package com.tencent.wetest.plugin;

import com.cloudtestapi.CTClient;
import com.cloudtestapi.account.models.Project;
import com.cloudtestapi.common.Credential;
import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.common.profile.ClientProfile;
import com.cloudtestapi.common.profile.HttpProfile;
import com.cloudtestapi.device.models.ModelList;
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

    public static final int DEFAULT_TIMEOUT = 600;
    public static final String DEFAULT_CLOUD_TOOL = "cloudtest";
    public static final String DEFAULT_PROTOCOL_TYPE = HttpProfile.REQ_HTTP;
    public static final int DEFAULT_CLOUD_ID = 2;
    public static final String DEFAULT_FRAME_TYPE = "uitest";

    private static final String CHOOSE_TYPE_DEVICE_IDS ="deviceids";
    private static final String CHOOSE_TYPE_MODEL_IDS ="modelids";

    private String secretId;
    private String secretKey;
    private String hostUrl;
    private String toolPath;
    private String protocol;

    private Credential credential;
    private ClientProfile profile;
    private CTClient ctClient;

    ModelList[] modelList;

    private String chooseType = CHOOSE_TYPE_DEVICE_IDS;

    public WTApiClient(String secretId, String secretKey, String hostUrl, String toolPath, String protocol) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.hostUrl = hostUrl;
        this.toolPath = StringUtils.isBlank(toolPath) ? DEFAULT_CLOUD_TOOL : toolPath;
        this.protocol = StringUtils.isBlank(protocol) ? DEFAULT_PROTOCOL_TYPE : protocol;
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

    TestInfo startTest(String projectId, int appId, int scriptId, String groupId, String timeout,
                       String cloudId, String frameType) {
        try {
            CompatibilityTest compatibilityTest = new CompatibilityTest();
            compatibilityTest.setAppId(appId);
            compatibilityTest.setScriptId(scriptId);
            compatibilityTest.setDevices(getDeviceIdsByGroup(groupId));
            // choose type set by getDeviceIdsByGroup()
            compatibilityTest.setDeviceChooseType(chooseType);

            compatibilityTest.setCloudIds(new int[]{Integer.parseInt(cloudId)});//TODO: support cloud ids
            compatibilityTest.setFrameType(frameType);

            int testTimeout = Integer.parseInt(timeout);//TODO: check timeout format
            compatibilityTest.setMaxDeviceRunTime(testTimeout);
            compatibilityTest.setMaxTestRunTime(testTimeout);

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
        int appid;
        try {
            appid = Integer.parseInt(appPath);
        } catch (NumberFormatException e) {
            App appResp = ctClient.upload.multiPartUploadApk(appPath);
            appid = appResp.appId;
        }
        return appid;
    }

    int uploadScript(String script) throws CloudTestSDKException {
        int appid;
        try {
            appid = Integer.parseInt(script);
        } catch (NumberFormatException e) {
            Script scriptResp = ctClient.upload.multiPartUploadScript(script);
            appid = scriptResp.scriptId;
        }
        return appid;
    }

    List<ProjectInfo> getProjectIds() throws CloudTestSDKException {
        List<ProjectInfo> projects = new ArrayList<>();
        Project[] projectResp = ctClient.account.getProjects();
        if (projectResp != null) {
            for (Project project : projectResp) {
                projects.add(new ProjectInfo(project.projectName, project.projectId));
            }
        }
        return projects;
    }

    List<GroupInfo> getGroupIds() throws CloudTestSDKException {
        List<GroupInfo> groups = new ArrayList<>();
        modelList = ctClient.device.getModelList();
        if (modelList != null) {
            for (ModelList modelList : modelList) {
                groups.add(new GroupInfo(modelList.name, modelList.name));
            }
        }
        groups.add(new GroupInfo("random1", "1"));
        return groups;
    }

    private int[] getDeviceIdsByGroup(String groupName) {
        if (modelList != null) {
            for (ModelList modelList : modelList) {
                if (modelList.name.equals(groupName)) {
                    if (isLegalGroup(modelList.deviceIds)) {
                        chooseType = CHOOSE_TYPE_DEVICE_IDS;
                        return modelList.deviceIds;
                    }
                    if (isLegalGroup(modelList.modelIds)) {
                        chooseType = CHOOSE_TYPE_MODEL_IDS;
                        return modelList.modelIds;
                    }
                }
            }
        }
        return null;
    }

    private boolean isLegalGroup(int[] group) {
        return group != null && (group.length > 1 || group.length == 1 && group[0] != 0); // ignore group[0] = 0
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
