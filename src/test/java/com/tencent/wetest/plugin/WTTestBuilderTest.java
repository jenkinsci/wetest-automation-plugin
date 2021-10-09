package com.tencent.wetest.plugin;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class WTTestBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String name = "Bobby";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new WTTestBuilder(name));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new WTTestBuilder(name), project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();

        WTTestBuilder builder = new WTTestBuilder(name);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        String expectedString = "device";
        jenkins.assertLogContains(expectedString, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  greet '" + name + "'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "device";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}