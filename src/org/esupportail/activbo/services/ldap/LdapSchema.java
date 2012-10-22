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
	
	private String eduPersonPrimaryAffiliation;
	
	private String eduPersonAffiliation;
	
	private String supannListeRouge;
	
	private String supannRefId;
	
	private String supannCivilite;
	
	private String sn;
	
	private String givenName;
	
	private String telephoneNumber;
	
	private String facsimileTelephoneNumber;
	
	private String postalAddress;
	
	private String homePostalAddress;
	
	private String supannEntiteAffectation;
	
	private String employeeType;
	
	private String supannEmpId;
	
	private String mobile;
	
	private String supannCodeINE;
	
	private String supannEtuId;
	
	private String supannEtuCursusAnnee;
	
	private String mailForwardingAddress;
	
	private String info;
	
	private String roomNumber;
	
	private String up1FloorNumber;
	
	private String supannAliasLogin;
	
	private String up1AltGivenName;
	
	private byte[] jpegPhoto;
	
	public String getEduPersonAffiliation() {
		return eduPersonAffiliation;
	}

	public void setEduPersonAffiliation(String eduPersonAffiliation) {
		this.eduPersonAffiliation = eduPersonAffiliation;
	}

	public String getEduPersonPrimaryAffiliation() {
		return eduPersonPrimaryAffiliation;
	}

	public void setEduPersonPrimaryAffiliation(String eduPersonPrimaryAffiliation) {
		this.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation;
	}

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

	/**
	 * @return the supannListeRouge
	 */
	public String getSupannListeRouge() {
		return supannListeRouge;
	}

	/**
	 * @param supannListeRouge the supannListeRouge to set
	 */
	public void setSupannListeRouge(String supannListeRouge) {
		this.supannListeRouge = supannListeRouge;
	}

	/**
	 * @return the supannRefId
	 */
	public String getSupannRefId() {
		return supannRefId;
	}

	/**
	 * @param supannRefId the supannRefId to set
	 */
	public void setSupannRefId(String supannRefId) {
		this.supannRefId = supannRefId;
	}

	public String getSupannCivilite() {
		return supannCivilite;
	}

	public void setSupannCivilite(String supannCivilite) {
		this.supannCivilite = supannCivilite;
	}

	/**
	 * @return the sn
	 */
	public String getSn() {
		return sn;
	}

	/**
	 * @param sn the sn to set
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}

	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return the telephoneNumber
	 */
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	/**
	 * @param telephoneNumber the telephoneNumber to set
	 */
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	/**
	 * @return the facsimileTelephoneNumber
	 */
	public String getFacsimileTelephoneNumber() {
		return facsimileTelephoneNumber;
	}

	/**
	 * @param facsimileTelephoneNumber the facsimileTelephoneNumber to set
	 */
	public void setFacsimileTelephoneNumber(String facsimileTelephoneNumber) {
		this.facsimileTelephoneNumber = facsimileTelephoneNumber;
	}

	/**
	 * @return the postalAddress
	 */
	public String getPostalAddress() {
		return postalAddress;
	}

	/**
	 * @param postalAddress the postalAddress to set
	 */
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}

	/**
	 * @return the homePostalAddress
	 */
	public String getHomePostalAddress() {
		return homePostalAddress;
	}

	/**
	 * @param homePostalAddress the homePostalAddress to set
	 */
	public void setHomePostalAddress(String homePostalAddress) {
		this.homePostalAddress = homePostalAddress;
	}

	/**
	 * @return the supannEntiteAffectation
	 */
	public String getSupannEntiteAffectation() {
		return supannEntiteAffectation;
	}

	/**
	 * @param supannEntiteAffectation the supannEntiteAffectation to set
	 */
	public void setSupannEntiteAffectation(String supannEntiteAffectation) {
		this.supannEntiteAffectation = supannEntiteAffectation;
	}

	/**
	 * @return the employeeType
	 */
	public String getEmployeeType() {
		return employeeType;
	}

	/**
	 * @param employeeType the employeeType to set
	 */
	public void setEmployeeType(String employeeType) {
		this.employeeType = employeeType;
	}

	/**
	 * @return the supannEmpId
	 */
	public String getSupannEmpId() {
		return supannEmpId;
	}

	/**
	 * @param supannEmpId the supannEmpId to set
	 */
	public void setSupannEmpId(String supannEmpId) {
		this.supannEmpId = supannEmpId;
	}

	/**
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return the supannCodeINE
	 */
	public String getSupannCodeINE() {
		return supannCodeINE;
	}

	/**
	 * @param supannCodeINE the supannCodeINE to set
	 */
	public void setSupannCodeINE(String supannCodeINE) {
		this.supannCodeINE = supannCodeINE;
	}

	/**
	 * @return the supannEtdId
	 */
	public String getSupannEtuId() {
		return supannEtuId;
	}

	/**
	 * @param supannEtdId the supannEtdId to set
	 */
	public void setSupannEtuId(String supannEtuId) {
		this.supannEtuId = supannEtuId;
	}

	/**
	 * @return the supannEtuCursusAnnee
	 */
	public String getSupannEtuCursusAnnee() {
		return supannEtuCursusAnnee;
	}

	/**
	 * @param supannEtuCursusAnnee the supannEtuCursusAnnee to set
	 */
	public void setSupannEtuCursusAnnee(String supannEtuCursusAnnee) {
		this.supannEtuCursusAnnee = supannEtuCursusAnnee;
	}

	/**
	 * @return the mailForwardingAddress
	 */
	public String getMailForwardingAddress() {
		return mailForwardingAddress;
	}

	/**
	 * @param mailForwardingAddress the mailForwardingAddress to set
	 */
	public void setMailForwardingAddress(String mailForwardingAddress) {
		this.mailForwardingAddress = mailForwardingAddress;
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @return the roomNumber
	 */
	public String getRoomNumber() {
		return roomNumber;
	}

	/**
	 * @param roomNumber the roomNumber to set
	 */
	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	/**
	 * @return the up1FloorNumber
	 */
	public String getUp1FloorNumber() {
		return up1FloorNumber;
	}

	/**
	 * @param up1FloorNumber the up1FloorNumber to set
	 */
	public void setUp1FloorNumber(String up1FloorNumber) {
		this.up1FloorNumber = up1FloorNumber;
	}

	/**
	 * @return the supannAliasLogin
	 */
	public String getSupannAliasLogin() {
		return supannAliasLogin;
	}

	/**
	 * @param supannAliasLogin the supannAliasLogin to set
	 */
	public void setSupannAliasLogin(String supannAliasLogin) {
		this.supannAliasLogin = supannAliasLogin;
	}

	/**
	 * @return the up1AltGivenName
	 */
	public String getUp1AltGivenName() {
		return up1AltGivenName;
	}

	/**
	 * @param up1AltGivenName the up1AltGivenName to set
	 */
	public void setUp1AltGivenName(String up1AltGivenName) {
		this.up1AltGivenName = up1AltGivenName;
	}

	public byte[] getJpegPhoto() {
		return jpegPhoto;
	}

	public void setJpegPhoto(byte[] jpegPhoto) {
		this.jpegPhoto = jpegPhoto;
	}

	

	

}