package org.kontinuity.catapult.base.identity;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface IdentityVisitor {

    default void visit(TokenIdentity token) {
        throw new UnsupportedOperationException("Token authentication is not supported");
    }

    default void visit(UserPasswordIdentity userPassword) {
        throw new UnsupportedOperationException("User/Password authentication is not supported");
    }
}
