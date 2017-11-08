package io.openshift.appdev.missioncontrol.base.identity;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class TokenIdentity implements Identity {
    private final String token;

    TokenIdentity(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        this.token = token;
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

        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
