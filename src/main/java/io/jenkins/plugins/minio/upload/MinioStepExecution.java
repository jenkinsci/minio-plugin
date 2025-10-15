package io.jenkins.plugins.minio.upload;

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
import io.jenkins.plugins.minio.ClientUtil;
import io.jenkins.plugins.minio.ConfigHelper;
import io.jenkins.plugins.minio.config.GlobalMinioConfiguration;
import io.jenkins.plugins.minio.config.MinioConfiguration;
import io.minio.*;
import org.apache.commons.lang.StringUtils;

import javax.security.auth.login.CredentialNotFoundException;
import java.util.Arrays;
import java.util.Optional;
import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



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

        MinioClient client = ClientUtil.getClient(step.getHost(), step.getCredentialsId(), run);

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(step.getBucket()).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(step.getBucket()).build());
        }

        String includes = Util.replaceMacro(this.step.getIncludes(), env);
        String excludes = Util.replaceMacro(this.step.getExcludes(), env);
        String targetFolderExpanded = Util.replaceMacro(this.step.getTargetFolder(), env);

        if (!StringUtils.isEmpty(targetFolderExpanded) && !targetFolderExpanded.endsWith("/")) {
            targetFolderExpanded = targetFolderExpanded + "/";
        }

        final String targetFolder = targetFolderExpanded;
        for (FilePath filePath : workspace.list(includes, excludes)) {
            String filename = filePath.getName();
            Path path = Paths.get(filename);
            String contentType = null;
            try {
                contentType = Files.probeContentType(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (contentType == null) {
                contentType = new MimetypesFileTypeMap().getContentType(filename);
            }
            taskListener.getLogger().println(String.format("Storing [%s] in bucket  [%s] , mime [%s] ", filename, step.getBucket(),contentType));
            PutObjectArgs put = PutObjectArgs.builder()
                    .bucket(this.step.getBucket())
                    .object(targetFolder + filename)
                    .stream(filePath.read(), filePath.toVirtualFile().length(), -1)
                    .contentType(contentType)
                    .build();
            client.putObject(put);
        }

        return true;
    }
}
