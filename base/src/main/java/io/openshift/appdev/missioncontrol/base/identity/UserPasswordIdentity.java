package io.openshift.appdev.missioncontrol.base.identity;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class UserPasswordIdentity implements Identity {

    private final String username;
    private final String password;

    UserPasswordIdentity(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("User is required");
        }
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public void accept(IdentityVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPasswordIdentity that = (UserPasswordIdentity) o;

        if (!username.equals(that.username)) return false;
        return password != null ? password.equals(that.password) : that.password == null;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
