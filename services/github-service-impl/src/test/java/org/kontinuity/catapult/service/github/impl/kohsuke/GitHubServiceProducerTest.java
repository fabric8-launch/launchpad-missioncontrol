package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.junit.Assert;
import org.junit.Test;
import org.kontinuity.catapult.base.identity.IdentityFactory;
import org.kontinuity.catapult.service.github.api.GitHubService;

/**
 * Tests for the {@link GitHubServiceFactoryImpl}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceProducerTest {

    @Test(expected = IllegalArgumentException.class)
    public void identityCannotBeNull() {
        new GitHubServiceFactoryImpl().create(null);
    }

    @Test
    public void createsInstance() {
        // when
        final GitHubService service = new GitHubServiceFactoryImpl().create(IdentityFactory.usingUserPassword("test", "test"));
        // then
        Assert.assertNotNull("instance was not created", service);
    }
}
