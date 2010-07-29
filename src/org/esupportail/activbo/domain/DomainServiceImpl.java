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

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.esupportail.activbo.dao.DaoService;
import org.esupportail.activbo.domain.tools.CleaningHashCode;
import org.esupportail.activbo.domain.beans.HashCode;
import org.esupportail.activbo.domain.beans.User;
import org.esupportail.activbo.domain.beans.VersionManager;

import org.esupportail.activbo.domain.tools.StringTools;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBAdmin;
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.activbo.services.kerberos.KRBIllegalArgumentException;
import org.esupportail.activbo.services.kerberos.KRBPrincipalAlreadyExistsException;
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
import org.esupportail.commons.services.smtp.SmtpException;
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
	
	private String clePrimaireValidation;

	/**
	 * {@link DaoService}.
	 */
	private String accountDescrIdKey;
	private String accountDescrMailKey;
	private String accountDescrDNKey;
	private String accountDescrCodeKey;
	
	private int accessCodeDelay;
	
	private String ldapUsernameAdmin;
	private String ldapPasswordAdmin;
	
	private String hashCodeCodeKey;
	private String hashCodeDateKey;
	
	private List<String>attrPersoInfo;
	
	private String formatDateConv;
	
	private AsynchronousSmtpServiceImpl smtpService;
	
	private String mailCodeSubject;
	private String mailCodeBody;
	
	public String code;
		
	private HashCode hashCode;
	/**
	 * kerberos.ldap.method
	 * kerberos.host
	 */
	private String krbLdapMethod,krbHost;
	
	
	/**
	 * {@link KerberosAdmin}
	 */
	private KRBAdmin kerberosAdmin;
	
		
	private String codeTransmission;
	
	private String codeTransmissionMail;
	
	private String codeTransmissionSms;
	
	
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
		
	
	private WriteableLdapUserService writeableLdapUserService;

		
	private CleaningHashCode clean;
	
	
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
		
		logger.info("Lancement du thread de nettoyage de la table de hashage");
		clean.start();
		
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
	
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws LdapProblemException{
		
		HashMap<String, String> accountDescr=new HashMap<String,String>();
		
		try {
			
			this.attrPersoInfo=attrPersoInfo;
					
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+clePrimaireValidation+"="+ hashInfToValidate.get(clePrimaireValidation) + ")");
				
			if (ldapUserList.size() == 0) {
				return null;
			}
	
			LdapUser ldapUser = ldapUserList.get(0); 
							
				
			if (logger.isDebugEnabled()) {
					logger.debug("Validating account for : " + ldapUser);
			}
			
			//Parcours des informations � valider
			Iterator<Map.Entry<String,String>> it=hashInfToValidate.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String,String> e=it.next();
				
				//on ne teste pas la validit� de la valeur cl�
				if (!e.getKey().equals(clePrimaireValidation)){
					String ldapUserAttrValue = ldapUser.getAttribute(e.getKey());
					
					if (ldapUserAttrValue == null) {
						String errmsg = "LDAP account with "+ this.clePrimaireValidation+" "+ hashInfToValidate.get(clePrimaireValidation) + " has no "+e.getKey();
						logger.error(errmsg);
						throw new InvalidLdapAccountException(errmsg);
					}
	
					if (!ldapUserAttrValue.equals(e.getValue())){
						logger.info("Invalid value "+e.getValue()+" associate to attribute "+e.getKey()+" for LDAP Account with "+clePrimaireValidation+" "+hashInfToValidate.get(clePrimaireValidation));
						return null;
					}
				}
			}
			//Construction du hasMap de retour
			accountDescr.put(accountDescrIdKey, ldapUser.getId());
			accountDescr.put(accountDescrMailKey, ldapUser.getAttribute(LdapSchema.getMail()));
			
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), ldapUser.getAttribute(attrPersoInfo.get(j)));
			}
		
			//envoi d'un code si le compte n'est pas activ�
			if (ldapUser.getAttribute(LdapSchema.getShadowLastChange())==null){
				String code=this.genererCode();
				accountDescr.put(accountDescrCodeKey, code);
				this.insertCodeInHash(ldapUser.getId(),code);
				logger.debug("Insertion code pour l'utilisateur "+ldapUser.getId()+" dans la table effectu�e");
			}
				
			} catch (LdapException e) {
				logger.debug("Exception thrown by updateInfoPerso() : "+ e.getMessage());
				throw new LdapProblemException("Probleme au niveau du LDAP");
			}
		
		return accountDescr;
	}

	
	public boolean updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException{
		
		try{

			if (verifyCode(id,code)){/*security reasons*/
	
				this.writeableLdapUserService.defineAuthenticatedContext(ldapUsernameAdmin, ldapPasswordAdmin);
				logger.info("Authentification LDAP r�ussie");
				
				LdapUser ldapUser = this.ldapUserService.getLdapUser(id);
				ldapUser.getAttributes().clear();
				
				logger.debug("Parcours des informations personnelles mises � jour au niveau du FO pour insertion LDAP");
				Iterator<Map.Entry<String,String>> it=hashBeanPersoInfo.entrySet().iterator();
				int i=0;
				while(it.hasNext()){
					List<String> list=new ArrayList<String>();
					Map.Entry<String,String> e=it.next();
					list.add(e.getValue());
					ldapUser.getAttributes().put(attrPersoInfo.get(i),list);
					i++;
				}
				
				this.finalizeLdapWriting(ldapUser);
			}
			else{
				throw new UserPermissionException("L'utilisateur n'a pas le droit de continuer l'activation");
			}
		
		} catch (LdapException e) {
			logger.debug("Exception thrown by updateInfoPerso() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}

		return true;
	}
	
	
	public String getCode(String id,String canal){
		
		return null;
		
	}
	
	
	public String getCode(String id){
			
		String code=this.genererCode();
		this.insertCodeInHash(id,code);
		logger.info("Insertion code pour l'utilisateur "+id+" dans la table effectu�e");
		
		//Lecture LDAP pour obtenir l'adresse mail Perso
		/*List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("(uid="+ id + ")");
		
		if (ldapUserList.size() == 0) {
			return null;
		}

		LdapUser ldapUserRead = ldapUserList.get(0); 
		
		String mailPerso=ldapUserRead.getAttribute(accountMailKey);*/
		String mailPerso="lorvivien@yahoo.fr";
		if (codeTransmission.equals(codeTransmissionMail)){
			InternetAddress mail=null;
			try {
				mail = new InternetAddress(mailPerso);
				smtpService.send(mail,this.mailCodeSubject,this.mailCodeBody+" "+code,"");
				logger.info("Envoi du code � l'adresse mail "+mailPerso);
			
			}catch (Exception e) {
				return null;
			}
		}
		
		else{
			//Evoi du code par sms
		}
		
		return code;
	}
	
	public boolean setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException{
		
		LdapUser ldapUser=null;
		
		try {
			
			
			if (verifyCode(id,code)){/*security reasons*/
				
				this.writeableLdapUserService.defineAuthenticatedContext(this.ldapUsernameAdmin, ldapPasswordAdmin);
				logger.info("Authentification LDAP r�ussie");
								
				//Lecture LDAP
				List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("(uid="+ id + ")");
				
				if (ldapUserList.size() == 0) {
					return false;
				}

				LdapUser ldapUserRead = ldapUserList.get(0); 
				
				String ldapUserRedirectKerb = ldapUserRead.getAttribute("userPassword");
				String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
				
				logger.info("Lecture du ldap: "+ldapUserRead.toString());
				
				ldapUser = this.ldapUserService.getLdapUser(id);
				
				if (true){//if (!ldapUserRedirectKerb.equals(redirectKer)) {
					logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");
					
					/* Writing of Kerberos redirection in LDAP */
					List<String> listPasswordAttr = new ArrayList<String>();
					listPasswordAttr.add(redirectKer);
					ldapUser.getAttributes().put(LdapSchema.getPassword(),listPasswordAttr);
					if (logger.isDebugEnabled()) {
						logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
					}
				}			
				//si SLC null{
				/* Writing of shadowLastChange in LDAP */
				List<String> listShadowLastChangeAttr = new ArrayList<String>();
				Calendar cal = Calendar.getInstance();
				String shadowLastChange = Integer.toString((int) Math.floor(cal.getTimeInMillis()/ (1000 * 3600 * 24)));
				listShadowLastChangeAttr.add(shadowLastChange);
				ldapUser.getAttributes().put(LdapSchema.getShadowLastChange(),listShadowLastChangeAttr);
				if (logger.isDebugEnabled()) {
					logger.debug("Writing shadowLastChange in LDAP : " + shadowLastChange);
				}
		
				
				//Ajout ou modification du mot de passe dans kerberos
				kerberosAdmin.add(id, currentPassword);
				logger.info("Ajout de mot de passe dans kerberos effectu�e");

				this.finalizeLdapWriting(ldapUser);
							
			
			}else{
				throw new UserPermissionException("L'utilisateur n'a pas le droit de continuer l'activation");
			}
		
		} catch (KRBPrincipalAlreadyExistsException e){
			
			try{
				logger.info("Le compte kerberos existe d�ja, Modification du password");
				kerberosAdmin.changePasswd(id, currentPassword);
				this.finalizeLdapWriting(ldapUser);
			
			} catch (KRBIllegalArgumentException ie) {
				logger.debug("Exception thrown by setPassword() : "+ ie.getMessage());
				throw new KerberosException("Probleme au niveau de Kerberos");
			
			}catch(KRBException ke){
				logger.debug("Exception thrown by setPassword() : "+ ke.getMessage());
				throw new KerberosException("Probleme au niveau de Kerberos");
			}
			
		}catch(KRBException ke){
			logger.debug("Exception thrown by setPassword() : "+ ke.getMessage());
			throw new KerberosException("Probleme au niveau de Kerberos");
		
		} catch (LdapException e) {
			logger.debug("Exception thrown by setPassword() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}

		return true;
	}
	
	public HashMap<String,String> setPassword(String id,String oldPassword,final String currentPassword,List<String>attrPersoInfo)throws LdapProblemException,UserPermissionException,KerberosException{
		
		HashMap<String, String> accountDescr=new HashMap<String,String>();
		
		try{
			
			this.writeableLdapUserService.defineAuthenticatedContext(this.ldapUsernameAdmin, ldapPasswordAdmin);
			logger.info("Authentification LDAP r�ussie");
			
			
			//Lecture LDAP
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("(uid="+ id + ")");
			
			if (ldapUserList.size() == 0) {
				return null;
			}
	
			LdapUser ldapUserRead = ldapUserList.get(0); 
			
			String ldapUserRedirectKerb = ldapUserRead.getAttribute("userPassword");
			String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
			
			logger.info("Lecture du ldap: "+ldapUserRead.toString());
	
			LdapUser ldapUser = this.ldapUserService.getLdapUser(id);
			
			//si pas de redirection --> lecture du mot de passe dans le LDAP
			if (true){//if (!ldapUserRedirectKerb.equals(redirectKer)) {
				logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");
							
				if (ldapUserRedirectKerb.equals(oldPassword)){
					logger.debug("Ancien Password valide dans le LDAP");
					kerberosAdmin.add(id, currentPassword);
					logger.info("Ajout du nouveau mot de passe dans kerberos effectu�e");
				}
				
				/* Writing of Kerberos redirection in LDAP */
				List<String> listPasswordAttr = new ArrayList<String>();
				listPasswordAttr.add(redirectKer);
				ldapUser.getAttributes().put(LdapSchema.getPassword(),listPasswordAttr);
				if (logger.isDebugEnabled()) {
					logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
				}
			}
			
			kerberosAdmin.changePasswd(id,oldPassword, currentPassword);
			
			//Construction du hasMap de retour
			accountDescr.put(accountDescrIdKey, ldapUser.getId());
			accountDescr.put(accountDescrMailKey, ldapUser.getAttribute(LdapSchema.getMail()));
					
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), ldapUserRead.getAttribute(attrPersoInfo.get(j)));
			}
		
			//envoi d'un code si le compte n'est pas activ�
			String code=new String(this.genererCode());
			accountDescr.put(accountDescrCodeKey, code);
			this.insertCodeInHash(ldapUser.getId(),code);
			logger.debug("Insertion code pour l'utilisateur "+ldapUser.getId()+" dans la table effectu�e");
			
		} catch (KRBPrincipalAlreadyExistsException e){
			
			try{
				logger.info("Le compte kerberos existe d�ja, changement du password");
				kerberosAdmin.changePasswd(id,oldPassword, currentPassword);
			
			}catch(KRBException ke){
				logger.debug("Exception thrown by setPassword() : "+ ke.getMessage());
				//changer le nom de l'exception pour l'echec de mauvais mot de passe?????? 
				throw new KerberosException("Authentification invalide");
			}
		
		}catch(KRBException ke){
			logger.debug("Exception thrown by setPassword() : "+ ke.getMessage());
			throw new KerberosException("Probleme au niveau de Kerberos");
		
		} catch (LdapException e) {
			logger.debug("Exception thrown by setPassword() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}
		return accountDescr;
		
	}
	
	
	public WriteableLdapUserService getWriteableLdapUserService() {
		return writeableLdapUserService;
	}

	public void setWriteableLdapUserService(
			
			WriteableLdapUserService writeableLdapUserService) {
			this.writeableLdapUserService = writeableLdapUserService;
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
	 * @param kerberosAdmin
	 */
	public final void setKerberosAdmin(KRBAdmin kerberosAdmin) {
		this.kerberosAdmin = kerberosAdmin;
	}
	
	
	private String genererCode(){
		
		Random r = new Random();
		String code= "";
		char [] tableauChiffres = {'0','1','2','3','4','5','6','7','8','9'};
		
		for (int i = 0; i < 8; i++){
		    int indiceChiffre = r.nextInt(tableauChiffres.length);
			// retourne le chiffre correspondant � cette indice
			char chiffre = tableauChiffres[indiceChiffre];
			code=code+chiffre;
		}
		return code;
	}
	
	private String dateToString(Date sDate) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(formatDateConv);
        return sdf.format(sDate);
    }
    
    private boolean verifyCode(String id,String code){
    	//Recuperation du hashmap correspondant � l'id de l'utilisateur
		HashMap <String,String>result=hashCode.getValueWithKey(id);
		if (result!=null){
			logger.info("L'utilisateur "+id+" poss�de un code");//l'utilisateur poss�de un code 
			if (code.equalsIgnoreCase(result.get(hashCodeCodeKey))){
				logger.info("Code utilisateur "+id+" valide");//code invalide
				return true;
			}
			else{
				logger.warn("Attention!!!, code en param�tre de la m�thode invalide");
			}
		}
		else{
			logger.info("Pas de code pour l'utilisateur "+id);

		}
		return false;
		
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
	
	public void insertCodeInHash(String id,String code){
		
		HashMap<String,String> hashCodeDate= new HashMap<String,String>();
		hashCodeDate.put(hashCodeCodeKey,code);
		
		
		Calendar c = new GregorianCalendar();
		c.add(Calendar.MINUTE,accessCodeDelay);
				
		try {
			hashCodeDate.put(hashCodeDateKey,this.dateToString(c.getTime()));
		} catch (ParseException e) {
		
		}
		hashCode.putKeyValue(id, hashCodeDate);
	}

	public CleaningHashCode getClean() {
		return clean;
	}

	public void setClean(CleaningHashCode clean) {
		this.clean = clean;
	}
	
	public HashCode getHashCode() {
		return hashCode;
	}

	public void setHashCode(HashCode hashCode) {
		this.hashCode = hashCode;
	}

	public String getLdapUsernameAdmin() {
		return ldapUsernameAdmin;
	}

	public void setLdapUsernameAdmin(String ldapUsernameAdmin) {
		this.ldapUsernameAdmin = ldapUsernameAdmin;
	}

	public String getLdapPasswordAdmin() {
		return ldapPasswordAdmin;
	}

	public void setLdapPasswordAdmin(String ldapPasswordAdmin) {
		this.ldapPasswordAdmin = ldapPasswordAdmin;
	}

	public String getHashCodeCodeKey() {
		return hashCodeCodeKey;
	}

	public void setHashCodeCodeKey(String hashCodeCodeKey) {
		this.hashCodeCodeKey = hashCodeCodeKey;
	}

	public String getHashCodeDateKey() {
		return hashCodeDateKey;
	}

	public void setHashCodeDateKey(String hashCodeDateKey) {
		this.hashCodeDateKey = hashCodeDateKey;
	}

	

	public int getAccessCodeDelay() {
		return accessCodeDelay;
	}

	public void setAccessCodeDelay(int accessCodeDelay) {
		this.accessCodeDelay = accessCodeDelay;
	}
	
	
	private void finalizeLdapWriting(LdapUser ldapUser){
		this.writeableLdapUserService.updateLdapUser(ldapUser);
		ldapUser.getAttributes().clear();
		this.writeableLdapUserService.defineAnonymousContext();
		logger.info("Ecriture dans le LDAP r�ussie");
	}
	
	/*public void setMailPerso(String id,String mailPerso){
	
	try{
		this.mailPerso=mailPerso;
		//InternetAddress mail=new InternetAddress(mailPerso);
		
		//G�n�ration du code
		//code=this.genererCode();
		code="88888888";
		
		System.out.println("ENVOI DU CODE PAR MAIL");
		//smtpService.send(mail,"Code activation de compte","","Votre code est"+code); 
		System.out.println("ENVOI DU MAIL FAIT");
		
		
	
	}catch(Exception e){
		
	}
}*/

	public boolean validateCode(String id,String code){
				//Si le temps ecoul� est de moins de deux minutes
		if (verifyCode(id,code)){
			return true;
		
			
		}
		return false;
	}
	
	public String getCodeTransmission() {
		return codeTransmission;
	}

	public void setCodeTransmission(String codeTransmission) {
		this.codeTransmission = codeTransmission;
	}

	public String getCodeTransmissionMail() {
		return codeTransmissionMail;
	}

	public void setCodeTransmissionMail(String codeTransmissionMail) {
		this.codeTransmissionMail = codeTransmissionMail;
	}

	public String getCodeTransmissionSms() {
		return codeTransmissionSms;
	}

	public void setCodeTransmissionSms(String codeTransmissionSms) {
		this.codeTransmissionSms = codeTransmissionSms;
	}

	public String getClePrimaireValidation() {
		return clePrimaireValidation;
	}

	public void setClePrimaireValidation(String clePrimaireValidation) {
		this.clePrimaireValidation = clePrimaireValidation;
	}

	public String getMailCodeSubject() {
		return mailCodeSubject;
	}

	public void setMailCodeSubject(String mailCodeSubject) {
		this.mailCodeSubject = mailCodeSubject;
	}

	public String getMailCodeBody() {
		return mailCodeBody;
	}

	public void setMailCodeBody(String mailCodeBody) {
		this.mailCodeBody = mailCodeBody;
	}

	public String getAccountDescrIdKey() {
		return accountDescrIdKey;
	}

	public void setAccountDescrIdKey(String accountDescrIdKey) {
		this.accountDescrIdKey = accountDescrIdKey;
	}

	public String getAccountDescrMailKey() {
		return accountDescrMailKey;
	}

	public void setAccountDescrMailKey(String accountDescrMailKey) {
		this.accountDescrMailKey = accountDescrMailKey;
	}

	public String getAccountDescrDNKey() {
		return accountDescrDNKey;
	}

	public void setAccountDescrDNKey(String accountDescrDNKey) {
		this.accountDescrDNKey = accountDescrDNKey;
	}

	public String getAccountDescrCodeKey() {
		return accountDescrCodeKey;
	}

	public void setAccountDescrCodeKey(String accountDescrCodeKey) {
		this.accountDescrCodeKey = accountDescrCodeKey;
	}
}
