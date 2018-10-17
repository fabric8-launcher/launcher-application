package io.fabric8.launcher.core.impl.filters;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import javax.enterprise.inject.Vetoed;

import io.fabric8.launcher.base.identity.RSAPublicKeyConverter;
import io.fabric8.launcher.core.spi.PublicKeyProvider;
import org.junit.Test;

import static io.fabric8.launcher.base.test.identity.TokenFixtures.OUTDATED_TOKEN;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.PUBLIC_KEY;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.TOKEN_SIGNED_WITH_DIFFERENT_KEY;
import static io.fabric8.launcher.base.test.identity.TokenFixtures.VALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

public class JWTValidatorTest {

    @Test
    public void should_validate_for_valid_token_and_matching_issuer() {
        // given
        final JWTValidator jwtValidator = new JWTValidator("osio", new FixedPublicKeyProvider());

        // when
        final boolean isValid = jwtValidator.validate(VALID_TOKEN);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    public void should_invalidate_for_valid_token_but_not_matching_issuer() {
        // given
        final JWTValidator jwtValidator = new JWTValidator("launcher", new FixedPublicKeyProvider());

        // when
        final boolean isValid = jwtValidator.validate(VALID_TOKEN);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_invalidate_outdated_token() {
        // given
        final JWTValidator jwtValidator = new JWTValidator("osio", new FixedPublicKeyProvider());

        // when
        final boolean isValid = jwtValidator.validate(OUTDATED_TOKEN);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_invalidate_token_signed_with_different_key() {
        // given
        final JWTValidator jwtValidator = new JWTValidator("osio", new FixedPublicKeyProvider());

        // when
        final boolean isValid = jwtValidator.validate(TOKEN_SIGNED_WITH_DIFFERENT_KEY);

        // then
        assertThat(isValid).isFalse();
    }

    @Vetoed
    public static class FixedPublicKeyProvider implements PublicKeyProvider {
        @Override
        public Optional<RSAPublicKey> getKey(String keyId) {
            return Optional.of(RSAPublicKeyConverter.fromString(PUBLIC_KEY));
        }
    }
}