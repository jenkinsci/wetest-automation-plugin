package com.tencent.wetest.plugin;

import com.cloudtestapi.CTClient;
import com.cloudtestapi.common.Credential;
import com.cloudtestapi.common.profile.ClientProfile;
import com.cloudtestapi.common.profile.HttpProfile;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

public class WTTestBuilder extends Builder implements SimpleBuildStep {

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }
    }

    private final String name;

    @DataBoundConstructor
    public WTTestBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        showGlobalEnv(listener);
    }

    private void showGlobalEnv(TaskListener listener) {
        WTApiClient apiClient = WTApp.getGlobalApiClient();
        listener.getLogger().println("ENV: " + apiClient.toString());

        // list public cloud devices
        getCloudDeviceInfo(2, listener);
    }

    private void getCloudDeviceInfo(int cloudid, TaskListener listener) {
        WTApiClient apiClient = WTApp.getGlobalApiClient();
        Credential credential = new Credential(apiClient.getSecretId(), apiClient.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setRootDomain(apiClient.getHostUrl());
        httpProfile.setToolPath("cloud_test");
        httpProfile.setProtocol(HttpProfile.REQ_HTTPS);
        // 实例化一个client选项，可选的，没有特殊需求可跳过
        ClientProfile profile = new ClientProfile(ClientProfile.SIGN_SHA256, httpProfile);
        try {
            listener.getLogger().println("Start getDevicesByCloudId");
            CTClient ctClient = new CTClient(credential, profile);
            listener.getLogger().println("Get getDevicesByCloudId 2 : " +
                    ctClient.gson.toJson(ctClient.device.getDevicesByCloudId(cloudid)));
        } catch (Exception e) {
            listener.getLogger().println("Get getDevicesByCloudId : " + e);
        } finally {
            listener.getLogger().println("exit getDevicesByCloudId");
        }
    }
}
