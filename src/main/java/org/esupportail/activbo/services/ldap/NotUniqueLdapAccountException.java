/**
 * ESUP-Portail Commons - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-commons
 */
package org.esupportail.activbo.services.ldap; 

import org.esupportail.commons.services.ldap.LdapException;

/**
 * A class to represent the exceptions thrown when expected objects 
 * do not exists (either retrieved from the database or configuration files).
 */
public class NotUniqueLdapAccountException extends LdapException {

	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = -3452596009604914042L;

	/**
	 * @param message
	 */
	public NotUniqueLdapAccountException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NotUniqueLdapAccountException(final Exception cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotUniqueLdapAccountException(final String message, final Exception cause) {
		super(message, cause);
	}

}
