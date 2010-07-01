/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.mail.internet.InternetAddress;

import org.esupportail.activbo.dao.DaoService;
import org.esupportail.activbo.domain.beans.User;
import org.esupportail.activbo.domain.beans.VersionManager;

import org.esupportail.activbo.domain.tools.StringTools;
import org.esupportail.activbo.services.kerberos.KRBAdmin;
import org.esupportail.activbo.services.kerberos.KRBAdminTest;
import org.esupportail.activbo.services.ldap.InvalidLdapAccountException;
import org.esupportail.activbo.services.ldap.LdapSchema;
import org.esupportail.activbo.services.ldap.NotUniqueLdapAccountException;
import org.esupportail.activbo.services.ldap.WriteableLdapUserService;
import org.esupportail.commons.exceptions.ConfigException;
import org.esupportail.commons.exceptions.UserNotFoundException;
import org.esupportail.commons.services.application.Version;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.ldap.LdapUserService;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.services.smtp.AsynchronousSmtpServiceImpl;
import org.esupportail.commons.utils.Assert;
import org.esupportail.commons.web.beans.Paginator;
import org.springframework.beans.factory.InitializingBean;

/**
 * The basic implementation of DomainService.
 * 
 * See /properties/domain/domain-example.xml
 */
public class DomainServiceImpl implements DomainService, InitializingBean {

	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = -8200845058340254019L;

	/**
	 * {@link DaoService}.
	 */
	private String accountIdKey;
	private String accountMailKey;
	private String accountSLCKey;
	private String accountDNKey;
	
	
	private String accessCodeKey;
	private String accessDateKey;
	
	private String formatDateConv;
	
	private AsynchronousSmtpServiceImpl smtpService;
	
	public String code;
	
	
	private static HashMap<String,HashMap<String,String>> access=new HashMap<String,HashMap<String,String>>();
		
	
	/**
	 * kerberos.ldap.method
	 * kerberos.host
	 */
	private String krbLdapMethod,krbHost;
	
	
	/**
	 * {@link KerberosAdmin}
	 */
	private KRBAdminTest kerberosAdmin;
	
	
	
	private DaoService daoService;

	/**
	 * {@link LdapUserService}.
	 */
	private LdapUserService ldapUserService;

	/**
	 * The LDAP attribute that contains the display name. 
	 */
	private String displayNameLdapAttribute;
	
	
	private String mailPerso;
	
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
		
	private HashMap<String, String> accountDescr;
	
	private WriteableLdapUserService writeableLdapUserService;

	private String initialPassword;
	/**
	 * Bean constructor.
	 */
	public DomainServiceImpl() {
		super();
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.daoService, 
				"property daoService of class " + this.getClass().getName() + " can not be null");
		Assert.notNull(this.ldapUserService, 
				"property ldapUserService of class " + this.getClass().getName() + " can not be null");
		Assert.hasText(this.displayNameLdapAttribute, 
				"property displayNameLdapAttribute of class " + this.getClass().getName() 
				+ " can not be null");
	}
	
	
	
	
	
	//////////////////////////////////////////////////////////////
	// User
	//////////////////////////////////////////////////////////////

	/**
	 * Set the information of a user from a ldapUser.
	 * @param user 
	 * @param ldapUser 
	 * @return true if the user was updated.
	 */
	private boolean setUserInfo(
			final User user, 
			final LdapUser ldapUser) {
		String displayName = null;
		List<String> displayNameLdapAttributes = ldapUser.getAttributes().get(displayNameLdapAttribute);
		if (displayNameLdapAttributes != null) {
			displayName = displayNameLdapAttributes.get(0);
		}
		if (displayName == null) {
			displayName = user.getId();
		}
		if (displayName.equals(user.getDisplayName())) {
			return false;
		}
		user.setDisplayName(displayName);
		return true;
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#updateUserInfo(org.esupportail.activbo.domain.beans.User)
	 */
	public void updateUserInfo(final User user) {
		if (setUserInfo(user, ldapUserService.getLdapUser(user.getId()))) {
			updateUser(user);
		}
	}

	/**
	 * If the user is not found in the database, try to create it from a LDAP search.
	 * @see org.esupportail.activbo.domain.DomainService#getUser(java.lang.String)
	 */
	public User getUser(final String id) throws UserNotFoundException {
		User user = daoService.getUser(id);
		if (user == null) {
			LdapUser ldapUser = this.ldapUserService.getLdapUser(id);
			user = new User();
			user.setId(ldapUser.getId());
			setUserInfo(user, ldapUser);
			daoService.addUser(user);
			logger.info("user '" + user.getId() + "' has been added to the database");
		}
		return user;
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#getUsers()
	 */
	public List<User> getUsers() {
		return this.daoService.getUsers();
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#updateUser(org.esupportail.activbo.domain.beans.User)
	 */
	public void updateUser(final User user) {
		this.daoService.updateUser(user);
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#addAdmin(org.esupportail.activbo.domain.beans.User)
	 */
	public void addAdmin(
			final User user) {
		user.setAdmin(true);
		updateUser(user);
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#deleteAdmin(org.esupportail.activbo.domain.beans.User)
	 */
	public void deleteAdmin(
			final User user) {
		user.setAdmin(false);
		updateUser(user);
	}
	
	/**
	 * @see org.esupportail.activbo.domain.DomainService#getAdminPaginator()
	 */
	public Paginator<User> getAdminPaginator() {
		return this.daoService.getAdminPaginator();
	}

	/**
	 * @param displayNameLdapAttribute the displayNameLdapAttribute to set
	 */
	public void setDisplayNameLdapAttribute(final String displayNameLdapAttribute) {
		this.displayNameLdapAttribute = displayNameLdapAttribute;
	}

	//////////////////////////////////////////////////////////////
	// VersionManager
	//////////////////////////////////////////////////////////////

	/**
	 * @see org.esupportail.activbo.domain.DomainService#getDatabaseVersion()
	 */
	public Version getDatabaseVersion() throws ConfigException {
		VersionManager versionManager = daoService.getVersionManager();
		if (versionManager == null) {
			return null;
		}
		return new Version(versionManager.getVersion());
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#setDatabaseVersion(java.lang.String)
	 */
	public void setDatabaseVersion(final String version) {
		if (logger.isDebugEnabled()) {
			logger.debug("setting database version to '" + version + "'...");
		}
		VersionManager versionManager = daoService.getVersionManager();
		versionManager.setVersion(version);
		daoService.updateVersionManager(versionManager);
		if (logger.isDebugEnabled()) {
			logger.debug("database version set to '" + version + "'.");
		}
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#setDatabaseVersion(
	 * org.esupportail.commons.services.application.Version)
	 */
	public void setDatabaseVersion(final Version version) {
		setDatabaseVersion(version.toString());
	}

	//////////////////////////////////////////////////////////////
	// Authorizations
	//////////////////////////////////////////////////////////////

	/**
	 * @see org.esupportail.activbo.domain.DomainService#userCanViewAdmins(org.esupportail.activbo.domain.beans.User)
	 */
	public boolean userCanViewAdmins(final User user) {
		if (user == null) {
			return false;
		}
		return user.getAdmin();
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#userCanAddAdmin(org.esupportail.activbo.domain.beans.User)
	 */
	public boolean userCanAddAdmin(final User user) {
		if (user == null) {
			return false;
		}
		return user.getAdmin();
	}

	/**
	 * @see org.esupportail.activbo.domain.DomainService#userCanDeleteAdmin(
	 * org.esupportail.activbo.domain.beans.User, org.esupportail.activbo.domain.beans.User)
	 */
	public boolean userCanDeleteAdmin(final User user, final User admin) {
		if (user == null) {
			return false;
		}
		if (!user.getAdmin()) {
			return false;
		}
		return !user.equals(admin);
	}

	//////////////////////////////////////////////////////////////
	// Misc
	//////////////////////////////////////////////////////////////

	/**
	 * @param daoService the daoService to set
	 */
	public void setDaoService(final DaoService daoService) {
		this.daoService = daoService;
	}

	/**
	 * @param ldapUserService the ldapUserService to set
	 */
	public void setLdapUserService(final LdapUserService ldapUserService) {
		this.ldapUserService = ldapUserService;
	}
	
	public HashMap<String,String> validateAccount(String number,String birthName,Date birthDate,List<String>attrPersoInfo) throws LdapException{

		try {
			
			List<LdapUser> ldapUserList = this.ldapUserService.
					getLdapUsersFromFilter("(supannEmpId="
							+ number + ")");
			
			if (ldapUserList.size() > 1) {
				throw new NotUniqueLdapAccountException(
						"LDAP account with supannEmpId "
								+ number + " not unique");
			} else if (ldapUserList.size() == 0) {
				return accountDescr;
			}
			

			LdapUser ldapUser = ldapUserList.get(0); //recuperation de l'element LDAP correspondant au id 1102
			
						
			if (logger.isDebugEnabled()) {
				logger.debug("Validating account for : " + ldapUser);
				logger.debug("Birthdate checking");
			}
			
			/*
			 * Birthdate checking
			 */
			
			String ldapUserBirthdateStr = ldapUser.getAttribute(LdapSchema.getBirthdate());         //recuperation de la valeur de l'attribut up1Birthday de l'element LDAP
									
			if (ldapUserBirthdateStr == null) {
				String errmsg = "LDAP account with supannEmpId "
						+ number + " has no birthdate";
				logger.error(errmsg);
				throw new InvalidLdapAccountException(errmsg);
			}
			
			Date ldapUserBirthdate;

			try {
				
				SimpleDateFormat formatter = new SimpleDateFormat(LdapSchema.getBirthdateFormat());

				/*
				 * TimeZone must be the same in user interface and LDAP, UTC is
				 * chosen
				 */
				TimeZone tz = TimeZone.getTimeZone("UTC");
				formatter.setTimeZone(tz);

				ldapUserBirthdate = formatter.parse(ldapUserBirthdateStr);
			} catch (ParseException e) {
				throw new InvalidLdapAccountException(e.getMessage());
			}

			if (birthDate.compareTo(ldapUserBirthdate) != 0) {
				logger.info("Invalid birthdate (" + ldapUserBirthdate
						+ ") for LDAP account with supannEmpId "
						+ number);
				System.out.println("Mauvaise DATE");
				return accountDescr;
			}

			/*
			 * Patronymic name checking
			 */
			logger.debug("Patronymic name checking");

			List<String> ldapUserBirthnameList = ldapUser
					.getAttributes(LdapSchema.getBirthName());
			if (ldapUserBirthnameList == null
					|| ldapUserBirthnameList.size() == 0) {
				throw new InvalidLdapAccountException(
						"LDAP account with supannEmpId "
								+ number
								+ " has no patronymic name");
			}

			Iterator<String> i = ldapUserBirthnameList.iterator();
			String sn;
			boolean isInLdap = false;
			while (i.hasNext()) {
				sn = i.next();
				System.out.println(sn);
				if (StringTools.compareInsensitive(birthName, sn)) {
					isInLdap = true;
					break;
				}
			}

			if (!isInLdap) {
				logger.info("Invalid patronymic ("
						+ ldapUserBirthnameList.toArray()
						+ ") for LDAP account with supannEmpId"
						+ number);
				return accountDescr;
			}
			
			accountDescr=new HashMap<String,String>();
			accountDescr.put(accountIdKey, ldapUser.getId());
			//accountDescr.put(accountDNKey, ldapUser.getAttribute(LdapSchema.getDisplayName()));
			accountDescr.put(accountMailKey, ldapUser.getAttribute(LdapSchema.getMail()));
			accountDescr.put(accountSLCKey, ldapUser.getAttribute(LdapSchema.getShadowLastChange()));
			
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), ldapUser.getAttribute(attrPersoInfo.get(j)));
				System.out.println("RECUPERATION");
				System.out.println(ldapUser.getAttribute(attrPersoInfo.get(j)));
			}
			
			
			//génération du password initial
			initialPassword = "initialseed#";
			SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
			initialPassword += format.format(birthDate)+"#";
			initialPassword += number+"#";
			initialPassword += StringTools.cleanAllSpecialChar(birthName)+"#";
			
			//accountDescr.put("initialPassword", ldapUser.getAttribute(LdapSchema.getShadowLastChange()));
			
			return accountDescr;

		} catch (LdapException e) {
			logger.debug("Exception thrown by validateAccount() : "
					+ e.getMessage());
			throw e;
		}
	}
	
	
	
	public void setMailPerso(String id,String mailPerso){
		
		try{
			this.mailPerso=mailPerso;
			//InternetAddress mail=new InternetAddress(mailPerso);
			
			//Génération du code
			//code=this.genererCode();
			code="88888888";
			
			System.out.println("ENVOI DU CODE PAR MAIL");
			//smtpService.send(mail,"Code activation de compte","","Votre code est"+code); 
			System.out.println("ENVOI DU MAIL FAIT");
			
			//insertion dans un HashMap qui établit une correspondance entre l'id et une hashmap contenant code + date d'insertion
			HashMap<String,String> list= new HashMap<String,String>();
			list.put(accessCodeKey,code);
			Date date=new Date();
			list.put(accessDateKey,this.dateToString(date));
			access.put(id, list);
		
		}catch(Exception e){
			
		}
	}
	
	public int validateCode(String id,String code){
		try{
			
			//Si le temps ecoulé est de moins de deux minutes
			if (verifyTime(id)){
				if (verifyCode(id,code)){
					return GOOD;
				}else
					return BADCODE;
			}
		
		}catch(ParseException e){
			
		}
	
		return TIMEOUT;
	}
	
	
	
	public boolean setPassword(final String currentPassword,String id,String code) throws LdapException{

		try {
			
			//si la correspondance code/id est présente dans la table de hashage alors...
			if (verifyCode(id,code)){
				
				//authentification avec numid et mdpinitial
				this.writeableLdapUserService.defineAuthenticatedContext(id, initialPassword);
				
				/*if (logger.isTraceEnabled()) {
					logger.trace("Mot de passe initial : "
							+ account.getInitialPassword());
				}*/
				
				
				LdapUser ldapUser = this.ldapUserService.getLdapUser(id);
				ldapUser.getAttributes().clear();
	
				/* Writing of password in LDAP */
				List<String> listPasswordAttr = new ArrayList<String>();
				
				//
				//Anli redirection du bind LDAP vers Kerberos
				String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
				
				
				logger.debug(redirectKer);
				
				listPasswordAttr.add(redirectKer);
	
				//listPasswordAttr.add(currentPassword);
				ldapUser.getAttributes().put(LdapSchema.getPassword(),listPasswordAttr);
				
				/* Writing of shadowLastChange in LDAP */
				List<String> listShadowLastChangeAttr = new ArrayList<String>();
				Calendar cal = Calendar.getInstance();
				String shadowLastChange = Integer.toString((int) Math.floor(cal
						.getTimeInMillis()
						/ (1000 * 3600 * 24)));
							
				if (logger.isDebugEnabled()) {
					logger.debug("Writing shadowLastChange in LDAP : "
							+ shadowLastChange);
				}
	
				listShadowLastChangeAttr.add(shadowLastChange);
				ldapUser.getAttributes().put(LdapSchema.getShadowLastChange(),listShadowLastChangeAttr);
				
				
				/* Writing of displayName in LDAP */
				List<String> listDisplayNameAttr = new ArrayList<String>();
				listDisplayNameAttr.add(accountDescr.get("diplayName"));
				ldapUser.getAttributes().put(LdapSchema.getDisplayName(),
						listDisplayNameAttr);
				
				
				//Ajout ou modification du mot de passe dans kerberos
				kerberosAdmin.add(id, currentPassword);
				System.out.println(kerberosAdmin.add(id, currentPassword));
				/*if(state==KRBAdmin.ALREADY_EXIST)
					state=kerberosAdmin.changePasswd(id, currentPassword);*/
	
				
				
				this.writeableLdapUserService.updateLdapUser(ldapUser);
				
				ldapUser.getAttributes().clear();
	
				logger.info("Activation du compte : " + id);
	
				this.writeableLdapUserService.defineAnonymousContext();
			
			}
			else{
				//Throw nouvelle exception correspondant au code
			}
		
		} catch (LdapException e) {
			logger.error(e.getMessage());
			throw e;
		}

		return true;
	}
	
	
	public WriteableLdapUserService getWriteableLdapUserService() {
		return writeableLdapUserService;
	}

	public void setWriteableLdapUserService(
			
			WriteableLdapUserService writeableLdapUserService) {
			this.writeableLdapUserService = writeableLdapUserService;
	}

	
	
	public void updateDisplayName(String displayName,String id, String code){
		if (verifyCode(id,code)){
			accountDescr.remove("displayName");
			accountDescr.put("diplayName", displayName);
		}
		else{
			//Throw nouvelle exception correspondant au code
		}
	}

	
	
	
	/**
	 * @param krbLdapMethod
	 */
	public final void setKrbLdapMethod(String krbLdapMethod) {
		this.krbLdapMethod = krbLdapMethod;
	}

	/**
	 * @param krbHost
	 */
	public final void setKrbHost(String krbHost) {
		this.krbHost = krbHost;
	}

	/**
	 * @param securityCode
	 */
/*	public final void setSecurityCode(int securityCode) {
		this.securityCode = securityCode;
	}*/
	
	
	/**
	 * @param kerberosAdmin
	 */
	public final void setKerberosAdmin(KRBAdminTest kerberosAdmin) {
		this.kerberosAdmin = kerberosAdmin;
	}
	
	
	private String genererCode(){
		
		Random r = new Random();
		String code= "";
		char [] tableauChiffres = {'0','1','2','3','4','5','6','7','8','9'};
		
		for (int i = 0; i < 8; i++){
		    int indiceChiffre = r.nextInt(tableauChiffres.length);
			// retourne le chiffre correspondant à cette indice
			char chiffre = tableauChiffres[indiceChiffre];
			code=code+chiffre;
		}
		return code;
	}
	
	private String dateToString(Date sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);
        return sdf.format(sDate);
    }
    
	private Date stringToDate(String sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);
        return sdf.parse(sDate);
    }
    
    private boolean verifyCode(String id,String code){
    	//Recuperation du hashmap correspondant à l'id de l'utilisateur
		HashMap <String,String>result=access.get(id);
		if (result!=null){
			if (code.equalsIgnoreCase(result.get(accessCodeKey))){
				return true;
			}
		}
		return false;
    }
    
    private boolean verifyTime(String id)throws ParseException{
    	//Recuperation du hashmap correspondant à l'id de l'utilisateur
		HashMap <String,String>result=access.get(id);
		
		//tester null
		Date date=stringToDate(dateToString(new Date()));
		Long tempsEcoule=date.getTime()-this.stringToDate(result.get(accessDateKey)).getTime();
		if (tempsEcoule<=480000){
			return true;
		}
		return false;
    }
    
    


	public String getAccountIdKey() {
		return accountIdKey;
	}

	public void setAccountIdKey(String accountIdKey) {
		this.accountIdKey = accountIdKey;
	}

	public String getAccountMailKey() {
		return accountMailKey;
	}

	public void setAccountMailKey(String accountMailKey) {
		this.accountMailKey = accountMailKey;
	}

	public String getAccountSLCKey() {
		return accountSLCKey;
	}

	public void setAccountSLCKey(String accountSLCKey) {
		this.accountSLCKey = accountSLCKey;
	}

	public String getAccountDNKey() {
		return accountDNKey;
	}

	public void setAccountDNKey(String accountDNKey) {
		this.accountDNKey = accountDNKey;
	}

	
	public String getAccessCodeKey() {
		return accessCodeKey;
	}

	public void setAccessCodeKey(String accessCodeKey) {
		this.accessCodeKey = accessCodeKey;
	}

	public String getAccessDateKey() {
		return accessDateKey;
	}

	public void setAccessDateKey(String accessDateKey) {
		this.accessDateKey = accessDateKey;
	}

	public String getFormatDateConv() {
		return formatDateConv;
	}

	public void setFormatDateConv(String formatDateConv) {
		this.formatDateConv = formatDateConv;
	}

	public AsynchronousSmtpServiceImpl getSmtpService() {
		return smtpService;
	}

	public void setSmtpService(AsynchronousSmtpServiceImpl smtpService) {
		this.smtpService = smtpService;
	}
	





}
