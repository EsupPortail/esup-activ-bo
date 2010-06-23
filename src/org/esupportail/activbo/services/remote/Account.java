/**
 * ESUP-Portail LDAP Account Activation - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ
 */
package org.esupportail.activbo.services.remote;


import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Random;


import org.springframework.beans.factory.InitializingBean;
import org.esupportail.activbo.domain.DomainService;
import org.esupportail.activbo.domain.tools.StringTools;
import org.esupportail.commons.beans.AbstractI18nAwareBean;
import org.esupportail.commons.utils.strings.StringUtils;
import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;


/**
 * The class that represent net account.
 */
public class Account implements AccountInt,InitializingBean {
	
	
	private String id;
	
    /**
	 * Harpege number of this account
	 */
    private String harpegeNumber;
    
    
    private String displayName;
    
	private String birthName;
	
	private String shadowLastChange;
	
	private Date birthDate;
	
	private String password;
	
	private String mail;
	
	private String initialPassword;
	
	private boolean charterAgreement=false;
	
	//private DomainService domainService;

	/**
	 * Bean constructor.
	 */
	public Account() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = StringUtils.nullIfEmpty(id);
	}

	public String getHarpegeNumber() {
		System.out.println(harpegeNumber);
		return harpegeNumber;
	}

	public void setHarpegeNumber(String harpegeNumber) {
		System.out.println("set");
		this.harpegeNumber = harpegeNumber;
	}

	public String getBirthName() {
		return birthName;
	}

	public void setBirthName(String birthName) {
		this.birthName = birthName;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public void encryptPassword() {
		/*
		 * If we look at phpldapadmin SSHA encryption algorithm in :
		 * /usr/share/phpldapadmin/lib/functions.php function password_hash(
		 * $password_clear, $enc_type ) salt length for SSHA is 4
		 */
		final int SALT_LENGTH = 4;
		
		LdapShaPasswordEncoder ldapShaPasswordEncoder = new LdapShaPasswordEncoder();
		/* Salt generation */
		byte[] salt = new byte[SALT_LENGTH];
		Random generator = new Random();
		generator.nextBytes(salt);
		/* SSHA encoding */
		String encryptedPassword = ldapShaPasswordEncoder.encodePassword(this.getPassword(), salt);
		this.setPassword(encryptedPassword);
	}
	
	public void generateInitialPassword() {
		initialPassword = "initialseed#";
		SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
		initialPassword += format.format(this.birthDate)+"#";
		initialPassword += this.harpegeNumber+"#";
		initialPassword += StringTools.cleanAllSpecialChar(this.birthName)+"#";
	}
	
	public String getInitialPassword() {
		return initialPassword;
	}

	public void setInitialPassword(String initialPassword) {
		this.initialPassword = initialPassword;
	}

	public boolean changeDisplayName(String newDisplayName) {

		// Compare newDisplayName and displayName
		if (StringTools.compareInsensitive(this.getDisplayName(), newDisplayName)) {
			this.setDisplayName(newDisplayName);
			return true;
		}

		return false;
	}

	public String getShadowLastChange() {
		return shadowLastChange;
	}

	public void setShadowLastChange(String shadowLastChange) {
		this.shadowLastChange = shadowLastChange;
	}
	
	public boolean isActivated() {
		return (this.shadowLastChange!=null && this.shadowLastChange.length()!=0);
	}

	public boolean isCharterAgreement() {
		return charterAgreement;
	}

	public void setCharterAgreement(boolean charterAgreement) {
		this.charterAgreement = charterAgreement;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
	}

	/*public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}*/


}
