package org.kontinuity.catapult.service.openshift.api;

import java.net.URL;
import java.util.List;

/**
 * Represents a Project in OpenShift
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftProject {

    /**
     * @return the name of this project
     */
    String getName();

   /**
    * @return the URL of the console overview page for this project
    */
   URL getConsoleOverviewUrl();
   
   /**
    * @return an unmodifiable copy of the list of {@link OpenShiftResource} for this project
    */
   List<OpenShiftResource> getResources();
   
   
   	/**
	 * @param apiUrl the URL to the API on OpenShift
   	 * @return the webhook URL associated with the Build Configuration, which
	 *         GitHub can use to trigger a build upon change pushes.
	 */
   URL getWebhookUrl(URL apiUrl);

}
