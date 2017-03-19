package org.kontinuity.catapult.base.identity;

/**
 * Creates {@link Identity} objects
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactory {

    private IdentityFactory(){}

    public static TokenIdentity usingToken(String token) {
        assert token != null && !token.isEmpty() : "token is required";
        return new TokenIdentity(token);
    }

    public static UserPasswordIdentity usingUserPassword(String user, String password) {
        assert user != null && !user.isEmpty() : "user is required";
        return new UserPasswordIdentity(user,password);
    }
}
