
package io.jenkins.plugins.minio.config;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

@Extension
public class GlobalMinioConfiguration extends GlobalConfiguration {

    private MinioConfiguration configuration;

    @Nonnull
    public static GlobalMinioConfiguration get() {
        GlobalMinioConfiguration instance = GlobalConfiguration.all().get(GlobalMinioConfiguration.class);
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public GlobalMinioConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    @DataBoundSetter
    public void setConfiguration(MinioConfiguration configuration) {
        this.configuration = configuration;
        save();
    }

    public MinioConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        return true;
    }
}
