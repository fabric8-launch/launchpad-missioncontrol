package org.kontinuity.catapult.base.identity;

/**
 * Represents an identity used by authentication engines.
 *
 * Created by georgegastaldi on 17/03/17.
 */
public interface Identity {
    void accept(IdentityVisitor visitor);
}
