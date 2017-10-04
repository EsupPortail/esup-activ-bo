/**
 * ESUP-Portail Commons - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-commons
 */
package org.esupportail.activbo.services.ldap;

import org.esupportail.commons.services.ldap.LdapException;

/**
 * An exception thrown when failing to retrieve a user from a datasource.
 */
public class InvalidLdapAccountException extends LdapException {

	/**
	 * the id for serialization.
	 */
	private static final long serialVersionUID = 802347220128301517L;
	/**
	 * Bean constructor.
	 * @param message
	 * @param cause
	 */
	protected InvalidLdapAccountException(final String message, final Exception cause) {
		super(message, cause);
	}
	/**
	 * Bean constructor.
	 * @param message
	 */
	public InvalidLdapAccountException(final String message) {
		super(message);
	}
	/**
	 * Bean constructor.
	 * @param cause
	 */
	public InvalidLdapAccountException(final Exception cause) {
		super(cause);
	}
}
