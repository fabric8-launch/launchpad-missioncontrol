package org.kontinuity.catapult.base.identity;

/**
 * Created by georgegastaldi on 17/03/17.
 */
public interface TokenIdentity extends Identity {
    String getToken();

    @Override
    default void accept(IdentityVisitor visitor){
        visitor.visit(this);
    }
}
