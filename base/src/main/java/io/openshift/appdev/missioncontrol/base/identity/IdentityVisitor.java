package io.openshift.appdev.missioncontrol.base.identity;

/**
 * Visitor pattern used in conjuntion with Identity.  
 * Implementations should consume the corresponding Identity type by implementing the supported methods in this interface.
 * Unimplemented methods throw UnsupportedOperationException.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IdentityVisitor {

    default void visit(TokenIdentity token) {
        throw new UnsupportedOperationException("Token authentication is not supported");
    }

    default void visit(UserPasswordIdentity userPassword) {
        throw new UnsupportedOperationException("User/Password authentication is not supported");
    }
}
