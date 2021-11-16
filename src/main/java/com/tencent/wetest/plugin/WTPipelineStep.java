package com.tencent.wetest.plugin;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

/**
 * Pipeline build step for WeTest Automation Jenkins plugin.
 *
 * Can be invoked from pipeline like eg:
 * ...
 * steps {
 *     runInWeTestCloud(
 *         projectId: "test",
 *         appPath: "tests.apk",
 *         scriptPath: "tests.zip"
 *     )
 * }
 * ...
 */
public class WTPipelineStep extends AbstractStepImpl {

    private String projectId;
    private String appPath;
    private String scriptPath;
    private String groupId;
    private String timeout;
    private String framework;
    private String language;
    private String caseTimeout;
    private String targetOsType;

    @DataBoundConstructor
    public WTPipelineStep(String projectId, String appPath, String scriptPath, String groupId,
                          String timeout, String framework, String caseTimeout, String targetOsType) {
        this.projectId = projectId;
        this.appPath = appPath;
        this.scriptPath = scriptPath;
        this.groupId = groupId;
        this.timeout = timeout;
        this.framework = framework;
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

    public String getCaseTimeout() {
        return caseTimeout;
    }

    public void setCaseTimeout(String caseTimeout) {
        this.caseTimeout = caseTimeout;
    }

    public String getTargetOsType() {
        return targetOsType;
    }

    public void setTargetOsType(String targetOsType) {
        this.targetOsType = targetOsType;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl implements WTTestBuilder.WTStepDescriptorUtil {

        public DescriptorImpl() {
            super(CloudStepExecution.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }

        @Override
        public String getFunctionName() {
            return "runInWeTestCloud";
        }
    }

    public static final class CloudStepExecution extends AbstractSynchronousNonBlockingStepExecution<Boolean> {

        private static final long serialVersionUID = 1;

        @Inject
        private transient WTPipelineStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient Run<?, ?> build;

        @StepContextParameter
        private transient FilePath workspace;

        @Override
        protected Boolean run() throws IOException, InterruptedException {
            WTTestBuilder builder = new WTTestBuilder(
                    step.getProjectId(),
                    step.getAppPath(),
                    step.getScriptPath(),
                    step.getGroupId(),
                    step.getTimeout(),
                    step.getFramework(),
                    step.getCaseTimeout(),
                    step.getTargetOsType()
            );

            return builder.runTest(build, workspace, launcher, listener);
        }
    }
}
