package org.kontinuity.catapult.base.identity;

/**
 * Created by georgegastaldi on 17/03/17.
 */
public interface IdentityVisitor {


    default void visit(TokenIdentity token) {
        throw new UnsupportedOperationException("Token authentication is not supported");
    }

    default void visit(UserPasswordIdentity userPassword)  {
        throw new UnsupportedOperationException("User/Password authentication is not supported");
    }
}
