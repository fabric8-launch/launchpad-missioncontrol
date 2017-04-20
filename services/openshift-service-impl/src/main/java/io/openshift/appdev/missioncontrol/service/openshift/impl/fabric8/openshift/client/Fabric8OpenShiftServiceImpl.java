package io.openshift.appdev.missioncontrol.service.openshift.impl.fabric8.openshift.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.Parameter;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.base.identity.IdentityVisitor;
import io.openshift.appdev.missioncontrol.base.identity.TokenIdentity;
import io.openshift.appdev.missioncontrol.base.identity.UserPasswordIdentity;
import io.openshift.appdev.missioncontrol.service.openshift.api.DuplicateProjectException;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftResource;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.impl.OpenShiftProjectImpl;
import io.openshift.appdev.missioncontrol.service.openshift.impl.OpenShiftResourceImpl;
import io.openshift.appdev.missioncontrol.service.openshift.spi.OpenShiftServiceSpi;

/**
 * Implementation of the {@link OpenShiftService} using the Fabric8
 * OpenShift client
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public final class Fabric8OpenShiftServiceImpl implements OpenShiftService, OpenShiftServiceSpi {

    static {
        // Avoid using ~/.kube/config
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
    }

    /**
     * Name of the JSON file containing the template to apply on the OpenShift
     * project after it has been created.
     */
    public static final String OPENSHIFT_PROJECT_TEMPLATE = "openshift-project-template.json";

    /**
     * Creates an {@link OpenShiftService} implementation communicating
     * with the backend service via the specified, required apiUrl authenticated
     * through the required oauthToken
     *
     * @param apiUrl
     * @param consoleUrl
     * @param identity
     */
    Fabric8OpenShiftServiceImpl(final String apiUrl, final String consoleUrl, final Identity identity) {
        assert apiUrl != null && !apiUrl.isEmpty() : "apiUrl is required";
        assert consoleUrl != null && !consoleUrl.isEmpty() : "consoleUrl is required";
        assert identity != null : "oauthToken is required";
        try {
            this.apiUrl = new URL(apiUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            this.consoleUrl = new URL(consoleUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(apiUrl)
                //TODO Issue #17 never do this in production as it opens us to man-in-the-middle attacks
                .withTrustCerts(true);
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                configBuilder.withOauthToken(token.getToken());
            }

            @Override
            public void visit(UserPasswordIdentity userPassword) {
                configBuilder
                        .withUsername(userPassword.getUsername())
                        .withPassword(userPassword.getPassword());
            }
        });
        final Config config = configBuilder.build();
        final OpenShiftClient client = new DefaultOpenShiftClient(config);
        this.client = client;
    }

    private static final Logger log = Logger.getLogger(Fabric8OpenShiftServiceImpl.class.getName());

    private static final int CODE_DUPLICATE_PROJECT = 409;

    private static final String STATUS_REASON_DUPLICATE_PROJECT = "AlreadyExists";

    private final OpenShiftClient client;

    private final URL apiUrl;

    private final URL consoleUrl;

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

        // Block until exists
        int counter = 0;
        while (true) {
            counter++;
            if (projectExists(name)) {
                // We good
                break;
            }
            if (counter == 10) {
                throw new IllegalStateException("Newly-created project "
                                                        + name + " could not be found ");
            }
            log.finest("Couldn't find project " + name +
                               " after creating; waiting and trying again...");
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException ie) {
                Thread.interrupted();
                throw new RuntimeException("Someone interrupted thread while finding newly-created project", ie);
            }
        }
        // Populate value object and return it
        final String roundtripDisplayName = projectRequest.getMetadata().getName();
        final OpenShiftProject project = new OpenShiftProjectImpl(roundtripDisplayName);

        return project;
    }

    @Override
    public void configureProject(final OpenShiftProject project,
                                 final URI sourceRepositoryUri,
                                 final String gitRef,
                                 final URI pipelineTemplateUri) {
        final InputStream pipelineTemplateStream;
        try {
            pipelineTemplateStream = pipelineTemplateUri.toURL().openStream();
        } catch (IOException e) {
            throw new RuntimeException("Could not create OpenShift pipeline", e);
        }
        List<Parameter> parameters = Arrays.asList(
                createParameter("GIT_URL", sourceRepositoryUri.toString()),
                createParameter("GIT_REF", gitRef));
        configureProject(project, pipelineTemplateStream, parameters);
    }

    @Override
    public void configureProject(final OpenShiftProject project, final URI sourceRepositoryUri) {
        final InputStream pipelineTemplateStream = getClass().getResourceAsStream("/pipeline-template.yml");
        List<Parameter> parameters = Arrays.asList(
                createParameter("SOURCE_REPOSITORY_URL", sourceRepositoryUri.toString()),
                createParameter("PROJECT", project.getName()),
                createParameter("GITHUB_WEBHOOK_SECRET", new Long(System.currentTimeMillis()).toString()));
        configureProject(project, pipelineTemplateStream, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getWebhookUrl(final OpenShiftProject project) throws IllegalArgumentException {
        final URL openshiftConsoleUrl = this.getConsoleUrl();
        final Optional<OpenShiftResource> optionalBuildConfig = project.getResources().stream()
                .filter(r -> r.getKind()
                        .equals("BuildConfig")).findFirst();
        if (optionalBuildConfig.isPresent()) {
            final OpenShiftResource buildConfig = optionalBuildConfig.get();
            // Construct a URL in form:
            // https://<OS_IP>:<OS_PORT>/oapi/v1/namespaces/<project>/buildconfigs/<BC-name/webhooks/<secret>/github
            final String secret = buildConfig.getGitHubWebhookSecret();
            final String webhookContext = new StringBuilder().append("/oapi/v1/namespaces/")
                    .append(project.getName()).append("/buildconfigs/")
                    .append(buildConfig.getName()).append("/webhooks/").append(secret).append("/github")
                    .toString();
            try {
                return new URL(openshiftConsoleUrl.getProtocol(), openshiftConsoleUrl.getHost(),
                               openshiftConsoleUrl.getPort(), webhookContext);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to create Webhook URL for project '" + project.getName()
                                                   + "' using the OpenShift API URL '" + openshiftConsoleUrl.toExternalForm()
                                                   + "' and the webhook context '" + webhookContext + "'", e);
            }
        }
        return null;
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
        return this.deleteProject(projectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(final String projectName) throws IllegalArgumentException {
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("project name must be specified");
        }

        final boolean deleted = client.projects().withName(projectName).delete();
        if (deleted) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "Deleted project: " + projectName);
            }
        }
        return deleted;
    }

    private Parameter createParameter(final String name, final String value) {
        Parameter parameter = new Parameter();
        parameter.setName(name);
        parameter.setValue(value);
        return parameter;
    }

    private void configureProject(final OpenShiftProject project, final InputStream templateStream,
                                  List<Parameter> parameters) {
        try {
            try (final InputStream pipelineTemplateStream = templateStream) {
                final Template template = client.templates().load(pipelineTemplateStream).get();
                for (Parameter parameter : parameters) {
                    if (parameter.getValue() != null) {
                        log.info("Setting the '" + parameter.getName() + "' parameter value to '" + parameter.getValue() + "'.");
                        template.getParameters().stream().filter(p -> p.getName().equals(parameter.getName()))
                                .forEach(p -> p.setValue(parameter.getValue()));
                    }
                }
                log.info("Deploying template '" + template.getMetadata().getName() + "' with parameters:");
                template.getParameters().forEach(p -> log.info("\t" + p.getDisplayName() + '=' + p.getValue()));
                final Controller controller = new Controller(client);
                controller.setNamespace(project.getName());
                final KubernetesList processedTemplate = (KubernetesList) controller.processTemplate(template, OPENSHIFT_PROJECT_TEMPLATE);
                controller.apply(processedTemplate, OPENSHIFT_PROJECT_TEMPLATE);

                // add all template resources into the project
                processedTemplate.getItems().stream()
                        .map(item -> {
                                    String gitHubWebHookSecret = null;
                                    if (item instanceof BuildConfig) {
                                        final BuildConfig bc = (BuildConfig) item;
                                        gitHubWebHookSecret = bc.getSpec().
                                                getTriggers().
                                                stream().
                                                filter(
                                                        r -> r.getGithub() != null).
                                                findFirst().
                                                get().
                                                getGithub().
                                                getSecret();
                                    }
                                    final OpenShiftResource resource = new OpenShiftResourceImpl(
                                            item.getMetadata().getName(),
                                            item.getKind(),
                                            project,
                                            gitHubWebHookSecret);
                                    return resource;
                                }
                        )
                        .forEach(resource -> {
                            log.info("Adding resource '" + resource.getName() + "' (" + resource.getKind()
                                    + ") to project '" + project.getName() + "'");
                            ((OpenShiftProjectImpl) project).addResource(resource);
                        });
            }

            // Add Admin role to the jenkins serviceaccount
            log.info("Adding role admin to jenkins serviceaccount for project '" + project.getName() + "'");
            client.roleBindings()
                    .inNamespace(project.getName())
                    .withName("admin")
                    .edit()
                    .addToUserNames("system:serviceaccount:" + project.getName() + ":jenkins")
                    .addNewSubject().withKind("ServiceAccount").withNamespace(project.getName()).withName("jenkins").endSubject()
                    .done();

        } catch (Exception e) {
            throw new RuntimeException("Could not create OpenShift pipeline", e);
        }
    }

    private URL getConsoleUrl() {
        return consoleUrl;
    }


    @Override
    public boolean projectExists(String name) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        boolean projectExists = client.projects().list().getItems().stream()
                .map(p -> p.getMetadata().getName())
                .anyMatch(Predicate.isEqual(name));
        return projectExists;
    }
}