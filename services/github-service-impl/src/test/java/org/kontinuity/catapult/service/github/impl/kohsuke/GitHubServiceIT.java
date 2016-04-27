package org.kontinuity.catapult.service.github.impl.kohsuke;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;

import javax.inject.Inject;
import java.io.File;
import java.util.logging.Logger;

/**
 * Integration Tests for the {@link GitHubService}
 *
 * Relies on having environment variables set for:
 * GITHUB_USERNAME
 * GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public final class GitHubServiceIT extends GitHubServiceTestBase {

    private static final Logger log = Logger.getLogger(GitHubServiceIT.class.getName());

    @Inject
    private GitHubService githubService;
    
    /**
	 * @return a war file containing all the required classes and dependencies
	 *         to test the {@link GitHubService}
	 */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        // Create deploy file    
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(KohsukeGitHubServiceImpl.class.getPackage())
                .addClass(GitHubCredentials.class)
                .addClass(GitHubServiceSpi.class)
                .addAsLibraries(dependencies);
        // Show the deployed structure
        log.fine(war.toString(true)); 
        return war;
    }

    @Override
    protected GitHubService getGitHubService() {
        return githubService;
    }
}