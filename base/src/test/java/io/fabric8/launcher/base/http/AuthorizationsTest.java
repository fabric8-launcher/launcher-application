package io.fabric8.launcher.base.http;

import io.fabric8.launcher.base.identity.TokenIdentity;
import org.junit.jupiter.api.Test;

import static io.fabric8.launcher.base.http.Authorizations.addBearerPrefix;
import static io.fabric8.launcher.base.http.Authorizations.createAuthorization;
import static io.fabric8.launcher.base.http.Authorizations.isBearerAuthentication;
import static io.fabric8.launcher.base.http.Authorizations.isTokenOnly;
import static io.fabric8.launcher.base.http.Authorizations.removeBearerPrefix;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AuthorizationsTest {

    @Test
    void removeBearerToken() {
        String token = "Bearer foo";
        assertThat(removeBearerPrefix(token)).isEqualTo("foo");
    }

    @Test
    void addBearerToken() {
        String token = "foo";
        assertThat(addBearerPrefix(token)).isEqualTo("Bearer foo");
    }

    @Test
    void removeBearerTokenNullReturnsNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> removeBearerPrefix(null));
    }

    @Test
    void removeBearerTokenWithoutBearerIsTheSame() {
        String token = "NoBearerPrefix";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> removeBearerPrefix(token));
    }

    @Test
    void shouldBeInvalidBearerAuthentication() {
        assertThat(isBearerAuthentication(null)).isFalse();
        assertThat(isBearerAuthentication("")).isFalse();
        assertThat(isBearerAuthentication("Bearer ")).isFalse();
        assertThat(isBearerAuthentication("Bearer")).isFalse();
        assertThat(isBearerAuthentication("172373737fjnen")).isFalse();
    }

    @Test
    void shouldBeValidBearerAuthentication() {
        assertThat(isBearerAuthentication("Bearer toto")).isTrue();
    }

    @Test
    void shouldBeInvalidTokenOnly() {
        assertThat(isTokenOnly(null)).isFalse();
        assertThat(isTokenOnly("")).isFalse();
        assertThat(isTokenOnly("Bearer ")).isFalse();
        assertThat(isTokenOnly("Bearer token")).isFalse();
        assertThat(isTokenOnly(" token")).isFalse();
        assertThat(isTokenOnly("token ")).isFalse();
        assertThat(isTokenOnly("token    ")).isFalse();
    }

    @Test
    void shouldBeValidTokenOnly() {
        assertThat(isTokenOnly("toto")).isTrue();
    }


    @Test
    void tokenAuthorizationTypePrependsToken() {
        TokenIdentity foo = TokenIdentity.of("foo");
        String authorization = createAuthorization(foo, AuthorizationType.TOKEN);
        assertThat(authorization).isEqualTo("token foo");
    }

}