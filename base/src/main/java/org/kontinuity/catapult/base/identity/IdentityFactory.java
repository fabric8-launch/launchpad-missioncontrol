package org.kontinuity.catapult.base.identity;

/**
 * Creates {@link Identity} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactory {

    private IdentityFactory(){}

    public static TokenIdentity usingToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        return new TokenIdentity(token);
    }

    public static UserPasswordIdentity usingUserPassword(String user, String password) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("User is required");
        }
        return new UserPasswordIdentity(user,password);
    }
}
