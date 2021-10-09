package com.tencent.wetest.plugin;

import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.test.models.TestInfo;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.EMPTY;

public class WTTestBuilder extends Builder implements SimpleBuildStep {

    private String projectId;
    private String appPath;
    private String scriptPath;
    private String groupId;
    private String timeout;

    @DataBoundConstructor
    public WTTestBuilder(String projectId, String appPath, String scriptPath, String groupId, String timeout) {
        this.projectId = projectId;
        this.appPath = appPath;
        this.scriptPath = scriptPath;
        this.groupId = groupId;
        this.timeout = timeout;
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
        return timeout;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Run WeTest Automated Testing process started...");
        listener.getLogger().println("Waiting for uploading files");
        //TODO: upload apk
        //TODO: upload script

        try {
            showConfig(listener);
        } catch (CloudTestSDKException e) {
            e.printStackTrace();
        }

        listener.getLogger().println("Running tests");
        TestInfo info = WTApp.getGlobalApiClient().startTest(projectId, appPath, scriptPath, groupId, timeout);
        if (info != null) {
            listener.getLogger().println("Start Test, testId: " + info.testId + ", report url: " + info.reportUrl);
            listener.getLogger().println("Run test in WeTest succeeded");
        } else {
            listener.getLogger().println("Run test in WeTest Failed");
        }
    }

    private void showConfig(TaskListener listener) throws CloudTestSDKException {
        listener.getLogger().println("Global ENV:\n" + WTApp.getGlobalApiClient().toString());

        listener.getLogger().println("Test ENV:" +
                "\nprojectId: " + projectId +
                "\ngroupId: " + groupId +
                "\nappPath: " + appPath +
                "\nscriptPath: " + scriptPath
        );
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
        default ListBoxModel doFillProjectIdItems() {
            ListBoxModel projectIds = new ListBoxModel();
            ListBoxModel.Option EMPTY_OPTION = new ListBoxModel.Option(EMPTY, EMPTY);
            projectIds.add(EMPTY_OPTION);
            for (WTApiClient.ProjectInfo info : WTApp.getGlobalApiClient().getProjectIds()) {
                projectIds.add(info.project_name, info.project_id);
            }
            return projectIds;
        }

        default ListBoxModel doFillGroupIdItems() {
            ListBoxModel projectIds = new ListBoxModel();
            for (WTApiClient.GroupInfo info : WTApp.getGlobalApiClient().getGroupIds()) {
                projectIds.add(info.group_name, info.group_id);
            }
            return projectIds;
        }
    }

}
