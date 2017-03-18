package org.kontinuity.catapult.base.identity;

/**
 * Created by georgegastaldi on 17/03/17.
 */
public interface UserPasswordIdentity extends Identity {
    String getUsername();
    String getPassword();

    @Override
    default void accept(IdentityVisitor visitor){
        visitor.visit(this);
    }

}
