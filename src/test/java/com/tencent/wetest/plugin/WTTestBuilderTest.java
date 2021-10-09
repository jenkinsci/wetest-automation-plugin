package com.tencent.wetest.plugin;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class WTTestBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        WTApp.initGlobalSettings(new WTSettings.DescriptorImpl(System.getenv("CT_SECRET_ID"),
                System.getenv("CT_SECRET_KEY"),
                System.getenv("CT_API_DOMAIN"), "cloudtest","http://"));

        WTTestBuilder builder = new WTTestBuilder("", "384", "1420", "1", "600");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        String expectedString = "report";
        jenkins.assertLogContains(expectedString, build);
    }
}