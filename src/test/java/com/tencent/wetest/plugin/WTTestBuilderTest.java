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
                System.getenv("CT_API_DOMAIN"), "cloudtest","https://"));

        WTTestBuilder builder = new WTTestBuilder("",
                System.getenv("CT_TEST_APK"),
                System.getenv("CT_TEST_ZIP"),
                "jenkins-test",
                String.valueOf(WTApiClient.DEFAULT_TIMEOUT),
                WTApiClient.DEFAULT_FRAME_TYPE,
                String.valueOf(WTApiClient.DEFAULT_TIMEOUT), "Android");

        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        String expectedString = "report";
        jenkins.assertLogContains(expectedString, build);
    }
}