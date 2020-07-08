package io.jenkins.plugins.minio;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.minio.config.GlobalMinioConfiguration;
import io.jenkins.plugins.minio.config.MinioConfiguration;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import javax.security.auth.login.CredentialNotFoundException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Ronald Kamphuis
 */
public class MinioStepExecution extends StepExecution {

    private final MinioBuildStep step;

    public MinioStepExecution(MinioBuildStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() throws Exception {
        Run run = Optional.ofNullable(this.getContext().get(Run.class)).orElseThrow(IllegalStateException::new);
        TaskListener listener = Optional.ofNullable(this.getContext().get(TaskListener.class)).orElseThrow(IllegalStateException::new);
        FilePath ws = Optional.ofNullable(this.getContext().get(FilePath.class)).orElseThrow(IllegalStateException::new);
        EnvVars envVars = Optional.ofNullable(this.getContext().get(EnvVars.class)).orElseThrow(IllegalStateException::new);

        MinioConfiguration config = determineConfig();
        StandardUsernamePasswordCredentials credentials = Optional.ofNullable(CredentialsProvider.findCredentialById(config.getCredentialsId(),
                StandardUsernamePasswordCredentials.class,
                run)).orElseThrow(CredentialNotFoundException::new);

        MinioClient client = MinioClient.builder()
                .endpoint(config.getHost())
                .credentials(credentials.getUsername(), credentials.getPassword().getPlainText())
                .build();

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(step.getBucket()).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(step.getBucket()).build());
        }

        String includes = Util.replaceMacro(this.step.getIncludes(), envVars);
        String excludes = Util.replaceMacro(this.step.getExcludes(), envVars);

        Arrays.asList(ws.list(includes, excludes)).forEach(filePath -> {
            String filename = filePath.getName();
            listener.getLogger().print(String.format("Storing %s in bucket %s", filename, step.getBucket()));
            try {
                UploadObjectArgs args = UploadObjectArgs.builder()
                        .bucket(this.step.getBucket())
                        .object(filename)
                        .filename(filePath.getRemote())
                        .build();

                client.uploadObject(args);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) { // Gotta catch 'em all
                run.setResult(Result.UNSTABLE);
                getContext().onSuccess(false);
            }
        });

        getContext().onSuccess(true);
        return true;
    }

    private MinioConfiguration determineConfig() {
        MinioConfiguration config;
        // If host is empty, use global config
        if (StringUtils.isEmpty(step.getHost())) {
            config = GlobalMinioConfiguration.get().getConfiguration();
        } else {
            config = new MinioConfiguration();
            config.setHost(step.getHost());
            config.setCredentialsId(step.getCredentialsId());
        }

        return config;
    }
}
