package org.kontinuity.catapult.core.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubServiceFactory;
import org.kontinuity.catapult.service.github.api.NoSuchRepositoryException;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;
import org.kontinuity.catapult.service.github.test.GitHubTestCredentials;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

/**
 * Test cases for the {@link Catapult}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class CatapultIT {

    private static final Logger log = Logger.getLogger(CatapultIT.class.getName());
    private static final String GITHUB_SOURCE_REPO_NAME = "kitchensink-html5-mobile";
    private static final String GITHUB_SOURCE_REPO_FULLNAME = "redhat-kontinuity/" + GITHUB_SOURCE_REPO_NAME;
    private static final String PROJECT_TEMPLATE_FILENAME = "pipelinetemplate.json";
    private final Collection<String> openshiftProjectsToDelete = new ArrayList<>();

    @Inject
    private OpenShiftService openShiftService; 
    
    @Inject
    private GitHubServiceFactory gitHubServiceFactory; 
    
    @Inject
    private Catapult catapult;
    
	/**
	 * @return a ear file containing all the required classes and dependencies
	 *         to test the {@link Catapult}
	 */
	@Deployment(testable = true)
	public static WebArchive createDeployment() {
		// Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        // Create deploy file    
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
        		.addPackage(Catapult.class.getPackage())
                .addPackage(CatapultImpl.class.getPackage())
                .addPackage(GitHubTestCredentials.class.getPackage())
                .addAsWebInfResource("META-INF/jboss-deployment-structure.xml", "jboss-deployment-structure.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(dependencies);
        // Show the deployed structure
        log.info(war.toString(true)); 
        return war;
	}

	@Before
	@After
	public void reset() {
		// also, make sure that the GitHub user's account does not already contain the repo to fork
		// After the test remove the repository we created
		final String repositoryName = GitHubTestCredentials.getUsername() + "/" + GITHUB_SOURCE_REPO_NAME;
		try {
			final GitHubService gitHubService = gitHubServiceFactory.create(GitHubTestCredentials.getToken());
			((GitHubServiceSpi) gitHubService).deleteRepository(repositoryName);
		} catch (NoSuchRepositoryException e) {
			// ignore
			log.info("Repository '" + repositoryName + "' does not exist.");
		}
	}
    
	@Before
	@After
    public void cleanupOpenShiftProjects() {
		openshiftProjectsToDelete.forEach(projectName -> {
			final boolean deleted = ((OpenShiftServiceSpi) openShiftService).deleteProject(projectName);
			if (deleted) {
				log.info("Deleted project: " + projectName);
			}
		});
		openshiftProjectsToDelete.clear();
    }

    @Test
    public void fling() {
        // Define the projectile
        final Projectile projectile = ProjectileBuilder.newInstance().
                gitHubAccessToken(GitHubTestCredentials.getToken()).
                sourceGitHubRepo(GITHUB_SOURCE_REPO_FULLNAME).
                openshiftProjectTemplateFileName(PROJECT_TEMPLATE_FILENAME).
                build();

        // Fling
        final Boom boom = catapult.fling(projectile);

        // Assertions
        final GitHubRepository createdRepo = boom.getCreatedRepository();
        Assert.assertNotNull("repo can not be null", createdRepo);
        final OpenShiftProject createdProject = boom.getCreatedProject();
        Assert.assertNotNull("project can not be null", createdProject);
        final String expectedName = GITHUB_SOURCE_REPO_NAME.substring(
                GITHUB_SOURCE_REPO_NAME.lastIndexOf('/') + 1,
                GITHUB_SOURCE_REPO_NAME.length());
        final String foundName = createdProject.getName();
        log.info("Created OpenShift project: " + foundName);
        openshiftProjectsToDelete.add(foundName);
        Assert.assertEquals(expectedName, foundName);
        /*
           Can't really assert on any of the properties of the
           new repo because they could change in GitHub and
           break our tests
         */
    }

}
