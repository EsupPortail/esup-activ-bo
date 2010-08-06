package org.esupportail.activbo.services.kerberos;

public class OldPassException extends Exception{
	
	/**
	 * The id for serialization.
	 */
	private static final long serialVersionUID = 8197090501242229324L;

	/**
	 * @param message
	 */
	public OldPassException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public OldPassException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OldPassException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
