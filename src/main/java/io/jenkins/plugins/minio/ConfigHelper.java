package io.jenkins.plugins.minio;

import io.jenkins.plugins.minio.config.GlobalMinioConfiguration;
import io.jenkins.plugins.minio.config.MinioConfiguration;
import org.apache.commons.lang.StringUtils;

public class ConfigHelper {

    private ConfigHelper() {

    }

    public static MinioConfiguration getConfig(String host, String credentialsId) {
        MinioConfiguration config;
        // If host is empty, use global config
        if (StringUtils.isEmpty(host)) {
            config = GlobalMinioConfiguration.get().getConfiguration();
        } else {
            config = new MinioConfiguration();
            config.setHost(host);
            config.setCredentialsId(credentialsId);
        }

        return config;
    }
}
