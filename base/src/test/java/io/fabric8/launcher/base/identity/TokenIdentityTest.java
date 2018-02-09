package io.fabric8.launcher.base.identity;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class TokenIdentityTest {

    @Test
    public void removeBearerToken() {
        String token = "Bearer foo";
        assertThat(TokenIdentity.removeBearerPrefix(token)).isEqualTo("foo");
    }

    @Test
    public void removeBearerTokenNullReturnsNull() {
        String token = null;
        assertThat(TokenIdentity.removeBearerPrefix(token)).isNull();
    }

    @Test
    public void removeBearerTokenWithoutBearerIsTheSame() {
        String token = "AnyToken";
        assertThat(TokenIdentity.removeBearerPrefix(token)).isEqualTo("AnyToken");
    }

}
