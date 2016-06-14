package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.net.URISyntaxException;

/**
 * Exception thrown when the path to a repository on GitHub is invalid
 */
public class InvalidPathException extends IllegalArgumentException {

	private static final long serialVersionUID = 450790118418275921L;

	/**
	 * Constructor.
	 * @param message the exception message
	 * @param cause the underlying cause
	 */
	public InvalidPathException(String message, URISyntaxException cause) {
		super(message, cause);
	}
	
	

}
