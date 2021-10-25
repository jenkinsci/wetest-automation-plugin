package com.tencent.wetest.plugin;

import com.cloudtestapi.CTClient;
import com.cloudtestapi.account.models.Project;
import com.cloudtestapi.common.Credential;
import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.common.profile.ClientProfile;
import com.cloudtestapi.common.profile.HttpProfile;
import com.cloudtestapi.device.models.ModelList;
import com.cloudtestapi.test.models.*;
import com.cloudtestapi.upload.models.App;
import com.cloudtestapi.upload.models.Script;
import com.tencent.wetest.plugin.model.GroupInfo;
import com.tencent.wetest.plugin.model.ProjectInfo;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WTApiClient {
    Logger LOGGER = Logger.getLogger(WTApp.class.getSimpleName());

    public static final String VERSION = "V1.0.0-20211009";

    public static final int DEFAULT_TIMEOUT = 30;
    public static final String DEFAULT_CLOUD_TOOL = "cloudtest";
    public static final String DEFAULT_PROTOCOL_TYPE = HttpProfile.REQ_HTTP;
    public static final String DEFAULT_FRAME_TYPE = "appium";

    private static final String CHOOSE_TYPE_DEVICE_IDS = "deviceids";
    private static final String CHOOSE_TYPE_MODEL_IDS = "modelids";

    public static final int DEFAULT_CASE_TIMEOUT = 10;

    public static final String DEFAULT_ORDER_ACCOUNT_TYPE = "personal";

    public static final int MODEL_LIST_FILTER_TYPE_MODEL = 1;
    public static final int MODEL_LIST_FILTER_TYPE_DEVICE = 2;
    private String secretId;
    private String secretKey;
    private String hostUrl;
    private String toolPath;
    private String protocol;

    private Credential credential;
    private ClientProfile profile;
    private CTClient ctClient;

    private ModelList[] modelList;

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

    TestInfo startTest(String projectId, int appId, int scriptId, String groupId, String timeOut,
                       String frameType, String parserType, String caseTimeOut) throws CloudTestSDKException {
        AutomationTest automationTest = new AutomationTest();
        automationTest.setAppId(appId);
        automationTest.setScriptId(scriptId);
        automationTest.setDevices(getDeviceIdsByGroup(groupId));
        // choose type set by getDeviceIdsByGroup()
        automationTest.setDeviceChooseType(chooseType);

        automationTest.setFrameType(frameType);
        automationTest.setParserType(parserType);
        int testTimeout = Integer.parseInt(timeOut);
        int caseTestTimeout = Integer.parseInt(caseTimeOut);
        automationTest.setMaxTestRunTime(testTimeout * 60);
        automationTest.setMaxCaseRuntime(caseTestTimeout * 60);
        automationTest.setOrderAccountType(DEFAULT_ORDER_ACCOUNT_TYPE);

        if (!StringUtils.isBlank(projectId)) {
            automationTest.setProject(projectId);
        }

        return ctClient.test.startAutomationTest(automationTest);
    }

    int waitTestEnd(long testId) throws CloudTestSDKException {
        while (true) {
            TestStatus testStatus = ctClient.test.getTestStatus(testId);
            try {
                Thread.sleep(10* 1000);
            } catch (InterruptedException e) {
                return 0;
            }
            if (testStatus.finished) {
                return testStatus.statusCode;
            }
        }
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
        } else {
            LOGGER.log(Level.SEVERE, "Get project ids failed: result is null");
        }
        return projects;
    }

    List<GroupInfo> getGroupIds(String groupId) throws CloudTestSDKException {
        List<GroupInfo> groups = new ArrayList<>();
        modelList = ctClient.device.getModelList(groupId);
        if (modelList != null) {
            for (ModelList modelList : modelList) {
                groups.add(new GroupInfo(modelList.name, modelList.name,
                        modelList.cloudName, GetDeviceNums(modelList)));
            }
        } else {
            LOGGER.log(Level.SEVERE, "Get group ids failed: result is null");
        }
        return groups;
    }

    private int[] getDeviceIdsByGroup(String groupName) {
        if (modelList == null) {
            // init modelList by call getGroupIds(String groupId).
            return null;
        }

        for (ModelList modelList : modelList) {
            if (!modelList.name.equals(groupName)) {
                continue;
            }
            switch (modelList.filterType) {
                case MODEL_LIST_FILTER_TYPE_MODEL: {
                    chooseType = CHOOSE_TYPE_MODEL_IDS;
                    return modelList.modelIds;
                }
                case MODEL_LIST_FILTER_TYPE_DEVICE: {
                    chooseType = CHOOSE_TYPE_DEVICE_IDS;
                    return modelList.deviceIds;
                }
                default:
                    //not support filter type
                    LOGGER.log(Level.SEVERE, "Get devices by group failed: unknown filter type "
                            + modelList.filterType);
                    return null;
            }
        }
        return null;
    }

    private int GetDeviceNums(ModelList list) {
        return list.filterType == MODEL_LIST_FILTER_TYPE_MODEL ?
                list.modelIds.length : list.deviceIds.length;
    }
}
