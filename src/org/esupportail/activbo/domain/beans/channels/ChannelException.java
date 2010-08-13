package org.esupportail.activbo.domain.beans.channels;

public class ChannelException extends Exception{
	
	/**
	 * The id for serialization.
	 */
	private static final long serialVersionUID = 8197090501242229324L;

	/**
	 * @param message
	 */
	public ChannelException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ChannelException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ChannelException(final String message, final Throwable cause) {
		super(message, cause);
	}


}
