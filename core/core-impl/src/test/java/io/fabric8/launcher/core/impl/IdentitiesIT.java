package io.fabric8.launcher.core.impl;

import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.core.api.Identities;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.service.github.api.GitHubServiceFactory;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class IdentitiesIT {

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;

    @Inject
    private Identities identities;

    /**
     * @return a war file containing all the required classes and dependencies
     * to test the {@link MissionControl}
     */
    @Deployment
    public static WebArchive createDeployment() {
        return Deployments.createDeployment();
    }

    @Test
    public void defaultGitHubIdentitiesShouldMatch() {
        Identity identity = gitHubServiceFactory.getDefaultIdentity().get();
        Identity gitHubIdentity = identities.getGitHubIdentity(null);
        Assert.assertEquals(identity, gitHubIdentity);
    }

    @Test
    public void defaultOpenShiftIdentitiesShouldMatch() {
        Identity identity = openShiftServiceFactory.getDefaultIdentity().get();
        Identity openShiftIdentity = identities.getOpenShiftIdentity(null, null);
        Assert.assertEquals(identity, openShiftIdentity);
    }
}
