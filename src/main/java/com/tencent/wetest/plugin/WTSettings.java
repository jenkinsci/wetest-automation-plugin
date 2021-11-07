package com.tencent.wetest.plugin;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Objects;

public class WTSettings implements Describable<WTSettings> {

    @Override
    public Descriptor<WTSettings> getDescriptor() {
        return Jenkins.get().getDescriptorByType(DescriptorImpl.class);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<WTSettings> {
        private static final String DEFAULT_HOST_URL = "pre.api.paas.cloudtest.woa.com";

        String secretId;

        Secret secretKey;

        String hostUrl;

        String toolPath = WTApiClient.DEFAULT_CLOUD_TOOL;

        String protocol = WTApiClient.DEFAULT_PROTOCOL_TYPE;

        public DescriptorImpl() {
            super();
            load();
        }

        DescriptorImpl(String secretId, Secret secretKey, String hostUrl, String toolPath, String protocol) {
            this();
            setHostUrl(hostUrl);
            setSecretId(secretId);
            setSecretKey(secretKey);
            setToolPath(toolPath);
            setProtocol(protocol);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.PLUGIN_NAME();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            req.bindParameters(this);
            super.configure(req, json);
            save();
            return true;
        }

        @Override
        public synchronized void save() {
            setHostUrl(hostUrl);
            setSecretId(secretId);
            setSecretKey(secretKey);
            setToolPath(toolPath);
            setProtocol(protocol);
            WTApp.initGlobalSettings(this);
            super.save();
        }

        @Override
        public synchronized void load() {
            super.load();
        }

        public FormValidation doSaveData(String secretId, Secret secretKey, String hostUrl, String toolPath,
                                         String protocol) {
            setHostUrl(hostUrl);
            setSecretId(secretId);
            setSecretKey(secretKey);
            setToolPath(toolPath);
            setProtocol(protocol);
            save();
            WTApp.initGlobalSettings(this);
            return FormValidation.ok();
        }

        public String getSecretId() {
            return this.secretId;
        }

        public void setSecretId(String secretId) {
            if (!StringUtils.isBlank(secretId)) {
                this.secretId = secretId.trim();
            }
        }

        public void setSecretKey(Secret secretKey) {
            this.secretKey = secretKey;
        }

        public Secret getSecretKey() {
            return secretKey;
        }

        public void setHostUrl(String hostUrl) {
            if (!StringUtils.isBlank(hostUrl)) {
                this.hostUrl = hostUrl.trim();
            }
        }

        public String getHostUrl() {
            if (StringUtils.isBlank(hostUrl)) {
                hostUrl = DEFAULT_HOST_URL;
            }
            return this.hostUrl;
        }

        public void setToolPath(String toolPath) {
            if (!StringUtils.isBlank(toolPath)) {
                this.toolPath = toolPath.trim();
            }
        }

        public String getToolPath() {
            return toolPath;
        }

        public void setProtocol(String protocol) {
            if (!StringUtils.isBlank(protocol)) {
                this.protocol = protocol.trim();
            }
        }

        public String getProtocol() {
            return protocol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DescriptorImpl that = (DescriptorImpl) o;
            return Objects.equals(secretId, that.secretId)
                    && Objects.equals(secretKey, that.secretKey)
                    && Objects.equals(hostUrl, that.hostUrl)
                    && Objects.equals(toolPath, that.toolPath)
                    && Objects.equals(protocol, that.protocol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(secretId, secretKey, hostUrl);
        }
    }
}
