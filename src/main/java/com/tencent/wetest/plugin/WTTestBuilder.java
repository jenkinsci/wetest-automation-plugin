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
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;

public class WTTestBuilder extends Builder {

    private String projectId;
    private String appPath;
    private String scriptPath;
    private String groupId;
    private String timeout;
    private String cloudId;
    private String framework;
    private String language;

    private int appId;
    private int scriptId;

    @DataBoundConstructor
    public WTTestBuilder(String projectId, String appPath, String scriptPath, String groupId,
                         String timeout, String cloudId, String framework, String language) {
        this.projectId = projectId;
        this.appPath = appPath;
        this.scriptPath = scriptPath;
        this.groupId = groupId;
        this.timeout = timeout;
        this.cloudId = cloudId;
        this.framework = framework;
        this.language = language;
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

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return runTest(build, launcher, listener);
    }

    private boolean runTest(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println(Messages.STARTED_RUN_TEST());
        listener.getLogger().println(Messages.STARTED_UPLOAD_TEST_FILE());
        WTApiClient client = WTApp.getGlobalApiClient();

        try {
            //-----------Step: upload app file ------------------------------
            String appAbsPath = WTUtils.getAbsPath(build.getWorkspace(), appPath);
            if (!WTUtils.isExist(appAbsPath)) {
                listener.getLogger().println(Messages.ERR_UPLOAD_FILE_NOT_FOUND(appAbsPath));
                return false;
            }
            listener.getLogger().println(Messages.READY_UPLOAD_APPLICATION_FILE(appAbsPath));
            appId = client.uploadApp(appAbsPath);
            if (appId <= 0) {
                listener.getLogger().println(Messages.ERR_UPLOAD_APPLICATION_FILE(appAbsPath));
                return false;
            }

            //-----------Step: upload script file ------------------------------
            String scriptAbsPath = WTUtils.getAbsPath(build.getWorkspace(), scriptPath);
            if (!WTUtils.isExist(scriptAbsPath)) {
                listener.getLogger().println(Messages.ERR_UPLOAD_FILE_NOT_FOUND(scriptAbsPath));
                return false;
            }
            listener.getLogger().println(Messages.READY_UPLOAD_SCRIPT_FILE(scriptAbsPath));
            scriptId = client.uploadScript(scriptAbsPath);
            if (scriptId <= 0) {
                listener.getLogger().println(Messages.ERR_UPLOAD_SCRIPT_FILE(scriptAbsPath));
                return false;
            }

            listener.getLogger().println(Messages.READY_RUN_TEST());

            //-----------Step: show all test configs ------------------------------
            printTestConfig(listener);

            //-----------Step: start test ------------------------------
            TestInfo info = WTApp.getGlobalApiClient().startTest(projectId, appId, scriptId,
                    groupId, timeout, cloudId, framework);
            if (info != null) {
                listener.getLogger().println(Messages.SUCCESS_TEST_INFO(info.testId, info.reportUrl));
                listener.getLogger().println(Messages.SUCCESS_RUN_TEST());
            } else {
                listener.getLogger().println(Messages.FAILED_RUN_TEST());
                return false;
            }

            //-----------Step: after test running ------------------------------
        } catch (CloudTestSDKException e) {
            listener.getLogger().println(Messages.ERR_SDK_REQUEST(e.toString()));
            return false;
        } catch (IOException e) {
            listener.getLogger().println(Messages.ERR_SDK_CONNECT(e.toString()));
            return false;
        } catch (InterruptedException e) {
            listener.getLogger().println(Messages.ERR_SDK_UNKNOWN(e.toString()));
            return false;
        }

        return true;
    }

    private void printTestConfig(BuildListener listener) {
        listener.getLogger().println(Messages.CONFIG_INFO_TIPS());
        listener.getLogger().println(Messages.CONFIG_INFO_HOST_URL(WTApp.getGlobalApiClient().getHostUrl()));
        listener.getLogger().println(Messages.CONFIG_INFO_USER_ID(WTApp.getGlobalApiClient().getSecretId()));
        listener.getLogger().println(Messages.CONFIG_INFO_PLUGIN_VERSION(WTApiClient.VERSION));
        listener.getLogger().println(Messages.CONFIG_INFO_PROJECT_ID(projectId));
        listener.getLogger().println(Messages.CONFIG_INFO_APP_ID(appId));
        listener.getLogger().println(Messages.CONFIG_INFO_SCRIPT_ID(scriptId));
        listener.getLogger().println(Messages.CONFIG_INFO_GROUP_Id(groupId));
        listener.getLogger().println(Messages.CONFIG_INFO_FRAME_TYPE(framework));
        listener.getLogger().println(Messages.CONFIG_INFO_LANG(language));
        listener.getLogger().println(Messages.CONFIG_INFO_TIMEOUT(timeout));
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

        default ListBoxModel doFillGroupIdItems(@QueryParameter String projectId) {
            ListBoxModel projectIds = new ListBoxModel();
            try {
                for (WTApiClient.GroupInfo info : WTApp.getGlobalApiClient().getGroupIds(projectId)) {
                    projectIds.add(String.format("%s(%s, %d)", info.group_name, info.cloud_name,
                            info.device_num), info.group_id);
                }
            } catch (CloudTestSDKException e) {
                projectIds.add(EMPTY_OPTION);
            }
            return projectIds;
        }

        default ListBoxModel doFillFrameworkItems() {
            return new ListBoxModel().add(WTApiClient.DEFAULT_FRAME_TYPE);
        }

        default ListBoxModel doFillLanguageItems() {
            return new ListBoxModel().add("python").add("java");
        }
    }
}
