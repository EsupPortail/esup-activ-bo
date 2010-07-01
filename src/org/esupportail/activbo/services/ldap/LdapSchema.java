/*
 * Created on Apr 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.esupportail.activbo.services.ldap;

import org.esupportail.commons.utils.Assert;
import org.springframework.beans.factory.InitializingBean;

/**
 * @brief Load and manage properties file
 * 
 * @author fjammes
 * 
 */

/* TODO : gérer une liste de noms d'attributs afin de
 * ne pas avoir de code à ajouter ici en cas d'ajout
 * de paramètres */

public final class LdapSchema implements InitializingBean{
	

	/**
	 * 
	 */
	public static String displayName;
	
	/**
	 * 
	 */
	protected static String birthdate;
	
	/**
	 * 
	 */
	protected static String birthdateFormat;
	
	/**
	 * 
	 */
	protected static String uid;
	
	/**
	 * 
	 */
	protected static String employeeId;
	
	/**
	 * 
	 */
	protected static String cn;
	
	/**
	 * 
	 */
	protected static String birthName;
	
	/**
	 * 
	 */
	protected static String password;
	
	/**
	 * 
	 */
	protected static String shadowLastChange;
	
	/**
	 * 
	 */
	protected static String mail;
	
	
	protected static String usernameAdmin;
	
	
	protected static String passwordAdmin;


	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.displayName, 
				"property displayName of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.birthdate, 
				"property birthdate of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.birthdateFormat, 
				"property birthdateFormat of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.uid, 
				"property uid of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.employeeId, 
				"property employeeId of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.cn, 
				"property cn of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.birthName, 
				"property sn of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.password, 
				"property password of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.shadowLastChange, 
				"property shadowLastChange of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.mail, 
				"property mail of class " + this.getClass().getName() + " can not be null");
	}
	
	/**
	 * @return Returns the birthdate.
	 */
	public static String getBirthdate() {
		return birthdate;
	}

	/**
	 * @param birthdate The birthdate to set.
	 */
	public void setBirthdate(String birthdate) {
		LdapSchema.birthdate = birthdate;
	}
	
	/**
	 * @return Returns the birthdateFormat.
	 */
	public static String getBirthdateFormat() {
		return birthdateFormat;
	}

	/**
	 * @param birthdateFormat The birthdate format to set.
	 */
	public void setBirthdateFormat(String birthdateFormat) {
		LdapSchema.birthdateFormat = birthdateFormat;
	}

	/**
	 * @return Returns the cn.
	 */
	public static String getCn() {
		return cn;
	}

	/**
	 * @param cn The cn to set.
	 */
	public void setCn(String cn) {
		LdapSchema.cn = cn;
	}

	/**
	 * @return Returns the sn.
	 */
	public static String getBirthName() {
		return birthName;
	}

	/**
	 * @param sn The sn to set.
	 */
	public void setBirthName(String sn) {
		LdapSchema.birthName = sn;
	}


	/**
	 * @return Returns the supannEmpId.
	 */
	public static String getEmployeeId() {
		return employeeId;
	}

	/**
	 * @param supannEmpId The supannEmpId to set.
	 */
	public void setEmployeeId(String employeeId) {
		LdapSchema.employeeId = employeeId;
	}

	/**
	 * @return Returns the uid.
	 */
	public static String getUid() {
		return uid;
	}

	/**
	 * @param uid The uid to set.
	 */
	public void setUid(String uid) {
		LdapSchema.uid = uid;
	}
	
	/**
	 * @return Returns the displayName.
	 */
	public static String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		LdapSchema.displayName = displayName;
	}

	public static String getUsernameAdmin() {
		return usernameAdmin;
	}

	public void setUsernameAdmin(String usernameAdmin) {
		LdapSchema.usernameAdmin = usernameAdmin;
	}

	public static String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		LdapSchema.password = password;
	}

	public static String getShadowLastChange() {
		return shadowLastChange;
	}

	public void setShadowLastChange(String shadowLastChange) {
		LdapSchema.shadowLastChange = shadowLastChange;
	}

	public static String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		LdapSchema.mail = mail;
	}

	public static String getPasswordAdmin() {
		return passwordAdmin;
	}

	public void setPasswordAdmin(String passwordAdmin) {
		LdapSchema.passwordAdmin = passwordAdmin;
	}

}
