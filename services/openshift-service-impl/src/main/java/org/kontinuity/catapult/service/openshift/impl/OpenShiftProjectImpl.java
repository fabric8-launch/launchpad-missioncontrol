package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;

/**
 * Implementation of a value object representing a project in OpenShift
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class OpenShiftProjectImpl implements OpenShiftProject {

    private final String name;

    /**
     * Creates a new {@link OpenShiftProject} value object
     *
     * @param name
     * @throws IllegalArgumentException
     */
    public OpenShiftProjectImpl(final String name) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }
}
