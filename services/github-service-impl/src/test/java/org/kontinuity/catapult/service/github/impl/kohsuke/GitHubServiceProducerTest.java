package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.junit.Assert;
import org.junit.Test;
import org.kontinuity.catapult.service.github.api.GitHubService;

/**
 * Tests for the {@link GitHubServiceFactoryImpl}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceProducerTest {

    @Test(expected = IllegalArgumentException.class)
    public void tokenCannotBeEmptyWhenUsingUsername() {
        new GitHubServiceFactoryImpl().create("", "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenCannotBeNullWhenUsingUsername() {
        new GitHubServiceFactoryImpl().create(null, "test");
    }

    @Test
    public void createsInstance() {
        // when
        final GitHubService service = new GitHubServiceFactoryImpl().create("test", "test");
        // then
        Assert.assertNotNull("instance was not created", service);
    }
}
