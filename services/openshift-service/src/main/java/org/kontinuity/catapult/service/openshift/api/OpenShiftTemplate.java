package org.kontinuity.catapult.service.openshift.api;

/**
 * Represents a Template in OpenShift
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface OpenShiftTemplate {
	
	String getName();
	
	TemplateParameter[] getParameters();

}
