package com.tencent.wetest.plugin;

import com.cloudtestapi.CTClient;
import com.cloudtestapi.common.Credential;
import com.cloudtestapi.common.profile.ClientProfile;
import com.cloudtestapi.common.profile.HttpProfile;
import com.cloudtestapi.test.models.CompatibilityTest;
import com.cloudtestapi.test.models.TestInfo;

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

    public WTApiClient(String secretId, String secretKey, String hostUrl) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.hostUrl = hostUrl;
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

    public TestInfo startTest(String projectId, String appPath, String scriptPath, String groupId, String timeout) {
        WTApiClient apiClient = WTApp.getGlobalApiClient();
        Credential credential = new Credential(apiClient.getSecretId(), apiClient.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setRootDomain(apiClient.getHostUrl());
        httpProfile.setToolPath(DEFAULT_CLOUD_TOOL);
        httpProfile.setProtocol(HttpProfile.REQ_HTTPS);
        ClientProfile profile = new ClientProfile(ClientProfile.SIGN_SHA256, httpProfile);
        try {
            CTClient ctClient = new CTClient(credential, profile);
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
}
