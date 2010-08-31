package org.esupportail.activbo.exceptions;

public class LoginException extends Exception{
	
	/**
	 * The id for serialization.
	 */
	private static final long serialVersionUID = 8197090501242229324L;

	/**
	 * @param message
	 */
	public LoginException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LoginException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LoginException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
