package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftTemplate;
import org.kontinuity.catapult.service.openshift.api.TemplateParameter;

/**
 * Implementation of a value object representing a template in OpenShift
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public class OpenShiftTemplateImpl implements OpenShiftTemplate {
	
	private String templateName;
	
	private TemplateParameter[] parameters;
	
	public OpenShiftTemplateImpl(String templateName, TemplateParameter[] parameters) {
		this.templateName = templateName;
		this.parameters = parameters;
	}

	@Override
	public String getName() {
		return this.templateName;
	}

	@Override
	public TemplateParameter[] getParameters() {
		return this.parameters;
	}

}
