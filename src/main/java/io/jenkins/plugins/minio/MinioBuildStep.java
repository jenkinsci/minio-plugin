package io.jenkins.plugins.minio;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class MinioBuildStep extends Step implements Serializable {

    private static final long serialVersionUID = 3L;

    private String host;
    private String credentialsId;
    private String bucket;
    private String includes;
    private String excludes;

    @DataBoundConstructor
    public MinioBuildStep(String bucket, String includes) {
        this.bucket = bucket;
        this.includes = includes;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new MinioStepExecution(this, stepContext);
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

    @Override
    public StepDescriptor getDescriptor() {
        return new DescriptorImpl();
    }

    @Symbol("minio")
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<Class<?>>() {{
                add(Run.class);
                add(TaskListener.class);
                add(FilePath.class);
                add(EnvVars.class);
            }};
        }

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter String credentialsId,
                @QueryParameter String uri
        ) {
            return CredentialsHelper.getCredentialsListBox(item, credentialsId, uri);
        }

        @Override
        public String getFunctionName() {
            return "minio";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Upload build artifacts to Minio";
        }
    }
}
