package org.kontinuity.catapult.base.identity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by georgegastaldi on 17/03/17.
 */
public class IdentityFactoryTest {

    @Test
    public void testTokenIdentity() {
        final AtomicBoolean test = new AtomicBoolean();
        TokenIdentity identity = IdentityFactory.usingToken("FOO");
        Assert.assertThat(identity.getToken(), CoreMatchers.equalTo("FOO"));
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                test.set(true);
            }
        });
        Assert.assertTrue(test.get());
    }

    @Test
    public void testUserPasswordIdentity() {
        final AtomicBoolean test = new AtomicBoolean();
        UserPasswordIdentity identity = IdentityFactory.usingUserPassword("USER", "PASS");
        Assert.assertThat(identity.getUsername(), CoreMatchers.equalTo("USER"));
        Assert.assertThat(identity.getPassword(), CoreMatchers.equalTo("PASS"));
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(UserPasswordIdentity userPasswordIdentity) {
                test.set(true);
            }
        });
        Assert.assertTrue(test.get());
    }

}
