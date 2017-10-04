package org.esupportail.activbo.exceptions;

public class LoginAlreadyExistsException extends Exception{
	
	/**
	 * The id for serialization.
	 */
	private static final long serialVersionUID = 8197090501242229324L;

	/**
	 * @param message
	 */
	public LoginAlreadyExistsException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LoginAlreadyExistsException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LoginAlreadyExistsException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
