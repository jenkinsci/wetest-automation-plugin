package com.tencent.wetest.plugin;

import com.cloudtestapi.common.exception.CloudTestSDKException;
import com.cloudtestapi.test.models.TestInfo;
import com.tencent.wetest.plugin.model.GroupInfo;
import com.tencent.wetest.plugin.model.ProjectInfo;
import com.tencent.wetest.plugin.util.FileUtils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WTTestBuilder extends Builder {
    static Logger logger = Logger.getLogger(WTApp.class.getSimpleName());

    private static final String REPORT_URL = "https://console.wetest.net/app/testlab/automation/report/%d";
    public static final int DEFAULT_MAX_TIMEOUT = 90;
    public static final int DEFAULT_MIN_TIMEOUT = 15;
    public static final int DEFAULT_MAX_CASE_TIMEOUT = 30;
    public static final int DEFAULT_MIN_CASE_TIMEOUT = 5;

    public static final String ANDROID_OS_TYPE = "Android";
    public static final String IOS_OS_TYPE = "Ios";
    public static final int IOS_DEVICE_TYPE = 1;

    private String projectId;
    private String appPath;
    private String scriptPath;
    private String groupId;
    private String timeout;
    private String frameType;
    private String caseTimeout;
    private String targetOsType;

    @DataBoundConstructor
    public WTTestBuilder(String projectId, String appPath, String scriptPath, String groupId,
                         String timeout,  String frameType, String caseTimeout,
                         String targetOsType) {
        this.projectId = projectId;
        this.appPath = appPath;
        this.scriptPath = scriptPath;
        this.groupId = groupId;
        this.timeout = timeout;
        this.frameType = frameType;
        this.caseTimeout = caseTimeout;
        this.targetOsType = targetOsType;
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

    public void setCaseTimeout(String caseTimeout) {
        this.caseTimeout = caseTimeout;
    }

    public String getCaseTimeout() {
        if (StringUtils.isBlank(caseTimeout)) {
            caseTimeout = String.valueOf(WTApiClient.DEFAULT_CASE_TIMEOUT);
        }
        return caseTimeout;
    }

    public String getFrameType() {
        return frameType;
    }

    public void setFrameType(String frameType) {
        this.frameType = frameType;
    }

    public String getTargetOsType() {
        return targetOsType;
    }

    public void setTargetOsType(String targetOsType) {
        this.targetOsType = targetOsType;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        return runTest(build, build.getWorkspace(), launcher, listener, false);
    }

    public boolean runTest(Run<?, ?> build, FilePath workPath, Launcher launcher,
                           TaskListener listener, boolean isPipeline)
            throws InterruptedException, IOException {
        listener.getLogger().println(Messages.STARTED_RUN_TEST());
        listener.getLogger().println(Messages.STARTED_UPLOAD_TEST_FILE());
        WTApiClient client = WTApp.getGlobalApiClient();

        try {
            //-----------Step: upload app file ------------------------------
            String appAbsPath = FileUtils.getAbsPath(appPath);
            if (!FileUtils.isExist(appAbsPath)) {
                listener.getLogger().println(Messages.ERR_UPLOAD_FILE_NOT_FOUND(appAbsPath));
                return false;
            }
            listener.getLogger().println(Messages.READY_UPLOAD_APPLICATION_FILE(appAbsPath));
            String appHashId = client.uploadApp(appAbsPath, projectId);
            if (appHashId.isEmpty()) {
                listener.getLogger().println(Messages.ERR_UPLOAD_APPLICATION_FILE(appAbsPath));
                return false;
            }

            //-----------Step: upload script file ------------------------------
            int scriptId = 0;
            String scriptAbsPath = FileUtils.getAbsPath(scriptPath);
            if (!FileUtils.isExist(scriptAbsPath)) {
                if (!WTApiClient.GAME_LOOP_FRAME_TYPE.equals(frameType)) {
                    listener.getLogger().println(Messages.ERR_UPLOAD_FILE_NOT_FOUND(scriptAbsPath));
                    return false;
                }
            } else {
                listener.getLogger().println(Messages.READY_UPLOAD_SCRIPT_FILE(scriptAbsPath));
                scriptId = client.uploadScript(scriptAbsPath);
                if (scriptId <= 0) {
                    listener.getLogger().println(Messages.ERR_UPLOAD_SCRIPT_FILE(scriptAbsPath));
                    return false;
                }
            }

            listener.getLogger().println(Messages.READY_RUN_TEST());

            //-----------Step: show all test configs ------------------------------
            printTestConfig(listener, appHashId, scriptId);

            //-----------Step: start test ------------------------------
            TestInfo info = WTApp.getGlobalApiClient().startTest(projectId, appHashId, scriptId,
                    groupId, timeout, frameType, caseTimeout, isPipeline);
            if (info != null) {
                listener.getLogger().println(Messages.SUCCESS_TEST_INFO(info.testId,
                        String.format(REPORT_URL, info.testId)));
                listener.getLogger().println(Messages.SUCCESS_RUN_TEST());
            } else {
                listener.getLogger().println(Messages.FAILED_RUN_TEST());
                return false;
            }
            listener.getLogger().println(Messages.WAIT_TEST_END());
            // wait test end
            int testStatus = WTApp.getGlobalApiClient().waitTestEnd(info.testId);
            if (testStatus == 1) {
                return true;
            } else {
                listener.getLogger().println(Messages.TEST_FAILED());
                return false;
            }

            //-----------Step: after test running ------------------------------
        } catch (CloudTestSDKException e) {
            listener.getLogger().println(Messages.ERR_SDK_REQUEST(e.getMessage()));
            return false;
        } catch (IOException e) {
            listener.getLogger().println(Messages.ERR_SDK_CONNECT(e.toString()));
            return false;
        } catch (InterruptedException e) {
            listener.getLogger().println(Messages.ERR_SDK_UNKNOWN(e.toString()));
            return false;
        }
    }

    private void printTestConfig(TaskListener listener, String hashAppId, int scriptId) {
        listener.getLogger().println(Messages.CONFIG_INFO_TIPS());
        listener.getLogger().println(Messages.CONFIG_INFO_HOST_URL(WTApp.getGlobalApiClient().getHostUrl()));
        listener.getLogger().println(Messages.CONFIG_INFO_USER_ID(WTApp.getGlobalApiClient().getSecretId()));
        listener.getLogger().println(Messages.CONFIG_INFO_PLUGIN_VERSION(WTApiClient.VERSION));
        listener.getLogger().println(Messages.CONFIG_INFO_PROJECT_ID(projectId));
        listener.getLogger().println(Messages.CONFIG_INFO_APP_ID(hashAppId));
        listener.getLogger().println(Messages.CONFIG_INFO_SCRIPT_ID(scriptId));
        listener.getLogger().println(Messages.CONFIG_INFO_GROUP_Id(groupId));
        listener.getLogger().println(Messages.CONFIG_INFO_FRAME_TYPE(frameType));
        listener.getLogger().println(Messages.CONFIG_INFO_TIMEOUT(timeout));
        listener.getLogger().println(Messages.CONFIG_INFO_CASE_TIMEOUT(caseTimeout));
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
            implements Serializable, WTStepDescriptorUtil {

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

        default ListBoxModel doFillProjectIdItems(@AncestorInPath Item item) {
            ListBoxModel projectIds = new ListBoxModel();
            if ((item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER))
                    || (item != null && !item.hasPermission(Item.CONFIGURE))) {
                return projectIds;
            }
            try {
                for (ProjectInfo info : WTApp.getGlobalApiClient().getProjectIds()) {
                    projectIds.add(info.getProjectName(), info.getProjectEnId());
                }
            } catch (CloudTestSDKException e) {
                logger.log(Level.SEVERE, "doFillProjectIdItems error : " + e);
            }
            return projectIds;
        }

        default ListBoxModel doFillGroupIdItems(@AncestorInPath Item item, @QueryParameter String projectId,
                                                @QueryParameter String targetOsType) {
            ListBoxModel groupIds = new ListBoxModel();
            if ((item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER))
                    || (item != null && !item.hasPermission(Item.CONFIGURE))) {
                    return groupIds;
            }
            try {
                // first create job, project id is empty, set default value
                if (projectId.isEmpty()) {
                    List<ProjectInfo> infos = WTApp.getGlobalApiClient().getProjectIds();
                    if (infos.size() == 0) {
                        return groupIds;
                    }
                    projectId = infos.get(0).getProjectId();
                }

                for (GroupInfo info : WTApp.getGlobalApiClient().getGroupIds(projectId)) {
                    if ((ANDROID_OS_TYPE.equals(targetOsType) && info.getDeviceType() == IOS_DEVICE_TYPE) ||
                            (IOS_OS_TYPE.equals(targetOsType) && info.getDeviceType() != IOS_DEVICE_TYPE)) {
                        continue;
                    }
                    groupIds.add(String.format("%s(%s, %ddevices)", info.getGroupName(), info.getCloudName(),
                            info.getDeviceNum()), info.getGroupId());
                }
            } catch (CloudTestSDKException e) {
                logger.log(Level.SEVERE, "doFillGroupIdItems error : " + e);
                groupIds.add(EMPTY_OPTION);
            }
            return groupIds;
        }

        default FormValidation doCheckProjectId(@AncestorInPath Item item) {
            if ((item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER))
                    || (item != null && !item.hasPermission(Item.CONFIGURE))) {
                return FormValidation.ok();
            }
            try {
                List<ProjectInfo> projectInfos = WTApp.getGlobalApiClient().getProjectIds();
                if (projectInfos.size() == 0) {
                    return FormValidation.error(Messages.SUGGESTION_EMPTY_PROJECT());
                }
            } catch (CloudTestSDKException e) {
                logger.log(Level.SEVERE, "doCheckProjectId error : " + e);
            }
            return FormValidation.ok();
        }

        default FormValidation doCheckGroupId(@AncestorInPath Item item, @QueryParameter String projectId) {
            if ((item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER))
                    || (item != null && !item.hasPermission(Item.CONFIGURE))) {
                return FormValidation.ok();
            }
            try {
                List<GroupInfo>  groupInfos = WTApp.getGlobalApiClient().getGroupIds(projectId);
                if (groupInfos.size() == 0) {
                    return FormValidation.error(Messages.SUGGESTION_EMPTY_GROUP());
                }
            } catch (CloudTestSDKException e) {
                logger.log(Level.SEVERE, "doCheckGroupId error : " + e);
            }
            return FormValidation.ok();
        }


        default ListBoxModel doFillFrameTypeItems() {
            return new ListBoxModel().add(WTApiClient.DEFAULT_FRAME_TYPE).add(WTApiClient.GAME_LOOP_FRAME_TYPE);
        }

        default ListBoxModel doFillTargetOsTypeItems() {

            return new ListBoxModel().add("Android");
        }

        default FormValidation doCheckTimeout(@QueryParameter String value) {
            return checkTimeout(value);
        }

        default FormValidation doCheckCaseTimeout(@QueryParameter String value) {
            return checkCaseTimeout(value);
        }

        default FormValidation checkTimeout(String value) {
            int timeout;
            try {
                timeout = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error(Messages.ERROR_INVALID_TIMEOUT_TYPE());
            }

            if (timeout > DEFAULT_MAX_TIMEOUT || timeout < DEFAULT_MIN_TIMEOUT) {
                return FormValidation.error(Messages.ERROR_INVALID_TIMEOUT_TYPE());
            }
            return FormValidation.ok();
        }

        default FormValidation checkCaseTimeout(String value) {
            int caseTimeout;
            try {
                caseTimeout = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error(Messages.ERROR_INVALID_CASE_TIMEOUT_VALUE());
            }

            if (caseTimeout > DEFAULT_MAX_CASE_TIMEOUT || caseTimeout < DEFAULT_MIN_CASE_TIMEOUT) {
                return FormValidation.error(Messages.ERROR_INVALID_TIMEOUT_TYPE());
            }
            return FormValidation.ok();
        }
    }
}
