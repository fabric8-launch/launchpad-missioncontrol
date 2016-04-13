package org.kontinuity.catapult.service.openshift.api;

import io.fabric8.openshift.client.ParameterValue;

/**
 * Represents a Template Parameter in OpenShift
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface TemplateParameter {
	
	String getName();
	
	String getValue();
	
	ParameterValue toParameterValue();

}
