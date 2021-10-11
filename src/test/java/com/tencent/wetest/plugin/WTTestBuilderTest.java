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

        WTTestBuilder builder = new WTTestBuilder("", "/Users/kiwim/work/code/config/landtest.apk",
                "/Users/kiwim/work/code/config/sleep1m.zip", "VIVO测试组",
                String.valueOf(WTApiClient.DEFAULT_TIMEOUT),
                String.valueOf(WTApiClient.DEFAULT_CLOUD_ID),
                WTApiClient.DEFAULT_FRAME_TYPE, "", "");

        WTApp.getGlobalApiClient().getGroupIds("projectId");

        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        String expectedString = "report";
        jenkins.assertLogContains(expectedString, build);
    }
}