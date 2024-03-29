/**
 * ESUP-Portail Commons - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-commons
 */
package org.esupportail.activbo.services.ldap;

/**
 * An exception thrown when failing to retrieve a user from a datasource.
 */
public class LdapAttributesModificationException extends Exception {

    /**
     * the id for serialization.
     */
    private static final long serialVersionUID = 802347220128301517L;
    /**
     * Bean constructor.
     * @param message
     * @param cause
     */
    protected LdapAttributesModificationException(final String message, final Exception cause) {
        super(message, cause);
    }
    /**
     * Bean constructor.
     * @param message
     */
    public LdapAttributesModificationException(final String message) {
        super(message);
    }
    /**
     * Bean constructor.
     * @param cause
     */
    public LdapAttributesModificationException(final Exception cause) {
        super(cause);
    }
}
