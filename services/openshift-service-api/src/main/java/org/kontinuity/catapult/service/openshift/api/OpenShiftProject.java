package org.kontinuity.catapult.service.openshift.api;

/**
 * Represents a Project in OpenShift
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftProject {

    /**
     * Returns the name of this project
     * @return
     */
    String getName();

}
