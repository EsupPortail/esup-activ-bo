/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.esupportail.activbo.dao.DaoService;
import org.esupportail.activbo.domain.tools.CleaningValidationCode;
import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.activbo.domain.beans.User;
import org.esupportail.activbo.domain.beans.VersionManager;
import org.esupportail.activbo.domain.beans.channels.Channel;
import org.esupportail.activbo.domain.beans.channels.ChannelException;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBAdmin;
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.activbo.services.kerberos.KRBIllegalArgumentException;
import org.esupportail.activbo.services.kerberos.KRBPrincipalAlreadyExistsException;
import org.esupportail.activbo.services.ldap.InvalidLdapAccountException;
import org.esupportail.activbo.services.ldap.LdapSchema;
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
	
	private String cleValidationPers;
	private String cleValidationEtu;
	
	private List<Channel> channels;
	
	/**
	 * {@link DaoService}.
	 */
	//ce qui servira de login?????? c'est comme ca que je le vois, c'est pour ca que j'utilise pas le ldapSchema sachant qu'on pourra prendre supannAliasLoin ou uid)
	private String accountDescrIdKey;
	
	private LdapSchema ldapSchema;
	
	private String accountDescrCodeKey;	
			
	private ValidationCode validationCode;
	/**
	 * kerberos.ldap.method
	 * kerberos.host
	 */
	private String krbLdapMethod,krbHost;
	
	
	/**
	 * {@link KerberosAdmin}
	 */
	private KRBAdmin kerberosAdmin;
	
		
	
	private DaoService daoService;

	/**
	 * {@link LdapUserService}.
	 */
	private LdapUserService ldapUserService;

	/**
	 * The LDAP attribute that contains the display name. 
	 */
	private String displayNameLdapAttribute;
	
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	private WriteableLdapUserService writeableLdapUserService;

		
	private CleaningValidationCode clean;
	
	
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
		Assert.notNull(this.channels, 
				"property channels of class " + this.getClass().getName() + " can not be null");
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
		String cle=null;
		HashMap<String, String> accountDescr=new HashMap<String,String>();
		List<LdapUser> ldapUserList=null;
		
		try {
			if (hashInfToValidate.containsKey(cleValidationPers)){
				cle=cleValidationPers;
				ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+cleValidationPers+"="+ hashInfToValidate.get(cleValidationPers) + ")");
			}
			else if (hashInfToValidate.containsKey(cleValidationEtu)){
				cle=cleValidationEtu;
				ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+cleValidationEtu+"="+ hashInfToValidate.get(cleValidationEtu) + ")");
			}
				
				
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
				if (!e.getKey().equals(cle)){
					String ldapUserAttrValue = ldapUser.getAttribute(e.getKey());
					
					if (ldapUserAttrValue == null) {
						String errmsg = "LDAP account with "+ cle+" "+ hashInfToValidate.get(cle) + " has no "+e.getKey();
						logger.error(errmsg);
						throw new InvalidLdapAccountException(errmsg);
					}
	
					if (!ldapUserAttrValue.equals(e.getValue())){
						logger.info("Invalid value "+e.getValue()+" associate to attribute "+e.getKey()+" for LDAP Account with "+cle+" "+hashInfToValidate.get(cle));
						return null;
					}
				}
			}
			//Construction du hasMap de retour
			accountDescr.put(accountDescrIdKey, ldapUser.getAttribute(accountDescrIdKey));
			accountDescr.put(ldapSchema.getMail(), ldapUser.getAttribute(ldapSchema.getMail()));
			
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), ldapUser.getAttribute(attrPersoInfo.get(j)));
			}
			
			//envoi d'un code si le compte n'est pas activ�
			if (ldapUser.getAttribute(ldapSchema.getShadowLastChange())==null){
				String code=validationCode.generateCode(ldapUser.getAttribute(accountDescrIdKey),1800);
				accountDescr.put(accountDescrCodeKey,code);
				logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(accountDescrIdKey)+" dans la table effectu�e");
			}
				
			} catch (LdapException e) {
				logger.debug("Exception thrown by validateAccount() : "+ e.getMessage());
				throw new LdapProblemException("Probleme au niveau du LDAP");
			}
		
		return accountDescr;
	}

	
	public boolean updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException{
		
		try{

			if (validationCode.verify(id,code)){/*security reasons*/
				this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(),ldapSchema.getPasswordAdmin());
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
					ldapUser.getAttributes().put(e.getKey(),list);
					i++;
				}
				
				this.finalizeLdapWriting(ldapUser);
			}
			else{
				throw new UserPermissionException("L'utilisateur n'a pas le droit de continuer l'activation");
			}
		
		} catch (LdapException e) {
			logger.debug("Exception thrown by updatePersonalInfo() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}

		return true;
	}
	
	
	public boolean getCode(String id,String canal)throws LdapProblemException{
		
		for(Channel c:channels)
			if(c.getName().equalsIgnoreCase(canal))
				try {
					c.send(id);
					break;
				} catch (ChannelException e) {
					// TODO supprimer le try catch et remplacer LdapProblemException par ChannelException 
					e.printStackTrace();
				}
		//TODO supprimer le return et changer le nom de la méthode
		return true;
		
	}
	
	public boolean setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException{
		
		LdapUser ldapUser=null;
		
		try {
			
			
			if (validationCode.verify(id,code)){/*security reasons*/
				
				//Lecture LDAP
				List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+accountDescrIdKey+"="+ id + ")");
				
				if (ldapUserList.size() == 0) {
					return false;
				}

				LdapUser ldapUserRead = ldapUserList.get(0); 
				
				String ldapUserRedirectKerb = ldapUserRead.getAttribute(ldapSchema.getPassword());
				String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
				
				this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(), ldapSchema.getPasswordAdmin());
				ldapUser = this.ldapUserService.getLdapUser(id);
				
				if (true){//if (!ldapUserRedirectKerb.equals(redirectKer)) {
					logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");

					/* Writing of Kerberos redirection in LDAP */
					List<String> listPasswordAttr = new ArrayList<String>();
					listPasswordAttr.add(redirectKer);
					ldapUser.getAttributes().put(ldapSchema.getPassword(),listPasswordAttr);
					if (logger.isDebugEnabled()) {
						logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
					}
				}
				
				if (ldapUserRead.getAttribute(ldapSchema.getShadowLastChange())==null){//si SLC null{
					/* Writing of shadowLastChange in LDAP */
					List<String> listShadowLastChangeAttr = new ArrayList<String>();
					Calendar cal = Calendar.getInstance();
					String shadowLastChange = Integer.toString((int) Math.floor(cal.getTimeInMillis()/ (1000 * 3600 * 24)));
					listShadowLastChangeAttr.add(shadowLastChange);
					ldapUser.getAttributes().put(ldapSchema.getShadowLastChange(),listShadowLastChangeAttr);
					if (logger.isDebugEnabled()) {
						logger.debug("Writing shadowLastChange in LDAP : " + shadowLastChange);
					}
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
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException{
		
		HashMap<String, String> accountDescr=new HashMap<String,String>();
		
		try{
			this.writeableLdapUserService.defineAuthenticatedContextForUser(id, password);
			
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+accountDescrIdKey+"="+id+ ")");
			
			if (ldapUserList.size() == 0) {
				return null;
			}
	
			LdapUser ldapUser = ldapUserList.get(0); 
				
			if (logger.isDebugEnabled()) {
					logger.debug("Validating account for : " + ldapUser);
			}
			
			//Construction du hasMap de retour
			accountDescr.put(accountDescrIdKey, ldapUser.getAttribute(accountDescrIdKey));
			accountDescr.put(ldapSchema.getMail(), ldapUser.getAttribute(ldapSchema.getMail()));
			
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), ldapUser.getAttribute(attrPersoInfo.get(j)));
			}
			//envoi d'un code si le compte n'est pas activ�
			if (ldapUser.getAttribute(ldapSchema.getShadowLastChange())!=null){
				String code=validationCode.generateCode(ldapUser.getAttribute(accountDescrIdKey),1800);
				accountDescr.put(accountDescrCodeKey,code );
				logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(accountDescrIdKey)+" dans la table effectu�e");
			}
			
		}catch(LdapException e){
			logger.debug("Exception thrown by authentificateUser() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}
		return accountDescr;
	}
	
	
	    
    public boolean changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException{
    	LdapUser ldapUser=null;
    	try{
	    	if (validationCode.verify(id,code)){/*security reasons*/

	    		List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+accountDescrIdKey+"="+newLogin+ ")");
				
				if (ldapUserList.size() == 0) {
					throw new LdapLoginAlreadyExistsException("");
				}
	    		
	    		this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(), ldapSchema.getPasswordAdmin());
				logger.info("Authentification LDAP r�ussie");
				
				ldapUser = this.ldapUserService.getLdapUser(id);
				ldapUser.getAttributes().clear();
				
				List<String> list=new ArrayList<String>();
				list.add(newLogin);
				ldapUser.getAttributes().put(accountDescrIdKey,list);
				
				kerberosAdmin.rename(id,newLogin);
				this.finalizeLdapWriting(ldapUser);
					    	
	    	}else{
	    		throw new UserPermissionException("L'utilisateur n'a pas le droit de continuer l'activation");
	    	}
		
    	
    	}catch (LdapException e) {
			logger.debug("Exception thrown by changeLogin() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");    	
    	}catch(KRBPrincipalAlreadyExistsException e){
			logger.debug("Exception thrown by changeLogin() : "+ e.getMessage());
			throw new LoginAlreadyExistsException("Le login existe d�ja");
			
    	}catch(KRBException e){
			logger.debug("Exception thrown by changeLogin() : "+ e.getMessage());
			throw new KerberosException("Probleme au niveau de Kerberos");
    	
    	}catch(LdapLoginAlreadyExistsException e){
			logger.debug("Exception thrown by changeLogin() : "+ e.getMessage());
			throw new LoginAlreadyExistsException("Le login existe d�ja");
    	}	

    	return true;
    	
	}
    
    public boolean validateCode(String id,String code) {
		
		try {
			if (validationCode.verify(id,code)){
				return true;
			}
		} catch (UserPermissionException e) {
			// TODO supprimer le try catch et échaper l'exception de la méthode
			e.printStackTrace();
		}
		return false;
	}
			
	public CleaningValidationCode getClean() {
		return clean;
	}

	public void setClean(CleaningValidationCode clean) {
		this.clean = clean;
	}
	
	public ValidationCode getValidationCode() {
		return validationCode;
	}

	public void setValidationCode(ValidationCode validationCode) {
		this.validationCode = validationCode;
	}
		
	private void finalizeLdapWriting(LdapUser ldapUser){
		this.writeableLdapUserService.updateLdapUser(ldapUser);
		ldapUser.getAttributes().clear();
		this.writeableLdapUserService.defineAnonymousContext();
		logger.info("Ecriture dans le LDAP r�ussie");
	}
	
	public String getAccountDescrIdKey() {
		return accountDescrIdKey;
	}

	public void setAccountDescrIdKey(String accountDescrIdKey) {
		this.accountDescrIdKey = accountDescrIdKey;
	}

	public String getAccountDescrCodeKey() {
		return accountDescrCodeKey;
	}

	public void setAccountDescrCodeKey(String accountDescrCodeKey) {
		this.accountDescrCodeKey = accountDescrCodeKey;
	}

	public String getCleValidationEtu() {
		return cleValidationEtu;
	}

	public void setCleValidationEtu(String cleValidationEtu) {
		this.cleValidationEtu = cleValidationEtu;
	}

	public String getCleValidationPers() {
		return cleValidationPers;
	}

	public void setCleValidationPers(String cleValidationPers) {
		this.cleValidationPers = cleValidationPers;
	}

	public LdapSchema getLdapSchema() {
		return ldapSchema;
	}

	public void setLdapSchema(LdapSchema ldapSchema) {
		this.ldapSchema = ldapSchema;
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

	/**
	 * @param channels the channels to set
	 */
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}


	
	
	
}
