package org.kontinuity.catapult.core.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.google.common.io.Files;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the {@link Catapult}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class CatapultIT {

    private static final Logger log = Logger.getLogger(CatapultIT.class.getName());

    //TODO #135 Remove reliance on tzonicka
    private static final String GITHUB_SOURCE_REPO_NAME = "jboss-eap-quickstarts";

    private static final String GITHUB_SOURCE_REPO_FULLNAME = "redhat-kontinuity/" + GITHUB_SOURCE_REPO_NAME;

    private static final String GIT_REF = "kontinu8";

    private static final String PIPELINE_TEMPLATE_PATH = "helloworld/.openshift-ci_cd/pipeline-template.yaml";

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    private final Collection<String> openshiftProjectsToDelete = new ArrayList<>();

    private final Collection<String> githubReposToDelete = new ArrayList<>();

    @Inject
    private OpenShiftService openShiftService;

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private Catapult catapult;

    /**
     * @return a ear file containing all the required classes and dependencies
     * to test the {@link Catapult}
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
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
    public void cleanupGitHubProjects() {
        // also, make sure that the GitHub user's account does not already contain the repo to fork
        // After the test remove the repository we created
        final String repositoryName = GitHubTestCredentials.getUsername() + "/" + GITHUB_SOURCE_REPO_NAME;
        final GitHubService gitHubService = gitHubServiceFactory.create(GitHubTestCredentials.getToken());
        try {
            ((GitHubServiceSpi) gitHubService).deleteRepository(repositoryName);
        } catch (NoSuchRepositoryException e) {
            // ignore
            log.info("Repository '" + repositoryName + "' does not exist.");
        }
        githubReposToDelete.forEach(repoName -> {
            final String fullRepoName = GitHubTestCredentials.getUsername() + '/' + repoName;
            try {
                ((GitHubServiceSpi) gitHubService).deleteRepository(fullRepoName);
                log.info("Deleted GitHub repository: " + fullRepoName);
            } catch (final NoSuchRepositoryException nsre) {
                log.severe("Could not remove GitHub repo " + fullRepoName + ": " + nsre.getMessage());
            }

        });
        githubReposToDelete.clear();
    }

    @Before
    @After
    public void cleanupOpenShiftProjects() {
        openshiftProjectsToDelete.forEach(projectName -> {
            final boolean deleted = ((OpenShiftServiceSpi) openShiftService).deleteProject(projectName);
            if (deleted) {
                log.info("Deleted OpenShift project: " + projectName);
            }
        });
        openshiftProjectsToDelete.clear();
    }

    @Test
    public void flingFork() {
        // Define the projectile with a custom, unique OpenShift project name.
        final String expectedName = getUniqueProjectName();
        final Projectile projectile = ProjectileBuilder.newInstance()
                .gitHubAccessToken(GitHubTestCredentials.getToken())
                .openShiftProjectName(expectedName)
                .forkType()
                .sourceGitHubRepo(GITHUB_SOURCE_REPO_FULLNAME)
                .gitRef(GIT_REF)
                .pipelineTemplatePath(PIPELINE_TEMPLATE_PATH)
                .build();

        // Fling
        final Boom boom = catapult.fling(projectile);

        // Assertions
        assertions(expectedName, boom);
    }

    @Test
    public void flingCreate() {
        // Define the projectile with a custom, unique OpenShift project name.
        final String expectedName = getUniqueProjectName();
        File tempDir = Files.createTempDir();
        final Projectile projectile = ProjectileBuilder.newInstance()
                .gitHubAccessToken(GitHubTestCredentials.getToken())
                .openShiftProjectName(expectedName)
                .createType()
                .projectLocation(tempDir.getPath())
                .build();

        // Mark GitHub repo for deletion
        githubReposToDelete.add(expectedName);

        // Fling
        final Boom boom = catapult.fling(projectile);

        // Assertions
        assertions(expectedName, boom);
    }

    private void assertions(String expectedName, Boom boom) {
        /*
           Can't really assert on any of the properties of the
           new repo because they could change in GitHub and
           break our tests
         */
        final GitHubRepository createdRepo = boom.getCreatedRepository();
        Assert.assertNotNull("repo can not be null", createdRepo);
        final OpenShiftProject createdProject = boom.getCreatedProject();
        Assert.assertNotNull("project can not be null", createdProject);
        final String foundName = createdProject.getName();
        log.info("Created OpenShift project: " + foundName);
        openshiftProjectsToDelete.add(foundName);
        Assert.assertEquals(expectedName, foundName);
        // checking that the Build Config was created.
        assertThat(createdProject.getResources()).isNotNull().hasSize(1);
        assertTrue(createdProject.getResources().get(0).getKind().equals("BuildConfig"));
        assertThat(boom.getGitHubWebhook()).isNotNull();
    }

    private String getUniqueProjectName() {
        return PREFIX_NAME_PROJECT + System.currentTimeMillis();
    }
}
