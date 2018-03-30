package io.fabric8.launcher.base.identity;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import static io.fabric8.launcher.base.identity.TokenIdentity.Type.AUTHORIZATION;
import static io.fabric8.launcher.base.identity.TokenIdentity.Type.PRIVATE_TOKEN;


public class TokenIdentityTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void shouldDefaultBeTypeAuthorization() {
        TokenIdentity identity = TokenIdentity.of("FOO");

        softly.assertThat(identity.getToken()).isEqualTo("FOO");
        softly.assertThat(identity.getType()).isEqualTo(AUTHORIZATION);
    }
    
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNull() {
        TokenIdentity.of(null);
    }

    @Test
    public void shouldTypeAndTokenFactoryWorkCorrectly() {
        TokenIdentity identity = TokenIdentity.of(PRIVATE_TOKEN, "TOKEN");
        softly.assertThat(identity.getType()).isEqualTo(PRIVATE_TOKEN);
        softly.assertThat(identity.getToken()).isEqualTo("TOKEN");
    }

    @Test
    public void shouldBearerTokenFactoryWorkCorrectly() {
        final String bearerToken = "Bearer TOKEN";
        TokenIdentity identity = TokenIdentity.fromBearerAuthorizationHeader(bearerToken);
        softly.assertThat(identity.getType()).isEqualTo(TokenIdentity.Type.AUTHORIZATION);
        softly.assertThat(identity.getToken()).isEqualTo("TOKEN");
        softly.assertThat(identity.toRequestHeaderValue()).isEqualTo(bearerToken);
    }
}