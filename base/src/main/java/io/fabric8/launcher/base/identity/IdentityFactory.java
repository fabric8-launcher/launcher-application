package io.fabric8.launcher.base.identity;

/**
 * Creates {@link Identity} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactory {

    private IdentityFactory() {
    }

    public static TokenIdentity createFromToken(String token) {
        return new TokenIdentity(token);
    }

    public static TokenIdentity createFromToken(String type, String token) {
        return new TokenIdentity(type, token);
    }


    public static UserPasswordIdentity createFromUserPassword(String user, String password) {
        return new UserPasswordIdentity(user, password);
    }
}
