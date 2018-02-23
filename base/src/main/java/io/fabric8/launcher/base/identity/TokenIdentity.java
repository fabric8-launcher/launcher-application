package io.fabric8.launcher.base.identity;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class TokenIdentity implements Identity {

    private final String type;

    private final String token;


    TokenIdentity(String type, String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        this.type = type;
        this.token = token;
    }

    TokenIdentity(String token) {
        this(null, token);
    }

    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }

    public String getToken() {
        return this.token;
    }


    @Override
    public void accept(IdentityVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenIdentity that = (TokenIdentity) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, token);
    }
}
