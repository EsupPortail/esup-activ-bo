/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.activbo.services.ldap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.ehcache.CacheManager;


import org.esupportail.commons.exceptions.UserNotFoundException;
import org.esupportail.commons.services.i18n.I18nService;
import org.esupportail.commons.services.ldap.CachingLdapEntityServiceImpl;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.ldap.LdapUserImpl;
import org.esupportail.commons.services.ldap.LdapUserService;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.filter.OrFilter;
import org.springframework.ldap.support.filter.WhitespaceWildcardsFilter;
import org.springframework.util.StringUtils;

/**
 * An implementation of LdapUserService that delegates to a CachingLdapEntityServiceImpl.
 */
public class LdapUserServiceTest implements LdapUserService, InitializingBean, Serializable {

	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = 2538032574940842579L;

	/**
	 * The default unique attribute.
	 */
	private static final String DEFAULT_ID_ATTRIBUTE = "uid";

	/**
	 * The default object class.
	 */
	private static final String DEFAULT_OBJECT_CLASS = "Person";

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * The real LDAP entity service to delegate.
	 */
	private CachingLdapEntityServiceImpl service;
	
	/**
	 * The attribute used by method getLdapUsersFromToken().
	 */
	private String searchAttribute;

	/**
	 * The attributes that will be shown when searching for a user.
	 */
	private List<String> searchDisplayedAttributes;

	/**
	 * Bean constructor.
	 */
	public LdapUserServiceTest() {
		super();
		service = new CachingLdapEntityServiceImpl();
		service.setIdAttribute(DEFAULT_ID_ATTRIBUTE);
		service.setObjectClass(DEFAULT_OBJECT_CLASS);
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		if (searchAttribute == null) {
			logger.info("property searchAttribute is not set, method getLdapUsersFromToken() will fail");
		} else {
			Assert.notEmpty(searchDisplayedAttributes, "property searchDisplayedAttribute is not set");
		}
		service.afterPropertiesSet();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + hashCode() + "[" 
		+ "searchDisplayedAttributes=[" + getSearchDisplayedAttributes() + "], " 
		+ "searchAttribute=[" + searchAttribute + "], " 
		+ "service=" + service  
		+ "]";
	}

	/**
	 * @see org.esupportail.commons.services.ldap.LdapUserService#getLdapUser(java.lang.String)
	 */
	public LdapUser getLdapUser(final String id) throws LdapException, UserNotFoundException {
		LdapUserImpl ldapUser = new LdapUserImpl();
		ldapUser.setId("lorvivien");
	//	ldapUser.setAttributes(ldapEntity.getAttributes());
		Map<String,List<String>>e=new HashMap<String,List<String>>();
		
		List<String> listDisplayName=new ArrayList<String>();
		listDisplayName.add("Vivien LORENTE");
		e.put("displayName", listDisplayName);
		
		List<String> listUid=new ArrayList<String>();
		listUid.add("cdiallo");
		e.put("uid", listUid);
		
		List<String> listLogin=new ArrayList<String>();
		listLogin.add("claudiallo");
		e.put("supannAliasLogin", listLogin);
		
		List<String> listMail=new ArrayList<String>();
		listMail.add("aanli@univ-paris1.fr");
		e.put("mail", listMail);
		
		List<String> listPager=new ArrayList<String>();
		listPager.add("0616945407");
		e.put("pager", listPager);
		
		List<String> listMailPerso=new ArrayList<String>();
		listMailPerso.add("aanli@univ-paris1.fr");
		e.put("supannMailPerso", listMailPerso);
		
		List<String> listSLC=new ArrayList<String>();
		listSLC.add("14804");
		e.put("shadowLastChange", listSLC);
		
		List<String> listHN=new ArrayList<String>();
		listHN.add("1102");
		e.put("supannEmpId", listHN);
		
		List<String> listBirthDay=new ArrayList<String>();
		listBirthDay.add("198120515000000Z");
		e.put("up1BirthDay", listBirthDay);
		
		List<String> listBirthName=new ArrayList<String>();
		listBirthName.add("Lorente");
		e.put("up1BirthName", listBirthName);
		
		List<String> listPassword=new ArrayList<String>();
		listPassword.add("koukou");
		e.put("userPassword", listPassword);
		
		List<String> listSmsAgreement=new ArrayList<String>();
		listSmsAgreement.add("{SMSU}CG");
		e.put("up1TermsOfUse", listSmsAgreement);
		
		ldapUser.setAttributes(e);
		return ldapUser;
	}

	/**
	 * @see org.esupportail.commons.services.ldap.LdapUserService#getLdapUsersFromFilter(java.lang.String)
	 */
	public List<LdapUser> getLdapUsersFromFilter(final String filterExpr) throws LdapException {
		LdapUserImpl ldapUser = new LdapUserImpl();
		ldapUser.setId("cdiallo");
	
		
		
		Map<String,List<String>>e=new HashMap<String,List<String>>();
		
		List<String> listUid=new ArrayList<String>();
		listUid.add("lorvivien");
		e.put("uid", listUid);
		
		
		List<String> listDisplayName=new ArrayList<String>();
		listDisplayName.add("Vivien LORENTE");
		e.put("displayName", listDisplayName);
		
		List<String> listMail=new ArrayList<String>();
		listMail.add("aanli@univ-paris1.fr@univ-paris1.fr");
		e.put("mail", listMail);
		
		List<String> listPager=new ArrayList<String>();
		listPager.add("0616945407");
		e.put("pager", listPager);
		
		List<String> listLogin=new ArrayList<String>();
		listLogin.add("claudiallo");
		e.put("supannAliasLogin", listLogin);
		
		
		List<String> listMailPerso=new ArrayList<String>();
		listMailPerso.add("anlifss@yahoo.fr");
		e.put("supannMailPerso", listMailPerso);
		
		List<String> listSLC=new ArrayList<String>();
		listSLC.add("14804");
		e.put("shadowLastChange", listSLC);
		
		List<String> listHN=new ArrayList<String>();
		listHN.add("1102");
		e.put("supannEmpId", listHN);
		
		List<String> listBirthDay=new ArrayList<String>();
		listBirthDay.add("19810515000000Z");
		e.put("up1BirthDay", listBirthDay);
		
		List<String> listBirthName=new ArrayList<String>();
		listBirthName.add("Lorente");
		e.put("up1BirthName", listBirthName);
		
		List<String> listPassword=new ArrayList<String>();
		listPassword.add("koukou");
		e.put("userPassword", listPassword);
		
		List<String> listSmsAgreement=new ArrayList<String>();
		listSmsAgreement.add("{SMSU}CG");
		e.put("up1TermsOfUse", listSmsAgreement);
		
		ldapUser.setAttributes(e);
		List<LdapUser> listLdapUser=new ArrayList<LdapUser>();
		listLdapUser.add(0,ldapUser);
		
		return listLdapUser; 
		
		//return LdapUserImpl.createLdapUsers(service.getLdapEntitiesFromFilter(filterExpr));
	}

	/**
	 * @see org.esupportail.commons.services.ldap.LdapUserService#getLdapUsersFromToken(java.lang.String)
	 */
	public List<LdapUser> getLdapUsersFromToken(final String token) throws LdapException {
		OrFilter filter = new OrFilter();
		filter.or(new WhitespaceWildcardsFilter(searchAttribute, token));
		filter.or(new WhitespaceWildcardsFilter(service.getIdAttribute(), token));
		return getLdapUsersFromFilter(filter.encode());
	}

	/**
	 * @see org.esupportail.commons.services.ldap.LdapUserService#userMatchesFilter(
	 * java.lang.String, java.lang.String)
	 */
	public boolean userMatchesFilter(final String id, final String filter) throws LdapException {
		return service.entityMatchesFilter(id, filter);
	}

	/**
	 * @see org.esupportail.commons.services.ldap.BasicLdapService#getStatistics(java.util.Locale)
	 */
	public List<String> getStatistics(final Locale locale) {
		return service.getStatistics(locale);
	}

	/**
	 * @see org.esupportail.commons.services.ldap.BasicLdapService#resetStatistics()
	 */
	public void resetStatistics() {
		service.resetStatistics();
	}

	/**
	 * Set the cache manager.
	 * @param cacheManager
	 */
	public void setCacheManager(final CacheManager cacheManager) {
		service.setCacheManager(cacheManager);
	}

	/**
	 * Set the cache name.
	 * @param cacheName
	 */
	public void setCacheName(final String cacheName) {
		service.setCacheName(cacheName);
	}

	/**
	 * Set the dnSubPath.
	 * @param dnSubPath
	 */
	public void setDnSubPath(final String dnSubPath) {
		service.setDnSubPath(dnSubPath);
	}

	/**
	 * Set the i18nService.
	 * @param i18nService
	 */
	public void setI18nService(final I18nService i18nService) {
		service.setI18nService(i18nService);
	}

	/**
	 * Set the idAttribute.
	 * @param idAttribute
	 */
	public void setIdAttribute(final String idAttribute) {
		service.setIdAttribute(idAttribute);
	}

	/**
	 * Set the attributes.
	 * @param attributes
	 */
	public void setAttributes(final List<String> attributes) {
		service.setAttributes(attributes);
	}

	/**
	 * Set the attributes.
	 * @param attributes
	 */
	public void setAttributesAsString(final String attributes) {
		List<String> list = new ArrayList<String>();
		for (String attribute : attributes.split(",")) {
			if (StringUtils.hasText(attribute)) {
				if (!list.contains(attribute)) {
					list.add(attribute);
				}
			}
		}
		setAttributes(list);
	}

	/**
	 * Set the ldapTemplate.
	 * @param ldapTemplate
	 */
	public void setLdapTemplate(final LdapTemplate ldapTemplate) {
		service.setLdapTemplate(ldapTemplate);
	}

	/**
	 * Set the objectClass.
	 * @param objectClass
	 */
	public void setObjectClass(final String objectClass) {
		service.setObjectClass(objectClass);
	}

	/**
	 * Set the testFilter.
	 * @param testFilter
	 */
	public void setTestFilter(final String testFilter) {
		service.setTestFilter(testFilter);
	}

	/**
	 * @see org.esupportail.commons.services.ldap.BasicLdapService#supportStatistics()
	 */
	public boolean supportStatistics() {
		return service.supportStatistics();
	}

	/**
	 * @see org.esupportail.commons.services.ldap.BasicLdapService#supportsTest()
	 */
	public boolean supportsTest() {
		return service.supportsTest();
	}

	/**
	 * @see org.esupportail.commons.services.ldap.BasicLdapService#test()
	 */
	public void test() {
		service.test();
	}

	/**
	 * @see org.esupportail.commons.services.ldap.BasicLdapService#testLdapFilter(java.lang.String)
	 */
	public String testLdapFilter(final String filterExpr) throws LdapException {
		return service.testLdapFilter(filterExpr);
	}

	/**
	 * @see org.esupportail.commons.services.ldap.LdapUserService#getSearchDisplayedAttributes()
	 */
	public List<String> getSearchDisplayedAttributes() {
		return searchDisplayedAttributes;
	}

	/**
	 * @param searchDisplayedAttributes the searchDisplayedAttributes to set
	 */
	public void setSearchDisplayedAttributes(final List<String> searchDisplayedAttributes) {
		this.searchDisplayedAttributes = searchDisplayedAttributes;
	}

	/**
	 * @param searchDisplayedAttributes the searchDisplayedAttributes to set
	 */
	public void setSearchDisplayedAttributesAsString(final String searchDisplayedAttributes) {
		List<String> list = new ArrayList<String>();
		for (String attribute : searchDisplayedAttributes.split(",")) {
			if (StringUtils.hasText(attribute)) {
				if (!list.contains(attribute)) {
					list.add(attribute);
				}
			}
		}
		setSearchDisplayedAttributes(list);
	}

	/**
	 * @param searchAttribute the searchAttribute to set
	 */
	public void setSearchAttribute(final String searchAttribute) {
		this.searchAttribute = searchAttribute;
	}

	/**
	 * @return the unique id attribute
	 * @see org.esupportail.commons.services.ldap.SimpleLdapEntityServiceImpl#getIdAttribute()
	 */
	public String getIdAttribute() {
		return service.getIdAttribute();
	}

}
