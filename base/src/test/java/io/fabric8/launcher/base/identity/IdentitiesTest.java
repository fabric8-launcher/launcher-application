package io.fabric8.launcher.base.identity;

import static io.fabric8.launcher.base.identity.Identities.createRequestAuthorizationHeaderKey;
import static io.fabric8.launcher.base.identity.Identities.createRequestAuthorizationHeaderValue;
import static io.fabric8.launcher.base.identity.Identities.isBearerAuthentication;
import static io.fabric8.launcher.base.identity.Identities.isTokenOnly;
import static io.fabric8.launcher.base.identity.TokenIdentity.Type.PRIVATE_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class IdentitiesTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testAuthorizationHeaderUserPassword() {
        UserPasswordIdentity identity = ImmutableUserPasswordIdentity.of("the_user", "p122$");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Basic dGhlX3VzZXI6cDEyMiQ=");
    }

    @Test
    public void testAuthorizationHeaderTokenOtherType() {
        TokenIdentity identity = TokenIdentity.of(PRIVATE_TOKEN, "TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Private-Token");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("TOKEN");
    }

    @Test
    public void testAuthorizationHeaderTokenContainingBearer() {
        TokenIdentity identity = TokenIdentity.fromBearerAuthorizationHeader("Bearer TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Bearer TOKEN");
    }

    @Test
    public void testAuthorizationHeaderTokenWithoutType() {
        TokenIdentity identity = TokenIdentity.of("TOKEN");
        softly.assertThat(createRequestAuthorizationHeaderKey(identity)).isEqualTo("Authorization");
        softly.assertThat(createRequestAuthorizationHeaderValue(identity)).isEqualTo("Bearer TOKEN");
    }

    @Test
    public void removeBearerToken() {
        String token = "Bearer foo";
        assertThat(Identities.removeBearerPrefix(token)).isEqualTo("foo");
    }

    @Test
    public void addBearerToken() {
        String token = "foo";
        assertThat(Identities.addBearerPrefix(token)).isEqualTo("Bearer foo");
    }

    @Test
    public void removeBearerTokenNullReturnsNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Identities.removeBearerPrefix(null));
    }

    @Test
    public void removeBearerTokenWithoutBearerIsTheSame() {
        String token = "NoBearerPrefix";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Identities.removeBearerPrefix(token));
    }

    @Test
    public void shouldBeInvalidBearerAuthentication() {
        assertThat(isBearerAuthentication(null)).isFalse();
        assertThat(isBearerAuthentication("")).isFalse();
        assertThat(isBearerAuthentication("Bearer ")).isFalse();
        assertThat(isBearerAuthentication("Bearer")).isFalse();
        assertThat(isBearerAuthentication("172373737fjnen")).isFalse();
    }

    @Test
    public void shouldBeValidBearerAuthentication() {
        assertThat(isBearerAuthentication("Bearer toto")).isTrue();
    }

    @Test
    public void shouldBeInvalidTokenOnly() {
        assertThat(isTokenOnly(null)).isFalse();
        assertThat(isTokenOnly("")).isFalse();
        assertThat(isTokenOnly("Bearer ")).isFalse();
        assertThat(isTokenOnly("Bearer token")).isFalse();
        assertThat(isTokenOnly(" token")).isFalse();
        assertThat(isTokenOnly("token ")).isFalse();
        assertThat(isTokenOnly("token    ")).isFalse();
    }

    @Test
    public void shouldBeValidTokenOnly() {
        assertThat(isTokenOnly("toto")).isTrue();
    }
}