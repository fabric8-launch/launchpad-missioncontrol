package org.kontinuity.catapult.service.openshift.impl;

import java.io.File;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftClientServiceImpl;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 *         Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@RunWith(Arquillian.class)
public class OpenShiftServiceIT extends OpenShiftServiceTestBase {

	@Inject
	private OpenShiftService openshiftService;

	@Override
	protected OpenShiftService getOpenShiftService() {
		return this.openshiftService;
	}
	
	private static final Logger log = Logger.getLogger(OpenShiftServiceIT.class.getName());

    /**
     * @return a jar file containing all the required classes to test the {@link GitHubService}
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        final File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        // Create deploy file    
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Fabric8OpenShiftClientServiceImpl.class.getPackage())
                .addPackage(OpenShiftServiceIT.class.getPackage())
                .addPackage(OpenShiftService.class.getPackage())
                .addClass(OpenShiftServiceSpi.class)
                .addAsLibraries(dependencies);
        // Show the deployed structure
        log.fine(war.toString(true)); 
        return war;
    }

}
