package org.kontinuity.catapult.service.openshift.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kontinuity.catapult.service.openshift.api.DuplicateProjectException;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftServiceFactory;
import org.kontinuity.catapult.service.openshift.api.OpenShiftUrl;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public class OpenShiftServiceIT {

    private static final Logger log = Logger.getLogger(OpenShiftServiceIT.class.getName());

    private static final String PREFIX_NAME_PROJECT = "test-project-";

    private static final Collection<OpenShiftProject> createdProjects = new ArrayList<>();

    private static OpenShiftService service;

    @BeforeClass
    public static void createService() {
        service = OpenShiftServiceFactory.INSTANCE.create(OpenShiftUrl.get());
    }

    @AfterClass
    public static void deleteCreatedProjects() {
        createdProjects.forEach(project -> {
            final String projectName = project.getName();
            ((OpenShiftServiceSpi) service).deleteProject(project);
            log.info("Deleted " + projectName);
        });
    }

    @Test
    public void createProject() {
        final String projectName = getUniqueProjectName();
        final OpenShiftProject project = service.createProject(projectName);
        final String name = project.getName();
        createdProjects.add(project);
        Assert.assertEquals("returned project did not have expected name", projectName, name);
    }

    @Test(expected = DuplicateProjectException.class)
    public void duplicateProjectNameShouldFail() {
        final OpenShiftProject project = triggerCreateProject(getUniqueProjectName());
        final String name = project.getName();
        service.createProject(name); // Using same name should fail with DPE here
        // Just in case the above doesn't fail
        createdProjects.add(project);
    }

    private String getUniqueProjectName(){
        return PREFIX_NAME_PROJECT + System.currentTimeMillis();
    }

	private OpenShiftProject triggerCreateProject(final String projectName) {
    	final OpenShiftProject project = service.createProject(projectName);
    	log.log(Level.INFO, "Created project: \'" + projectName + "\'");
    	createdProjects.add(project);
    	
    	return project;
	}
}
