package io.jenkins.plugins.minio.download;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.minio.ClientUtil;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class MinioDownloadStepExecution {

    private final Run<?, ?> run;
    private final FilePath workspace;
    private final EnvVars env;
    private final Launcher launcher;
    private final TaskListener taskListener;
    private final MinioDownloadBuildStep step;

    public MinioDownloadStepExecution(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                              @NonNull TaskListener taskListener, @NonNull MinioDownloadBuildStep step) {
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

        String filename = env.expand(step.getFile());
        filename = new File(filename).getName();
        String localFilePath = "";
        if (!StringUtils.isEmpty(step.getTargetFolder())) {
            localFilePath = env.expand(step.getTargetFolder()) + File.separator;
        }
        localFilePath = localFilePath + filename;

        String remoteFile = env.expand(step.getFile());
        taskListener.getLogger().println(String.format("Downloading %s from bucket %s", remoteFile, step.getBucket()));

        OutputStream fileOutputStream = workspace.child(localFilePath).write();
        GetObjectArgs getArgs = GetObjectArgs.builder()
                .bucket(step.getBucket())
                .object(remoteFile)
                .build();

        InputStream minioInputStream = client.getObject(getArgs);
        final byte[] buf = new byte[8192];
        int i = 0;
        while ((i = minioInputStream.read(buf)) != -1) {
            fileOutputStream.write(buf, 0, i);
        }

        return true;
    }
}
