package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.junit.Assert;
import org.junit.Test;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubServiceFactory;

/**
 * Tests for the {@link GitHubServiceFactory}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void tokenCannotBeEmptyWhenUsingUsername() {
        GitHubServiceFactory.INSTANCE.create("", "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenCannotBeEmptyWhenNotUsingUsername() {
        GitHubServiceFactory.INSTANCE.create("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenCannotBeNullWhenUsingUsername() {
        GitHubServiceFactory.INSTANCE.create(null, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tokenCannotBeNullWhenNotUsingUsername() {
        GitHubServiceFactory.INSTANCE.create(null);
    }

    @Test
    public void createsInstance() {
        final GitHubService service = GitHubServiceFactory.INSTANCE.create("test", "test");
        Assert.assertNotNull("instance was not created", service);
    }
}
