package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftResource;

/**
 * 
 */
public class OpenShiftResourceImpl implements OpenShiftResource {
	
	/** the resource name. */
	private final String name;
	
	/** the resource type. */
	private final String kind;
	
	/** the parent project. */
	private final OpenShiftProject project;

	/**
	 * Constructor.
	 * @param name the resource name
	 * @param kind the resource kind
	 * @param parent the parent project
	 */
	public OpenShiftResourceImpl(String name, String kind, OpenShiftProject parent) {
		super();
		this.name = name;
		this.kind = kind;
		this.project = parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public OpenShiftProject getProject() {
		return project;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return "[" + this.kind + "] " + this.project.getName() + "." + this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OpenShiftResourceImpl other = (OpenShiftResourceImpl) obj;
		if (kind == null) {
			if (other.kind != null) {
				return false;
			}
		} else if (!kind.equals(other.kind)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (project == null) {
			if (other.project != null) {
				return false;
			}
		} else if (!project.equals(other.project)) {
			return false;
		}
		return true;
	}
	
	
	

}
