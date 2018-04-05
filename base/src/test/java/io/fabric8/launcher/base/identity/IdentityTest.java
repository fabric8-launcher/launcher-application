package io.fabric8.launcher.base.identity;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import static io.fabric8.launcher.base.http.AuthorizationType.BASIC;
import static io.fabric8.launcher.base.http.AuthorizationType.BEARER_TOKEN;
import static io.fabric8.launcher.base.http.AuthorizationType.PRIVATE_TOKEN;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class IdentityTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testUserPasswordIdentity() {
        UserPasswordIdentity identity = ImmutableUserPasswordIdentity.of("the_user", "p122$");
        final String expectedBasicAuth = "Basic dGhlX3VzZXI6cDEyMiQ=";
        softly.assertThat(identity.getDefaultAuthorizationType()).isEqualTo(BASIC);
        softly.assertThat(identity.toRequestAuthorization()).isEqualTo(expectedBasicAuth);
        softly.assertThat(identity.toRequestAuthorization(BASIC)).isEqualTo(expectedBasicAuth);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> identity.toRequestAuthorization(BEARER_TOKEN));
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> identity.toRequestAuthorization(PRIVATE_TOKEN));
    }

    @Test
    public void testTokenIdentity() {
        TokenIdentity identity = TokenIdentity.of("the_token");
        final String expectedBearerAuth = "Bearer the_token";
        softly.assertThat(identity.getDefaultAuthorizationType()).isEqualTo(BEARER_TOKEN);
        softly.assertThat(identity.toRequestAuthorization()).isEqualTo(expectedBearerAuth);
        softly.assertThat(identity.toRequestAuthorization(BEARER_TOKEN)).isEqualTo(expectedBearerAuth);
        softly.assertThat(identity.toRequestAuthorization(PRIVATE_TOKEN)).isEqualTo(identity.getToken());
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> identity.toRequestAuthorization(BASIC));
    }

    @Test
    public void testBearerTokenIdentityFactory() {
        TokenIdentity identity = TokenIdentity.fromBearerAuthorizationHeader("Bearer the_token");
        final String expectedBearerAuth = "Bearer the_token";
        softly.assertThat(identity.getDefaultAuthorizationType()).isEqualTo(BEARER_TOKEN);
        softly.assertThat(identity.toRequestAuthorization()).isEqualTo(expectedBearerAuth);
        softly.assertThat(identity.toRequestAuthorization(BEARER_TOKEN)).isEqualTo(expectedBearerAuth);
        softly.assertThat(identity.toRequestAuthorization(PRIVATE_TOKEN)).isEqualTo(identity.getToken());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> TokenIdentity.fromBearerAuthorizationHeader("the_token"));
    }
}
