package org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.BuildConfigList;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.impl.OpenShiftProjectImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link OpenShiftService} using the Fabric8
 * OpenShift client
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
final class Fabric8OpenShiftClientServiceImpl implements OpenShiftService, OpenShiftServiceSpi {

    private static final Logger log = Logger.getLogger(Fabric8OpenShiftClientServiceImpl.class.getName());

    private static final int CODE_DUPLICATE_PROJECT = 409;
    private static final String STATUS_REASON_DUPLICATE_PROJECT = "AlreadyExists";

    private final OpenShiftClient client;

    /**
     * Creates an {@link OpenShiftService} implementation communicating
     * with the backend service via the specified, required apiUrl
     *
     * @param apiUrl
     */
    Fabric8OpenShiftClientServiceImpl(final String apiUrl) {
        assert apiUrl != null && !apiUrl.isEmpty() : "apiUrl is required";

        final Config config = new ConfigBuilder().
                withMasterUrl(apiUrl).
                withUsername("admin"). //TODO externalize or account for this?
                withPassword("admin"). // TODO externalize or account for this?
                withTrustCerts(true). //TODO never do this in production as it opens us to man-in-the-middle attacks
                build();
        final OpenShiftClient client = new DefaultOpenShiftClient(config);
        this.client = client;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenShiftProject createProject(final String name) throws
            DuplicateProjectException,
            IllegalArgumentException {

        // Create
        final ProjectRequest projectRequest;
        try {
            projectRequest = client.projectrequests().createNew().
                    withNewMetadata().
                    withName(name).
                    endMetadata().
                    done();
        } catch (final KubernetesClientException kce) {
            // Detect if duplicate project
            if (kce.getCode() == CODE_DUPLICATE_PROJECT &&
                    STATUS_REASON_DUPLICATE_PROJECT.equals(kce.getStatus().getReason())) {
                throw new DuplicateProjectException(name);
            }

            // Some other error, rethrow it
            throw kce;
        }

        // Populate value object and return it
        final String roundtripDisplayName = projectRequest.getMetadata().getName();
        final OpenShiftProject project = new OpenShiftProjectImpl(roundtripDisplayName);
        return project;
    }
    
    @Override
	public String getGithubWebhook(String namespace, String applicationName) {
		BuildConfigList builds = client
				.buildConfigs()
				.inNamespace(namespace)
				.withLabel("application", applicationName)
				.list();
		
		String secret = builds
				.getItems()
				.get(0)
				.getSpec()
				.getTriggers()
				.get(0)
				.getGithub()
				.getSecret();

		StringBuilder webhook = new StringBuilder();
		webhook.append(client.getOpenshiftUrl());
		webhook.append("namespaces/");
		webhook.append(namespace);
		webhook.append("/buildconfigs/");
		webhook.append(applicationName);
		webhook.append("/webhooks/");
		webhook.append(secret);
		webhook.append("/github");

		return webhook.toString();
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(final OpenShiftProject project) throws IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("project must be specified");
        }

        final String projectName = project.getName();

        final boolean deleted = client.projects().withName(projectName).delete();
        if (deleted) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "Deleted project: " + projectName);
            }
        }
        return deleted;
    }
}
