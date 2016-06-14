package org.kontinuity.catapult.service.openshift.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.rules.ExternalResource;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi;

/**
 * JUnit rule to delete OpenShift projects at the end of a test.
 */
public class DeleteOpenShiftProjectRule extends ExternalResource {

	/** the projects to delete. */
	private final Collection<OpenShiftProject> createdProjects = new ArrayList<>();

	/** the OpenShift service to call to delete the projects. */
	private final OpenShiftServiceSpi openShiftService;

	/**
	 * Constructor
	 * 
	 * @param openShiftService
	 *            the OpenShift service to call to delete the projects.
	 */
	public DeleteOpenShiftProjectRule(final OpenShiftServiceSpi openShiftService) {
		this.openShiftService = openShiftService;
	}

	/**
	 * Adds a project in the list of projects to delete at the end of the test.
	 * 
	 * @param project
	 *            the project to delete
	 */
	public void add(final OpenShiftProject project) {
		createdProjects.add(project);
	}

	@Override
	protected void before() throws Throwable {
		createdProjects.clear();
	}

	@Override
	protected void after() {
		createdProjects.forEach(project -> {
			final String projectName = project.getName();
			openShiftService.deleteProject(project);
			// log.info("Deleted " + projectName);
		});
	}

}
