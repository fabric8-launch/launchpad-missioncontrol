package org.kontinuity.catapult.base.identity;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityFactoryTest {

    @Test
    public void testTokenIdentity() {
        TokenIdentity identity = IdentityFactory.usingToken("FOO");
        Assert.assertThat(identity.getToken(), equalTo("FOO"));
    }

    @Test
    public void testUserPasswordIdentity() {
        UserPasswordIdentity identity = IdentityFactory.usingUserPassword("USER", "PASS");
        Assert.assertThat(identity.getUsername(), equalTo("USER"));
        Assert.assertThat(identity.getPassword(), equalTo("PASS"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTokenIdentityNotSupported() {
        IdentityFactory.usingToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserPasswordIdentityNotSupported() {
        IdentityFactory.usingUserPassword(null, "PASS");
    }

    @Test
    public void testUserNullPasswordIdentity() {
        UserPasswordIdentity identity = IdentityFactory.usingUserPassword("USER", null);
        Assert.assertThat(identity.getUsername(), equalTo("USER"));
        Assert.assertThat(identity.getPassword(), nullValue());
    }

}