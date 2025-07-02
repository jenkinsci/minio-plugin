package io.jenkins.plugins.minio.config;

import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlTextInput;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsSessionRule;

import static org.junit.Assert.*;

/**
 * @author Ronald Kamphuis
 */
public class GlobalMinioConfigurationTest {

    private static final String GLOBAL_HOST = "http://localhost:9000/";

    @ClassRule
    public static JenkinsSessionRule sr = new JenkinsSessionRule();

    @Test
    public void uiAndStorage() throws Throwable {
        sr.then(r -> {
            assertNull("not set initially",GlobalMinioConfiguration.get().getConfiguration());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput textbox = config.getInputByName("_.host");
            textbox.setText(GLOBAL_HOST);

            r.submit(config);
            assertEquals("global config page let us edit it", GLOBAL_HOST, GlobalMinioConfiguration.get().getConfiguration().getHost());
        });
        sr.then(r -> {
            assertEquals("still there after restart of Jenkins", GLOBAL_HOST, GlobalMinioConfiguration.get().getConfiguration().getHost());
        });
    }
}
