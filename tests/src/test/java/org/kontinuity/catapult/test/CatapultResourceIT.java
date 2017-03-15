package org.kontinuity.catapult.test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Validation of the {@link org.kontinuity.catapult.web.api.CatapultResource}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
@Ignore
public class CatapultResourceIT extends CatapultITBase {

    private static final Logger log = Logger.getLogger(CatapultResourceIT.class.getName());

    /**
     * Name of the repo on GitHub to fork into our user namespace
     */
    //TODO #135 Clean up reliance on tnozicka stuff
    private static final String SOURCE_REPO = "redhat-kontinuity/jboss-eap-quickstarts";

    private static final String GIT_REF = "kontinu8";

    private static final String PIPELINE_TEMPLATE_PATH = "helloworld/.openshift-ci_cd/pipeline-template.yaml";

    @Deployment(name = "real", testable = false)
    public static WebArchive getRealDeployment() {
        return Deployments.getMavenBuiltWar();
    }

    @Deployment(name = "test")
    public static WebArchive getTestDeployment() {
        return Deployments.getTestDeployment();
    }

    /**
     * Ensures that an HTTP GET request to the "fling" endpoint is
     * working as contracted
     *
     * @throws IOException
     */
    @Test
    @RunAsClient
    @InSequence(1)
    @OperateOnDeployment("real")
    public void shouldFlingViaHttp() throws IOException {

        // Define the request URL
        final String flingUrl = this.getDeploymentUrl().toExternalForm() + PATH_FLING +
                "?sourceRepo=" +
                SOURCE_REPO +
                "&gitRef=" +
                GIT_REF +
                "&pipelineTemplatePath=" +
                PIPELINE_TEMPLATE_PATH;
        log.info("Request URL: " + URLEncoder.encode(flingUrl, "UTF-8"));

        // Execute the Fling URL which should perform all actions and dump us on the return page
        driver.navigate().to(flingUrl);
        GitHubResourceIT.performGitHubOAuth(
                driver);

        // Ensure we land at *some* OpenShift console page until we can test for the
        // project overview page
        this.assertLanding(driver);
    }

    @Override
    String getSourceRepo() {
        return SOURCE_REPO;
    }
}
