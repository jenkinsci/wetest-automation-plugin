package com.tencent.wetest.plugin;

import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.test.models.TestInfo;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

public class WTTestBuilder extends Builder {

    private String projectId;
    private String appPath;
    private String scriptPath;
    private String groupId;
    private String timeout;
    private String cloudId;
    private String frameType;

    @DataBoundConstructor
    public WTTestBuilder(String projectId, String appPath, String scriptPath, String groupId,
                         String timeout, String cloudId, String frameType) {
        this.projectId = projectId;
        this.appPath = appPath;
        this.scriptPath = scriptPath;
        this.groupId = groupId;
        this.timeout = timeout;
        this.cloudId = cloudId;
        this.frameType = frameType;
    }

    public String getAppPath() {
        return appPath;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public String getTimeout() {
        if (StringUtils.isBlank(timeout)) {
            timeout = String.valueOf(WTApiClient.DEFAULT_TIMEOUT);
        }
        return timeout;
    }

    public String getCloudId() {
        if (StringUtils.isBlank(cloudId)) {
            cloudId = String.valueOf(WTApiClient.DEFAULT_CLOUD_ID);
        }
        return cloudId;
    }

    public String getFrameType() {
        if (StringUtils.isBlank(frameType)) {
            frameType = WTApiClient.DEFAULT_FRAME_TYPE;
        }
        return frameType;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return runTest(build, launcher, listener);
    }

    private boolean runTest(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Run WeTest Automated Testing process started...");
        listener.getLogger().println("Waiting for uploading files");
        WTApiClient client = WTApp.getGlobalApiClient();

        listener.getLogger().println("Uploading new app file: " + appPath);
        int appId;
        int scriptId;
        try {
            String absPath = WTUtils.getAbsPath(build.getWorkspace(), appPath);
            appId = client.uploadApp(absPath);
            if (appId <= 0) {
                listener.getLogger().println("Uploading app file failed. new: " + absPath + ", old:" + appPath);
                return false;
            }
            listener.getLogger().println("Uploading new script file: " + scriptPath);
            absPath = WTUtils.getAbsPath(build.getWorkspace(), scriptPath);
            scriptId = client.uploadScript(absPath);
            if (scriptId <= 0) {
                listener.getLogger().println("Uploading script file failed. new: " + absPath + ", old:" + scriptPath);
                return false;
            }
        } catch (CloudTestSDKException | IOException | InterruptedException e) {
            listener.getLogger().println("Uploading file failed, more exception info: " + e.toString());
            return false;
        }

        listener.getLogger().println("Task Configs :" +
                "\nHostUrl: " + WTApp.getGlobalApiClient().getHostUrl() +
                "\nUserId: " + WTApp.getGlobalApiClient().getSecretId() +
                "\nVersion: " + WTApiClient.VERSION +
                "\nProjectId: " + projectId +
                "\nAppId: " + appId +
                "\nScriptId: " + scriptId +
                "\nGroupId: " + groupId +
                "\ncloudId: " + cloudId +
                "\nFrameType: " + frameType +
                "\nTimeout: " + timeout
        );

        listener.getLogger().println("Running tests");
        TestInfo info = WTApp.getGlobalApiClient().startTest(projectId,
                appId, scriptId, groupId, timeout, cloudId, frameType);
        if (info != null) {
            listener.getLogger().println("Start Test, testId: " + info.testId + ", report url: " + info.reportUrl);
            listener.getLogger().println("Run test in WeTest succeeded");
        } else {
            listener.getLogger().println("Run test in WeTest failed");
            return false;
        }

        return true;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> implements Serializable, WTStepDescriptorUtil {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }
    }

    public interface WTStepDescriptorUtil {
        ListBoxModel.Option EMPTY_OPTION = new ListBoxModel.Option(StringUtils.EMPTY, StringUtils.EMPTY);

        default ListBoxModel doFillProjectIdItems() {
            ListBoxModel projectIds = new ListBoxModel();
            projectIds.add(EMPTY_OPTION);
            try {
                for (WTApiClient.ProjectInfo info : WTApp.getGlobalApiClient().getProjectIds()) {
                    projectIds.add(info.project_name, info.project_id);
                }
            } catch (CloudTestSDKException e) {
            }
            return projectIds;
        }

        default ListBoxModel doFillGroupIdItems() {
            ListBoxModel projectIds = new ListBoxModel();
            try {
                for (WTApiClient.GroupInfo info : WTApp.getGlobalApiClient().getGroupIds()) {
                    projectIds.add(info.group_name, info.group_id);
                }
            } catch (CloudTestSDKException e) {
                projectIds.add(EMPTY_OPTION);
            }
            return projectIds;
        }
    }

}
