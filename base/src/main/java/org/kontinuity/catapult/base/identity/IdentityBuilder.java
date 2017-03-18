package org.kontinuity.catapult.base.identity;

/**
 * Created by georgegastaldi on 17/03/17.
 */
public class IdentityBuilder {
    private IdentityBuilder(){}

    public static TokenIdentity usingToken(String token) {
        assert token != null && !token.isEmpty() : "token is required";
        return new TokenIdentityImpl(token);
    }

    public static UserPasswordIdentity usingUserPassword(String user, String password) {
        assert user != null && !user.isEmpty() : "user is required";
        return new UserPasswordIdentityImpl(user,password);
    }

    private static class TokenIdentityImpl implements TokenIdentity {
        private final String token;

        TokenIdentityImpl(String token) {
            this.token = token;
        }

        @Override
        public String getToken() {
            return token;
        }
    }

    private static class UserPasswordIdentityImpl implements UserPasswordIdentity {
        private final String username;
        private final String password;

        UserPasswordIdentityImpl(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }
    }

}
