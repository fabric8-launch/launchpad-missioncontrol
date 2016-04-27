package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftSettings;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of a value object representing a project in OpenShift
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public final class OpenShiftProjectImpl implements OpenShiftProject {

    private static final String CONSOLE_OVERVIEW_URL_PREFIX = "/console/project/";
    private static final String CONSOLE_OVERVIEW_URL_SUFFIX = "/overview/";

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

   /**
    * {@inheritDoc}
    */
    @Override
    public URL getConsoleOverviewUrl() {
        final StringBuilder sb = new StringBuilder();
        sb.append(OpenShiftSettings.getOpenShiftUrl());
        sb.append(CONSOLE_OVERVIEW_URL_PREFIX);
        sb.append(this.getName());
        sb.append(CONSOLE_OVERVIEW_URL_SUFFIX);
        final URL url;
        try {
            url = new URL(sb.toString());
        } catch (final MalformedURLException murle){
            throw new RuntimeException(murle);
        }

        return url;
    }
}
