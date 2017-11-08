package io.openshift.appdev.missioncontrol.base.identity;

/**
 * Creates {@link Identity} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactory {

    private IdentityFactory(){}

    public static TokenIdentity createFromToken(String token) {
        return new TokenIdentity(token);
    }

    public static UserPasswordIdentity createFromUserPassword(String user, String password) {
        return new UserPasswordIdentity(user,password);
    }
}
