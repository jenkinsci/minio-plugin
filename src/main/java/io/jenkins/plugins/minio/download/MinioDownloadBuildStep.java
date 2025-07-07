package io.jenkins.plugins.minio.download;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.minio.CredentialsHelper;
import io.minio.errors.ErrorResponseException;
import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Ronald Kamphuis
 */
public class MinioDownloadBuildStep extends Builder implements SimpleBuildStep {

    private String host;
    private String credentialsId;
    private final String bucket;
    private final String file;
    private String excludes;
    private String targetFolder;
    private boolean failOnNonExisting = true;

    @DataBoundConstructor
    public MinioDownloadBuildStep(String bucket, String file) {
        this.bucket = bucket;
        this.file = file;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                        @NonNull TaskListener listener) throws AbortException {
        try {
            new MinioDownloadStepExecution(run, workspace, env, launcher, listener, this).start();
        } catch (ErrorResponseException e) {
            String code = e.errorResponse().code();
            if (( code.equals("NoSuchBucket") || code.equals("NoSuchKey")) && !this.failOnNonExisting) {
                listener.getLogger().println(String.format(String.format("File [%s] not found in bucket [%s], but failOnNonExisting = false.", env.expand(this.file), this.bucket)));
            } else {
                setFailed(run, listener, e);
            }
        } catch (Exception e) {
            setFailed(run, listener, e);
        }
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @DataBoundSetter
    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    @DataBoundSetter
    public void setFailOnNonExisting(boolean failOnNonExisting) {
        this.failOnNonExisting = failOnNonExisting;
    }

    public String getHost() {
        return host;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getBucket() {
        return bucket;
    }

    public String getFile() {
        return file;
    }

    public String getExcludes() {
        return excludes;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public boolean getFailOnNonExisting() {
        return failOnNonExisting;
    }

    private void setFailed(@NonNull Run<?, ?> run, TaskListener listener, Exception e) throws AbortException {
        listener.getLogger().println(String.format("Problem downloading objects from Minio: %s", e.getMessage()));
        e.printStackTrace();
        throw new AbortException("Failed to download files from Minio");
    }

    @Symbol("minioDownload")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter String credentialsId,
                @QueryParameter String uri
        ) {
            return CredentialsHelper.getCredentialsListBox(item, credentialsId, uri);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Download files from Minio";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
