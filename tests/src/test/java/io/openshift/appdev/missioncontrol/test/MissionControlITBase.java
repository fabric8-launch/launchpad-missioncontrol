package io.openshift.appdev.missioncontrol.test;

import java.net.URL;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.spi.GitHubServiceSpi;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.spi.OpenShiftServiceSpi;
import org.openqa.selenium.WebDriver;

/**
 * Base class for building integration tests for the MissionControl; deploys both the real
 * WAR as well as a test deployment to do cleanupCreatedProject when done
 */
abstract class MissionControlITBase {

    private static final Logger log = Logger.getLogger(MissionControlITBase.class.getName());

    // We don't let Drone inject this because we manually-specify the version
    @Drone
    WebDriver driver;

    @ArquillianResource
    private URL deploymentUrl;

    @Inject
    private OpenShiftService openShiftService;

    @Inject
    private GitHubService gitHubService;

    /**
     * Not really a test, but abusing the test model to take advantage
     * of a test-only deployment to help us do some cleanup.  Contains no assertions
     * intentionally.
     */
    @Test
    @InSequence(2)
    @OperateOnDeployment("test")
    public void cleanupCreatedProject() {
        final String project = this.getProjectName();
        final boolean deleted = ((OpenShiftServiceSpi) openShiftService).deleteProject(project);
        log.info("Deleted OpenShift project \"" + project + "\" as part of cleanup: " + deleted);
        ((GitHubServiceSpi)gitHubService).deleteRepository(project);
    }

    URL getDeploymentUrl() {
        return deploymentUrl;
    }

    /**
     * Defines the source repository used for this test
     *
     * @return
     */
    abstract String getProjectName();
}