package org.kontinuity.catapult.core.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.CatapultService;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.impl.kohsuke.GitHubCredentials;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

/**
 * Test cases for the {@link org.kontinuity.catapult.core.api.CatapultService}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class CatapultServiceIT {

    private static final Logger log = Logger.getLogger(CatapultServiceIT.class.getName());
    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";
    private final Collection<String> openshiftProjectsToDelete = new ArrayList<>();

    @Inject
    private OpenShiftService openShiftService; 
    
    @Inject
    private CatapultService catapult;
    
	/**
	 * @return a ear file containing all the required classes and dependencies
	 *         to test the {@link CatapultService}
	 */
	@Deployment(testable = true)
	public static EnterpriseArchive createDeployment() {
		final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "catapult-core-test.jar").addPackage(CatapultService.class.getPackage())
				.addPackage(CatapultServiceImpl.class.getPackage())
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		// Import Maven runtime dependencies
		final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
		        .withTransitivity().asFile();
		// Create the deployable archive
		final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class).addAsLibraries(dependencies)
		        .addAsLibraries(jar);
		// Show the deployed structure
		System.err.println(ear.toString(true));
		return ear;
	}

	@Before
	public void reset() {
		openshiftProjectsToDelete.clear();
	}
    
    @After
    public void cleanup() {
        openshiftProjectsToDelete.forEach(projectName -> {
            final boolean deleted = ((OpenShiftServiceSpi) openShiftService).deleteProject(projectName);
            if (deleted) {
                log.info("Deleted project: " + projectName);
            }
        });
    }

    @Test
    public void fling() {

        // Define the projectile
        //TODO Inject the GitHubServiceProducer
        final Projectile projectile = ProjectileBuilder.newInstance().
                gitHubAccessToken(GitHubCredentials.getToken()).
                sourceGitHubRepo(NAME_GITHUB_SOURCE_REPO).build();

        // Fling
        final Boom boom = catapult.fling(projectile);

        // Assertions
        final GitHubRepository createdRepo = boom.getCreatedRepository();
        Assert.assertNotNull("repo can not be null", createdRepo);
        final OpenShiftProject createdProject = boom.getCreatedProject();
        Assert.assertNotNull("project can not be null", createdProject);
        final String expectedName = NAME_GITHUB_SOURCE_REPO.substring(
                NAME_GITHUB_SOURCE_REPO.lastIndexOf('/') + 1,
                NAME_GITHUB_SOURCE_REPO.length());
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
