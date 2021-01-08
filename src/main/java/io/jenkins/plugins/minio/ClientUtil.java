package io.jenkins.plugins.minio;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Run;
import io.jenkins.plugins.minio.config.MinioConfiguration;
import io.minio.MinioClient;

import javax.annotation.Nonnull;
import javax.security.auth.login.CredentialNotFoundException;
import java.util.Optional;

public class ClientUtil {

    public static final MinioClient getClient(String host, String credentialsId, @Nonnull Run<?, ?> run) throws CredentialNotFoundException {
        MinioConfiguration config = ConfigHelper.getConfig(host, credentialsId);
        StandardUsernamePasswordCredentials credentials = Optional.ofNullable(CredentialsProvider.findCredentialById(config.getCredentialsId(),
                StandardUsernamePasswordCredentials.class,
                run)).orElseThrow(CredentialNotFoundException::new);

        return MinioClient.builder()
                .endpoint(config.getHost())
                .credentials(credentials.getUsername(), credentials.getPassword().getPlainText())
                .build();

    }

}
