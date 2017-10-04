package org.esupportail.activbo.exceptions;

public class LdapLoginAlreadyExistsException extends Exception{
	
	/**
	 * The id for serialization.
	 */
	private static final long serialVersionUID = 8197090501242229324L;

	/**
	 * @param message
	 */
	public LdapLoginAlreadyExistsException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LdapLoginAlreadyExistsException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LdapLoginAlreadyExistsException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
