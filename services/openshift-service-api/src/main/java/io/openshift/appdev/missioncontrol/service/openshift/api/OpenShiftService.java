package io.openshift.appdev.missioncontrol.service.openshift.api;

import java.net.URI;
import java.net.URL;

/**
 * Defines the operations we support with the OpenShift backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftService {

    /**
     * Creates a project with the specified, required name.
     *
     * @param name the name of the project to create
     * @return the created {@link OpenShiftProject}
     * @throws DuplicateProjectException
     * @throws IllegalArgumentException  If the name is not specified
     */
    OpenShiftProject createProject(String name)
            throws DuplicateProjectException, IllegalArgumentException;

    /**
     * Creates all resources for the given {@code project}, using the given {@code projectTemplate}.
     * The {@code projectTemplate} is processed on the client side and then applied on OpenShift, where all the
     * described resources are created.
     *
     * @param project             the project in which the pipeline will be created
     * @param sourceRepositoryUri the location of the source repository to build the OpenShift application from
     * @param gitRef              The Git ref to use for the project
     * @param pipelineTemplateUri the location of the pipeline template file
     */
    void configureProject(OpenShiftProject project,
                          URI sourceRepositoryUri,
                          String gitRef,
                          URI pipelineTemplateUri);

    /**
     * Creates all resources for the given {@code project}, using a standard project template.
     * The project template creates a pipeline build for the passed {@code sourceRepositoryUri}
     *
     * @param project             the project in which the pipeline will be created
     * @param sourceRepositoryUri the location of the source repository to build the OpenShift application from
     */
    void configureProject(OpenShiftProject project, URI sourceRepositoryUri);

    /**
     * @param project The project for which to construct a webhook URL
     * @return the webhook URL associated with the Build Configuration, which
     * GitHub can use to trigger a build upon change pushes.
     * @throws IllegalArgumentException If the project is not specified
     */
    URL getWebhookUrl(final OpenShiftProject project) throws IllegalArgumentException;

    /**
     * Check if the specified project name exists
     *
     * @param name the project name. Required
     * @return <code>true</code> if the project name exists in this Openshift
     * @throws IllegalArgumentException If the project name is not specified
     */
    boolean projectExists(String name) throws IllegalArgumentException;

}
