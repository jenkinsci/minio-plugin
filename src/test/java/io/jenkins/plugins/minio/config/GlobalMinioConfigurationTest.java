package io.jenkins.plugins.minio.config;

import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlTextInput;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ronald Kamphuis
 */
public class GlobalMinioConfigurationTest {

    private static final String GLOBAL_HOST = "http://localhost:9000/";

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    @Test
    public void uiAndStorage() {
        rr.then(r -> {
            assertNull("not set initially", GlobalMinioConfiguration.get().getConfiguration());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput textbox = config.getInputByName("_.host");
            textbox.setText(GLOBAL_HOST);

            r.submit(config);
            assertEquals("global config page let us edit it", GLOBAL_HOST, GlobalMinioConfiguration.get().getConfiguration().getHost());
        });
        rr.then(r -> {
            assertEquals("still there after restart of Jenkins", GLOBAL_HOST, GlobalMinioConfiguration.get().getConfiguration().getHost());
        });
    }
}
