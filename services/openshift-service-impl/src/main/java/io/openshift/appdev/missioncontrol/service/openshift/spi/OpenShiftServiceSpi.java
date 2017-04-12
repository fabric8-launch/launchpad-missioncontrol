package io.openshift.appdev.missioncontrol.service.openshift.spi;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;

/**
 * Defines the service provider interface for implementations of {@link OpenShiftService}
 * that we won't expose in the API but need for testing or other purposes
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftServiceSpi extends OpenShiftService {

    /**
     * Deletes the specified, required project
     *
     * @param project
     * @return If the operation resulted in a deletion
     * @throws IllegalArgumentException If the project is not specified
     */
    boolean deleteProject(OpenShiftProject project) throws IllegalArgumentException;

    /**
     * Deletes the specified, required project
     *
     * @param projectName
     * @return If the operation resulted in a deletion
     * @throws IllegalArgumentException If the project is not specified
     */
    boolean deleteProject(String projectName) throws IllegalArgumentException;

    /**
     * Check if the specified project name exists
     *
     * @param name the project name. Required
     * @return <code>true</code> if the project name exists in this Openshift
     * @throws IllegalArgumentException If the project name is not specified
     */
    boolean projectExists(String name) throws IllegalArgumentException;


}
