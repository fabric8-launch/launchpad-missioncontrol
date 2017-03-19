package org.kontinuity.catapult.base.identity;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class UserPasswordIdentity implements Identity {

    private final String username;
    private final String password;

    UserPasswordIdentity(String username, String password) {
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

}
