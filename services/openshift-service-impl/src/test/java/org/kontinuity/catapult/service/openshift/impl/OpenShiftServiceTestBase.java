package org.kontinuity.catapult.service.openshift.impl;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kontinuity.catapult.service.openshift.api.*;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public abstract class OpenShiftServiceTestBase {

    private static final Logger log = Logger.getLogger(OpenShiftServiceTestBase.class.getName());

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    private static final Collection<OpenShiftProject> createdProjects = new ArrayList<>();

    protected abstract OpenShiftService getOpenShiftService();
    
    @After
    public void deleteCreatedProject() {
        createdProjects.forEach(project -> {
            final String projectName = project.getName();
            ((OpenShiftServiceSpi) getOpenShiftService()).deleteProject(project);
            log.info("Deleted " + projectName);
        });
    }
    
    @Test
    public void createProject() {
        final String projectName = getUniqueProjectName();
        final OpenShiftProject project = getOpenShiftService().createProject(projectName);
        final String name = project.getName();
        createdProjects.add(project);
        Assert.assertEquals("returned project did not have expected name", projectName, name);
    }

    @Test(expected = DuplicateProjectException.class)
    public void duplicateProjectNameShouldFail() {
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        final String name = project.getName();
        getOpenShiftService().createProject(name); // Using same name should fail with DPE here
        // Just in case the above doesn't fail
        createdProjects.add(project);
    }

    private String getUniqueProjectName(){
        return PREFIX_NAME_PROJECT + System.currentTimeMillis();
    }

	private OpenShiftProject triggerCreateProject(final String projectName) {
    	final OpenShiftProject project = getOpenShiftService().createProject(projectName);
    	log.log(Level.INFO, "Created project: \'" + projectName + "\'");
    	createdProjects.add(project);
    	return project;
	}
}
