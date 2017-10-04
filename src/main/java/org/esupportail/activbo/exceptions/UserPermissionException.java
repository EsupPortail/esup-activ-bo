package org.esupportail.activbo.exceptions;

public class UserPermissionException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8375439753015128832L;

	/**
	 * @param message
	 */
	public UserPermissionException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UserPermissionException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserPermissionException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
