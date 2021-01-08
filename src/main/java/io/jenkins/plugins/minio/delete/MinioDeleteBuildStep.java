package io.jenkins.plugins.minio.delete;

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
public class MinioDeleteBuildStep extends Builder implements SimpleBuildStep {

    private String host;
    private String credentialsId;
    private String bucket;
    private String files;
    private boolean failOnNonExisting;

    @DataBoundConstructor
    public MinioDeleteBuildStep(String bucket, String files) {
        this.bucket = bucket;
        this.files = files;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                        @NonNull TaskListener listener) throws AbortException {
        try {
            new MinioDeleteStepExecution(run, workspace, env, launcher, listener, this).start();
        } catch (Exception e) {
            run.setResult(Result.FAILURE);
            listener.getLogger().println(String.format("Problem deleting objects from Minio: %s", e.getMessage()));
            e.printStackTrace();
            throw new AbortException("Failed to delete objects from Minio");
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

    public String getFiles() {
        return files;
    }

    public boolean getFailOnNonExisting() {
        return failOnNonExisting;
    }
    
    @Symbol("minioDelete")
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
            return "Delete build artifacts from Minio";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
