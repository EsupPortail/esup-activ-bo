/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.activbo.services.ldap;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.commons.services.ldap.LdapUser;

/**
 * The interface of writeable LDAP user services.
 */
public interface WriteableLdapUserService extends org.esupportail.commons.services.ldap.WriteableLdapUserService {
	public void defineAuthenticatedContext(String username, String password);
	public void defineAuthenticatedContextForUser(String userId, String password);
	
	public void bindLdap(final LdapUser ldapUser) throws AuthentificationException;
	
	public void invalidateLdapCache();
}
