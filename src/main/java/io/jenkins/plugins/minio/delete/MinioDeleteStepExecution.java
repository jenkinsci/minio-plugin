package io.jenkins.plugins.minio.delete;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.minio.ClientUtil;
import io.jenkins.plugins.minio.download.MinioDownloadBuildStep;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MinioDeleteStepExecution {

    private static final Logger LOGGER = Logger.getLogger(MinioDeleteStepExecution.class.getName());

    private final Run<?, ?> run;
    private final FilePath workspace;
    private final EnvVars env;
    private final Launcher launcher;
    private final TaskListener taskListener;
    private final MinioDeleteBuildStep step;

    public MinioDeleteStepExecution(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                                      @NonNull TaskListener taskListener, @NonNull MinioDeleteBuildStep step) {
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
            throw new MinioException("Bucket '"+ step.getBucket() +"' does not exist");
        }

        List<DeleteObject> objectsToDelete = Arrays.stream(step.getFiles().split(","))
                .map(String::trim)
                .map(env::expand)
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .bucket(step.getBucket())
                .objects(objectsToDelete)
                .build();

        Iterable<Result<DeleteError>> results = client.removeObjects(args);

        for (Result<DeleteError> result : results) {
            DeleteError deleteError = result.get();
            taskListener.getLogger().println(String.format("Deleted %s from bucket %s", deleteError.resource(), step.getBucket()));
        }

        return true;
    }

}
