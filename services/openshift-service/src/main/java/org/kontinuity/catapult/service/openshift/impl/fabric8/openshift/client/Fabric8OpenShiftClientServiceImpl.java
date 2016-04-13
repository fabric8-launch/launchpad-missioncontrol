package org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftTemplate;
import org.kontinuity.catapult.service.openshift.api.TemplateParameter;
import org.kontinuity.catapult.service.openshift.impl.OpenShiftProjectImpl;
import org.kontinuity.catapult.service.openshift.impl.OpenShiftTemplateImpl;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.BuildConfigList;
import io.fabric8.openshift.api.model.Parameter;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.ParameterValue;

/**
 * Implementation of the {@link OpenShiftService} using the Fabric8 OpenShift
 * client
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
final class Fabric8OpenShiftClientServiceImpl implements OpenShiftService, OpenShiftServiceSpi {

	private static final Logger log = Logger.getLogger(Fabric8OpenShiftClientServiceImpl.class.getName());

	private static final int CODE_DUPLICATE_PROJECT = 409;
	private static final String STATUS_REASON_DUPLICATE_PROJECT = "AlreadyExists";

	private final OpenShiftClient client;

	/**
	 * Creates an {@link OpenShiftService} implementation communicating with the
	 * backend service via the specified, required apiUrl
	 *
	 * @param apiUrl
	 */
	Fabric8OpenShiftClientServiceImpl(final String apiUrl) {
		assert apiUrl != null && !apiUrl.isEmpty() : "apiUrl is required";

		final Config config = new ConfigBuilder()
				.withMasterUrl(apiUrl)
				.withUsername("admin") // TODO externalize or account for this?
				.withPassword("admin") // TODO externalize or account for this?
				.withTrustCerts(true) // TODO never do this in production as it opens us to man-in-the-middle attacks
				.build();
		final OpenShiftClient client = new DefaultOpenShiftClient(config);
		this.client = client;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OpenShiftProject createProject(final String name)
			throws DuplicateProjectException, IllegalArgumentException {

		// Create
		final ProjectRequest projectRequest;
		try {
			projectRequest = client.projectrequests().createNew().withNewMetadata().withName(name).endMetadata().done();
		} catch (final KubernetesClientException kce) {
			// Detect if duplicate project
			if (kce.getCode() == CODE_DUPLICATE_PROJECT
					&& STATUS_REASON_DUPLICATE_PROJECT.equals(kce.getStatus().getReason())) {
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
	public OpenShiftTemplate createApplicationFromTemplate(String namespace, String templateName, TemplateParameter... parameters) {
		// Converting the Array of value objects to an Array of ParameterValue
		ParameterValue[] params = new ParameterValue[parameters.length];
		params = Stream
			.of(parameters)
			.map(p -> p.toParameterValue())
			.collect(Collectors.toList())
			.toArray(params);

		// TODO This is the awful implementation I found by creating an application from a template.
		// Basically I retrieve the template from 'openshift' namespace, clone it and create in 
		// another namespace (which the user specifies). Only after that I can process and create the app.
		// Need to find a better way to do this.
		Template t = client
			.templates()
			.inNamespace("openshift")
			.withName(templateName)
			.get();
		
		HasMetadata[] objects = new HasMetadata[t.getObjects().size()];
		Parameter[] p = new Parameter[t.getParameters().size()];
		client.templates().inNamespace(namespace).createNew()
			.withNewMetadata().withName(templateName).endMetadata()
			.addToObjects(t.getObjects().toArray(objects))
			.addToParameters(t.getParameters().toArray(p))
			.done();

		KubernetesList list = client.templates().inNamespace(namespace).withName(templateName).process(params);
		client.lists().inNamespace(namespace).create(list);
		
		// Populate value object and return it
		OpenShiftTemplateImpl templateImpl = new OpenShiftTemplateImpl(templateName, parameters);
		return templateImpl;
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
