package org.kontinuity.catapult.base.identity;

/**
 * Marker Interface
 * Created by georgegastaldi on 17/03/17.
 */
public interface Identity {
    void accept(IdentityVisitor visitor);
}
