package io.jenkins.plugins.minio;

import hudson.Util;
import io.jenkins.plugins.minio.config.GlobalMinioConfiguration;
import io.jenkins.plugins.minio.config.MinioConfiguration;

public class ConfigHelper {

    private ConfigHelper() {

    }

    public static MinioConfiguration getConfig(String host, String credentialsId) {
        MinioConfiguration config;
        // If host is empty, use global config
        if (Util.fixEmpty(host) == null) {
            config = GlobalMinioConfiguration.get().getConfiguration();
        } else {
            config = new MinioConfiguration();
            config.setHost(host);
            config.setCredentialsId(credentialsId);
        }

        return config;
    }
}
