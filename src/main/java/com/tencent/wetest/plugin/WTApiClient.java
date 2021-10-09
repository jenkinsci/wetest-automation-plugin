package com.tencent.wetest.plugin;

public class WTApiClient {
    private String secretId;
    private String secretKey;
    private String hostUrl;

    public WTApiClient(String secretId, String secretKey, String hostUrl) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.hostUrl = hostUrl;
    }

    public String getSecretId() {
        return this.secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getHostUrl() {
        return this.hostUrl;
    }

    @Override
    public String toString() {
        return "{\nsecretId:" + secretId + ",\n" +
                "secretKey:" + secretKey + ",\n" +
                "hostUrl:" + hostUrl + "\n}";
    }
}
