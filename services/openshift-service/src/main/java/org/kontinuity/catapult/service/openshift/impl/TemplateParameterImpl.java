package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.TemplateParameter;

import io.fabric8.openshift.client.ParameterValue;

public class TemplateParameterImpl implements TemplateParameter {
	
	private String name;
	
	private String value;
	
	public TemplateParameterImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public ParameterValue toParameterValue() {
		return new ParameterValue(this.name, this.value);
	}

}
