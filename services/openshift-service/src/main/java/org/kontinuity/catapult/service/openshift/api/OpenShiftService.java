package org.kontinuity.catapult.service.openshift.api;

/**
 * Defines the operations we support with the OpenShift backend
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftService {

    /**
     * Creates a project with the specified, required name
     *
     * @param name
     * @return
     * @throws DuplicateProjectException
     * @throws IllegalArgumentException  If the name is not specified
     */
    OpenShiftProject createProject(String name) throws DuplicateProjectException,
            IllegalArgumentException;
    
    /**
     * Create an application with the template name and its parameters.
     * 
     * @param namespace
	 * @param templateName
	 * @param parameters
	 * @return
	 */
	OpenShiftTemplate createApplicationFromTemplate(String namespace, String templateName,
			TemplateParameter... parameters);
	
	/**
	 * Return the Github WebHook from an application.
	 *
	 * @param namespace
	 * @param applicationName
	 * @return
	 */
	String getGithubWebhook(String namespace, String applicationName);

}
