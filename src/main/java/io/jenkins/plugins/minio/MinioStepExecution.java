package io.jenkins.plugins.minio;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
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

import javax.security.auth.login.CredentialNotFoundException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Ronald Kamphuis
 */
public class MinioStepExecution {

    private final Run<?, ?> run;
    private final FilePath workspace;
    private final EnvVars env;
    private final Launcher launcher;
    private final TaskListener taskListener;
    private final MinioBuildStep step;

    public MinioStepExecution(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                              @NonNull TaskListener taskListener, @NonNull MinioBuildStep step) {
        this.run = run;
        this.workspace = workspace;
        this.env = env;
        this.launcher = launcher;
        this.taskListener = taskListener;
        this.step = step;
    }

    public boolean start() throws Exception {

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

        String includes = Util.replaceMacro(this.step.getIncludes(), env);
        String excludes = Util.replaceMacro(this.step.getExcludes(), env);

        Arrays.asList(workspace.list(includes, excludes)).forEach(filePath -> {
            String filename = filePath.getName();
            taskListener.getLogger().print(String.format("Storing %s in bucket %s", filename, step.getBucket()));
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
            }
        });

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
