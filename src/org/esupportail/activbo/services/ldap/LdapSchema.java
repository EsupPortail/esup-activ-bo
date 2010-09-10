/*
 * Created on Apr 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.esupportail.activbo.services.ldap;


import org.springframework.beans.factory.InitializingBean;



public class LdapSchema implements InitializingBean{
	
	private String pager;
	/**
	 * 
	 */
	private String displayName;
	
	/**
	 * 
	 */
	private String birthdate;
	
	/**
	 * 
	 */
	private String birthdateFormat;
	
	/**
	 * 
	 */
	private String uid;
	
	/**
	 * 
	 */
	private String employeeId;
	
	/**
	 * 
	 */
	private String cn;
	
	/**
	 * 
	 */
	private String birthName;
	
	/**
	 * 
	 */
	private String password;
	
	/**
	 * 
	 */
	private String shadowLastChange;
	
	/**
	 * 
	 */
	private String mail;
	
	
	
	private String usernameAdmin;
	
	
	private String passwordAdmin;
	
	
	private String mailPerso;
	
	private String login;
	
	private String termsOfUse;
	
	private String krbPrincipal;

	public String getLogin() {
		return login;
	}


	public void setLogin(String login) {
		this.login = login;
	}


	public LdapSchema(){
		super();
	}


	public String getPager() {
		return pager;
	}


	public void setPager(String pager) {
		this.pager = pager;
	}


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public String getBirthdate() {
		return birthdate;
	}


	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}


	

	public String getUid() {
		return uid;
	}


	public void setUid(String uid) {
		this.uid = uid;
	}


	public String getEmployeeId() {
		return employeeId;
	}


	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}


	public String getCn() {
		return cn;
	}


	public void setCn(String cn) {
		this.cn = cn;
	}


	public String getBirthName() {
		return birthName;
	}


	public void setBirthName(String birthName) {
		this.birthName = birthName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getShadowLastChange() {
		return shadowLastChange;
	}


	public void setShadowLastChange(String shadowLastChange) {
		this.shadowLastChange = shadowLastChange;
	}


	public String getMail() {
		return mail;
	}


	public void setMail(String mail) {
		this.mail = mail;
	}


	public String getUsernameAdmin() {
		return usernameAdmin;
	}


	public void setUsernameAdmin(String usernameAdmin) {
		this.usernameAdmin = usernameAdmin;
	}


	public String getPasswordAdmin() {
		return passwordAdmin;
	}


	public void setPasswordAdmin(String passwordAdmin) {
		this.passwordAdmin = passwordAdmin;
	}


	public String getMailPerso() {
		return mailPerso;
	}


	public void setMailPerso(String mailPerso) {
		this.mailPerso = mailPerso;
	}


	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}


	public String getBirthdateFormat() {
		return birthdateFormat;
	}


	public void setBirthdateFormat(String birthdateFormat) {
		this.birthdateFormat = birthdateFormat;
	}


	/**
	 * @return the termsOfUse
	 */
	public String getTermsOfUse() {
		return termsOfUse;
	}


	/**
	 * @param termsOfUse the termsOfUse to set
	 */
	public void setTermsOfUse(String termsOfUse) {
		this.termsOfUse = termsOfUse;
	}


	/**
	 * @return the krbPrincipal
	 */
	public String getKrbPrincipal() {
		return krbPrincipal;
	}


	/**
	 * @param krbPrincipal the krbPrincipal to set
	 */
	public void setKrbPrincipal(String krbPrincipal) {
		this.krbPrincipal = krbPrincipal;
	}
}