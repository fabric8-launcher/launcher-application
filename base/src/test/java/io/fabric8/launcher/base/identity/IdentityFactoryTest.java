package io.fabric8.launcher.base.identity;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactoryTest {

    @Test
    public void testTokenIdentity() {
        TokenIdentity identity = IdentityFactory.createFromToken("FOO");
        Assert.assertThat(identity.getToken(), equalTo("FOO"));
        Assert.assertThat(identity.getType().isPresent(), equalTo(false));
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
    public void testTokenIdentityType() {
        TokenIdentity identity = IdentityFactory.createFromToken("Private-Token", "TOKEN");
        Assert.assertThat(identity.getType().get(), equalTo("Private-Token"));
        Assert.assertThat(identity.getToken(), equalTo("TOKEN"));
    }

}