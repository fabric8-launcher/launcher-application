package io.fabric8.launcher.base.identity;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactoryTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testTokenIdentity() {
        TokenIdentity identity = IdentityFactory.createFromToken("FOO");

        softly.assertThat(identity.getToken()).isEqualTo("FOO");
        softly.assertThat(identity.getType()).isNotPresent();
    }

    @Test
    public void testUserPasswordIdentity() {
        UserPasswordIdentity identity = IdentityFactory.createFromUserPassword("USER", "PASS");
        softly.assertThat(identity.getUsername()).isEqualTo("USER");
        softly.assertThat(identity.getPassword()).isEqualTo("PASS");
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
        softly.assertThat(identity.getUsername()).isEqualTo("USER");
        softly.assertThat(identity.getPassword()).isNull();
    }

    @Test
    public void testTokenIdentityType() {
        TokenIdentity identity = IdentityFactory.createFromToken("Private-Token", "TOKEN");
        softly.assertThat(identity.getType()).isPresent().contains("Private-Token");
        softly.assertThat(identity.getToken()).isEqualTo("TOKEN");
    }


}