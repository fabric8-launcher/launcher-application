package io.fabric8.launcher.base.identity;

import io.fabric8.launcher.base.test.EnvironmentVariableController;
import org.junit.Assert;
import org.junit.Test;

import static io.fabric8.launcher.base.identity.IdentityFactory.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD;
import static io.fabric8.launcher.base.identity.IdentityFactory.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN;
import static io.fabric8.launcher.base.identity.IdentityFactory.LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactoryTest {

    @Test
    public void testTokenIdentity() {
        TokenIdentity identity = IdentityFactory.createFromToken("FOO");
        Assert.assertThat(identity.getToken(), equalTo("FOO"));
    }

    @Test
    public void testUserPasswordIdentity() {
        UserPasswordIdentity identity = IdentityFactory.createFromUserPassword("USER", "PASS");
        Assert.assertThat(identity.getUsername(), equalTo("USER"));
        Assert.assertThat(identity.getPassword(), equalTo("PASS"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTokenIdentityNotSupported() {
        IdentityFactory.createFromToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserPasswordIdentityNotSupported() {
        IdentityFactory.createFromUserPassword(null, "PASS");
    }

    @Test
    public void testUserNullPasswordIdentity() {
        UserPasswordIdentity identity = IdentityFactory.createFromUserPassword("USER", null);
        Assert.assertThat(identity.getUsername(), equalTo("USER"));
        Assert.assertThat(identity.getPassword(), nullValue());
    }

    @Test
    public void testDefaultOpenShiftIdentity() {
        // given
        EnvironmentVariableController.setEnv(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN, "token");

        // when
        Identity identity = IdentityFactory.getDefaultOpenShiftIdentity();

        // then
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                Assert.assertThat(token, notNullValue());
                Assert.assertThat(token.getToken(), equalTo("token"));
            }
        });

    }

    @Test
    public void testDefaultOpenShiftIdentityUserPass() {
        // given
        EnvironmentVariableController.setEnv(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME, "user");
        EnvironmentVariableController.setEnv(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD, "pass");

        // when
        Identity identity = IdentityFactory.getDefaultOpenShiftIdentity();

        // then
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(UserPasswordIdentity userPassword) {
                Assert.assertThat(userPassword, notNullValue());
                Assert.assertThat(userPassword.getUsername(), equalTo("user"));
                Assert.assertThat(userPassword.getPassword(), equalTo("pass"));
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultOpenShiftIdentityErrorWhenMissingEnvVar() {
        // given
        EnvironmentVariableController.setEnv(LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME, "user");

        // when
        IdentityFactory.getDefaultOpenShiftIdentity();

        // then
        fail("Exception should have been thrown");
    }
}