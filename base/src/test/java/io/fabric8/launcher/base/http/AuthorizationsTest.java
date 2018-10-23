package io.fabric8.launcher.base.http;

import io.fabric8.launcher.base.identity.TokenIdentity;
import org.junit.Test;

import static io.fabric8.launcher.base.http.Authorizations.addBearerPrefix;
import static io.fabric8.launcher.base.http.Authorizations.createAuthorization;
import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;
import static io.fabric8.launcher.base.http.Authorizations.isTokenOnly;
import static io.fabric8.launcher.base.http.Authorizations.removeBearerPrefix;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AuthorizationsTest {

    @Test
    public void removeBearerToken() {
        String token = "Bearer foo";
        assertThat(removeBearerPrefix(token)).isEqualTo("foo");
    }

    @Test
    public void addBearerToken() {
        String token = "foo";
        assertThat(addBearerPrefix(token)).isEqualTo("Bearer foo");
    }

    @Test
    public void removeBearerTokenNullReturnsNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> removeBearerPrefix(null));
    }

    @Test
    public void removeBearerTokenWithoutBearerIsTheSame() {
        String token = "NoBearerPrefix";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> removeBearerPrefix(token));
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


    @Test
    public void tokenAuthorizationTypePrependsToken() {
        TokenIdentity foo = TokenIdentity.of("foo");
        String authorization = createAuthorization(foo, AuthorizationType.TOKEN);
        assertThat(authorization).isEqualTo("token foo");
    }

}