package io.jenkins.plugins.minio;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import java.util.List;

public class CredentialsHelper {

    private CredentialsHelper() {

    }

    public static ListBoxModel getCredentialsListBox(Item item, String credentialsId, String uri) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId); // (2)
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return result.includeCurrentValue(credentialsId); // (2)
            }
        }

        List<DomainRequirement> domainRequirements = URIRequirementBuilder.fromUri(uri).build();
        return result
                .includeEmptyValue() // (3)
                .includeAs(ACL.SYSTEM, item, StandardUsernamePasswordCredentials.class, domainRequirements) // (4)
                .includeCurrentValue(credentialsId); // (5)
    }

}
