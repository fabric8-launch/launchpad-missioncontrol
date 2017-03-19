package org.kontinuity.catapult.base.identity;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class TokenIdentity implements Identity {
    private final String token;

    TokenIdentity(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    @Override
    public void accept(IdentityVisitor visitor) {
        visitor.visit(this);
    }
}
