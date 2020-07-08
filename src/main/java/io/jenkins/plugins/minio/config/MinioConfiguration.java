package io.jenkins.plugins.minio.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.minio.CredentialsHelper;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;

/**
 * @author Ronald Kamphuis
 */
public class MinioConfiguration extends AbstractDescribableImpl<MinioConfiguration> implements Serializable {

    private static final long serialVersionUID = 4L;

    private String host;
    private String credentialsId;

    public MinioConfiguration() {

    }

    @DataBoundConstructor
    public MinioConfiguration(String host, String credentialsId) {
        this.host = host;
        this.credentialsId = credentialsId;
    }

    public String getHost() {
        return host;
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<MinioConfiguration> {
        @Override
        public String getDisplayName() {
            return "Minio Configuration";
        }

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter String credentialsId,
                @QueryParameter String uri
        ) {
            return CredentialsHelper.getCredentialsListBox(item, credentialsId, uri);
        }
    }
}
