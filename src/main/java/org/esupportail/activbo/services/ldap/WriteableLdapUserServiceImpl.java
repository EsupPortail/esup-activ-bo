/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.activbo.services.ldap;

import java.util.ArrayList;
import java.util.List;
import net.sf.ehcache.CacheManager;

import javax.naming.Name;

import org.apache.commons.codec.binary.Base64;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;

/**
 * An implementation of WriteableLdapService based on LdapTemplate.
 * See /properties/ldap/ldap-write-example.xml.
 */
public class WriteableLdapUserServiceImpl extends org.esupportail.commons.services.ldap.WriteableLdapUserServiceImpl implements WriteableLdapUserService {
	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = -2833750508738328830L;

	private CacheManager cacheManager;

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * Bean constructor.
	 */
	public WriteableLdapUserServiceImpl() {
		super();
	}


	/** Modify an LDAP user using Spring LdapContextSource.
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#updateLdapUser(
	 * org.esupportail.commons.services.ldap.LdapUser)
	 */
	public void updateLdapUser(final LdapUser ldapUser) throws LdapAttributesModificationException {
		
		super.updateLdapUser(ldapUser);
		invalidateLdapCache();		
	}

	/**
	 * @param ldapUser
	 * @param context
	 */
	protected void mapToContext(final LdapUser ldapUser, final DirContextAdapter context) {
		List<String> attributesNames = ldapUser.getAttributeNames();
		for (String ldapAttributeName : attributesNames) {
			List<String> listAttrValue = new ArrayList<String>();			
			listAttrValue = ldapUser.getAttributes(ldapAttributeName);
			
			List<Object> obj = new ArrayList<Object>();
			byte[] bytetVal = null;
			
			// The attribute exists
			if (!listAttrValue.contains("null") && listAttrValue != null && listAttrValue.size() != 0 ) {
				for (String listVal : listAttrValue) 
				// Si insertion de l'attribut jpegphoto dans LDAP
				// Décoder la photo qui a été encodée lors de la saisie dans le formulaire accountDataChange
				 if (listVal.contains("encodeBase64")){
					 listVal=listVal.substring(12);
					 bytetVal = listVal.getBytes();
					 obj.add(Base64.decodeBase64(bytetVal));
					 context.setAttributeValues(ldapAttributeName, obj.toArray());
				 }
				// insertion autres attributs que jpegphoto
				 else context.setAttributeValues(ldapAttributeName, listAttrValue.toArray());
			}
			else  {
				context.setAttributeValues(ldapAttributeName, null); 
			}
		}
	}
	

	public void bindLdap(final LdapUser ldapUser)throws AuthentificationException{
		try{
			Name dn = buildLdapUserDn(ldapUser.getId());
			this.getLdapTemplate().lookup(dn);
		
		} catch (UncategorizedLdapException e) {
			logger.debug("Une authentification a échouée : "+e);
			if (e.getCause() instanceof javax.naming.AuthenticationException) {
				throw new AuthentificationException("Authentification invalide pour l'utilisateur " + ldapUser.getId());
			}
		} 
	}
	
	public void defineAuthenticatedContext(String username, String password) throws LdapException {
		this.getContextSource().setUserName(username);
		this.getContextSource().setPassword(password);
	}
	
	public void defineAuthenticatedContextForUser(String userId, String password) throws LdapException{
		DistinguishedName ldapBindUserDn = new DistinguishedName(this.getDnAuth());
		ldapBindUserDn.add(this.getIdAuth(), userId);
		logger.debug("Binding to LDAP with DN [" + ldapBindUserDn + "] (password ******)");
		
		this.getContextSource().setUserName(ldapBindUserDn.encode());
		this.getContextSource().setPassword(password);
	}

	/**
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#defineAnonymousContext()
	 */
	public void defineAnonymousContext() throws LdapException {
		this.getContextSource().setUserName("");
		this.getContextSource().setPassword("");
	}

	public void invalidateLdapCache() {
		net.sf.ehcache.Cache cache = cacheManager.getCache(org.esupportail.commons.services.ldap.CachingLdapEntityServiceImpl.class.getName());
		cache.removeAll();		
	}
		
	public void setCacheManager(final CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	
}
