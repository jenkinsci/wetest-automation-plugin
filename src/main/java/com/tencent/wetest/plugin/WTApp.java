package com.tencent.wetest.plugin;

import hudson.Extension;
import jenkins.model.Jenkins;

@Extension
public class WTApp {

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
