package io.fabric8.launcher.base.identity;

import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderKey;
import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class IdentityHelperTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testAuthorizationHeaderUserPassword() {
        UserPasswordIdentity identity = IdentityFactory.createFromUserPassword("the_user", "p122$");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Basic dGhlX3VzZXI6cDEyMiQ=");
    }

    @Test
    public void testAuthorizationHeaderTokenOtherType() {
        TokenIdentity identity = IdentityFactory.createFromToken("Private-Token", "TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Private-Token");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("TOKEN");
    }

    @Test
    public void testAuthorizationHeaderTokenContainingBearer() {
        TokenIdentity identity = IdentityFactory.createFromToken("Authorization", "Bearer TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Bearer TOKEN");
    }

    @Test
    public void testAuthorizationHeaderTokenWithoutType() {
        TokenIdentity identity = IdentityFactory.createFromToken("TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Bearer TOKEN");
    }

    @Test
    public void testAuthorizationHeaderTokenNotContainingBearer() {
        TokenIdentity identity = IdentityFactory.createFromToken("Authorization","TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Bearer TOKEN");
    }

    @Test
    public void removeBearerToken() {
        String token = "Bearer foo";
        assertThat(IdentityHelper.removeBearerPrefix(token)).isEqualTo("foo");
    }

    @Test
    public void addBearerToken() {
        String token = "Bearer foo";
        String token2 = "foo";
        assertThat(IdentityHelper.addBearerPrefix(token)).isEqualTo("Bearer foo");
        assertThat(IdentityHelper.addBearerPrefix(token2)).isEqualTo("Bearer foo");
    }

    @Test
    public void removeBearerTokenNullReturnsNull() {
        String token = null;
        assertThat(IdentityHelper.removeBearerPrefix(token)).isNull();
    }

    @Test
    public void removeBearerTokenWithoutBearerIsTheSame() {
        String token = "AnyToken";
        assertThat(IdentityHelper.removeBearerPrefix(token)).isEqualTo("AnyToken");
    }


}