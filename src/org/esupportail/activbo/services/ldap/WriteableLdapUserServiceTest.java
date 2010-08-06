/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.activbo.services.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;

import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.ldap.support.LdapContextSource;

/**
 * An implementation of WriteableLdapService based on LdapTemplate.
 * See /properties/ldap/ldap-write-example.xml.
 */
public class WriteableLdapUserServiceTest implements WriteableLdapUserService, InitializingBean {
	
	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = -2833750508738328830L;

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());

	/**
	 * A LdapTemplate instance, to perform LDAP operations.
	 */
	private LdapTemplate ldapTemplate;
	
	/**
	 * A LdapContextSource instance, to modify LDAP connections.
	 */
	private LdapContextSource contextSource;

	/**
	 * The name of the attribute that contains the unique id.
	 */
	private String idAttribute;

	/**
	 * The DN of LDAP users. for example : ou=people,dc=domain,dc=edu
	 */
	private String dnAuth;
	
	/**
	 * The name of the attribute that contains the unique id of LDAP users.
	 */
	private String idAuth;
	
	/**
	 * The DN sub path.
	 */
	private String dnSubPath;
	
	/**
	 * The names of the attributes to update.
	 */
	private List<String> attributes;
	
	/**
	 * Bean constructor.
	 */
	public WriteableLdapUserServiceTest() {
		super();
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		
	}

	/** Modify an LDAP user using Spring LdapContextSource.
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#updateLdapUser(
	 * org.esupportail.commons.services.ldap.LdapUser)
	 */
	public void updateLdapUser(final LdapUser ldapUser) throws LdapAttributesModificationException {
		
		
		
	}
	
	/** Create an LDAP user using Spring LdapContextSource.
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#createLdapUser(
	 * org.esupportail.commons.services.ldap.LdapUser)
	 */
	public void createLdapUser(final LdapUser ldapUser) {
		Name dn = buildLdapUserDn(ldapUser.getId());
		DirContextAdapter context = new DirContextAdapter(dn);
		mapToContext(ldapUser, context);
		ldapTemplate.bind(dn, context, null);
		logger.info("created [" + dn + "] from [" + ldapUser + "]");
	}
	
	/** Build user full DN.
	 * @param userId
	 * @return user full DN
	 */
	protected DistinguishedName buildLdapUserDn(final String userId) {
		DistinguishedName dn;
		dn = new DistinguishedName(dnSubPath);
		dn.add(this.idAttribute, userId);
		return dn;
	}
	
	/** Delete an LDAP user using Spring LdapContextSource.
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#deleteLdapUser(
	 * org.esupportail.commons.services.ldap.LdapUser)
	 */
	public void deleteLdapUser(final LdapUser ldapUser) {
		DistinguishedName ldapUserDn = buildLdapUserDn(ldapUser.getId());
		ldapTemplate.unbind(ldapUserDn);
	}
	
	/**
	 * @param ldapUser
	 * @param context
	 */
	protected void mapToContext(final LdapUser ldapUser, final DirContextAdapter context) {
		List<String> attributesNames = ldapUser.getAttributeNames();
		for (String ldapAttributeName : attributesNames) {
			List<String> listAttr = new ArrayList<String>();
			listAttr = ldapUser.getAttributes(ldapAttributeName);
			// The attribute exists
			if (listAttr != null && listAttr.size() != 0) {
				context.setAttributeValues(ldapAttributeName, listAttr.toArray());
			}
		}
	}
	
	/**
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#setAuthenticatedContext(
	 * java.lang.String, java.lang.String)
	 */
	public void setAuthenticatedContext(
			final String userId, 
			final String password) throws LdapException {
		DistinguishedName ldapBindUserDn = new DistinguishedName(this.dnAuth);
		ldapBindUserDn.add(this.idAuth, userId);
		if (logger.isDebugEnabled()) {
			logger.debug("Binding to LDAP with DN [" + ldapBindUserDn + "] (password ******)");
		}
		contextSource.setUserName(ldapBindUserDn.encode());
		contextSource.setPassword(password);
	}
	
	public void defineAuthenticatedContext(String username, String password) throws LdapException {
		
	}
	
	public void defineAuthenticatedContextForUser(String userId, String password) throws LdapException{
		
	}

	/**
	 * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#defineAnonymousContext()
	 */
	public void defineAnonymousContext() throws LdapException {
		
	}

	/**
	 * @return ldapTemplate the LdapTemplate to get
	 */
	public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}

	
	/**
	 * @param ldapTemplate the LdapTemplate to set
	 */
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/**
	 * @return the contextSource to get
	 */
	public LdapContextSource getContextSource() {
		return contextSource;
	}

	/**
	 * @param contextSource the ContextSource to set
	 */
	public void setContextSource(LdapContextSource contextSource) {
		this.contextSource = contextSource;
	}

	/**
	 * @return the idAttribute to get
	 */
	public String getIdAttribute() {
		return idAttribute;
	}

	/**
	 * @param idAttribute the idAttribute to set
	 */
	public void setIdAttribute(final String idAttribute) {
		this.idAttribute = idAttribute;
	}

	/**
	 * @return the dnSubPath to get
	 */
	public String getDnSubPath() {
		return dnSubPath;
	}

	/**
	 * @param dnSubPath the dnSubPath to set
	 */
	public void setDnSubPath(final String dnSubPath) {
		this.dnSubPath = dnSubPath;
	}

	/**
	 * @return the attributes to get
	 */
	public List<String> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(final List<String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the dnAuth to get
	 */
	public String getDnAuth() {
		return dnAuth;
	}

	/**
	 * @param dnAuth the dnAuth to set
	 */
	public void setDnAuth(final String dnAuth) {
		this.dnAuth = dnAuth;
	}

	/**
	 * @return the idAuth to get
	 */
	public String getIdAuth() {
		return idAuth;
	}

	/**
	 * @param idAuth the idAuth to set
	 */
	public void setIdAuth(final String idAuth) {
		this.idAuth = idAuth;
	}
	
}
