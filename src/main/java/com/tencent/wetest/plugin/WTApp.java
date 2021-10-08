package com.tencent.wetest.plugin;

import jenkins.model.Jenkins;

public class WTApp {
    public static WTApp getInstance() {
        return Jenkins.get().getExtensionList(WTApp.class).stream().findFirst().get();
    }
}
