package io.jenkins.plugins.minio.upload;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.minio.CredentialsHelper;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Ronald Kamphuis
 */
public class MinioBuildStep extends Builder implements SimpleBuildStep {

    private String host;
    private String credentialsId;
    private String bucket;
    private String includes;
    private String excludes;
    private String targetFolder;

    @DataBoundConstructor
    public MinioBuildStep(String bucket, String includes) {
        this.bucket = bucket;
        this.includes = includes;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                        @NonNull TaskListener listener) throws AbortException {
        try {
            new MinioStepExecution(run, workspace, env, launcher, listener, this).start();
        } catch (Exception e) {
            run.setResult(Result.FAILURE);
            listener.getLogger().println(String.format("Problem storing objects in Minio: %s", e.getMessage()));
            e.printStackTrace();
            throw new AbortException("Failed to upload build artifacts to Minio");
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

    public String getHost() {
        return host;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getBucket() {
        return bucket;
    }

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    @Symbol("minio")
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
            return "Upload build artifacts to Minio";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
