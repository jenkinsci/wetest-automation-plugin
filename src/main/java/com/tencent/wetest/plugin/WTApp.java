package com.tencent.wetest.plugin;

import com.cloudtestapi.common.exception.CloudTestSDKException;
import hudson.Extension;
import jenkins.model.Jenkins;

import java.util.logging.Logger;

@Extension
public class WTApp {
    Logger LOGGER = Logger.getLogger(WTApp.class.getSimpleName());

    private WTApiClient wtApiClient = null;

    public static WTApp getInstance() {
        return Jenkins.get().getExtensionList(WTApp.class).stream().findFirst().get();
    }

    static WTApiClient getGlobalApiClient() {
        if (getInstance().wtApiClient == null) {
            initGlobalSettings(new WTSettings.DescriptorImpl());
        }
        return getInstance().wtApiClient;
    }

    static void initGlobalSettings(WTSettings.DescriptorImpl settings) {
        getInstance().wtApiClient = new WTApiClient(settings.getSecretId(),
                settings.getSecretKey(),
                settings.getHostUrl());
    }
}
