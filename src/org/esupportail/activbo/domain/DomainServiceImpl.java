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
import java.util.Set;

import org.esupportail.activbo.dao.DaoService;
import org.esupportail.activbo.domain.tools.BruteForceBlock;
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
	 * liste des canaux pouvant être gérés par l'application
	 */
	private List<Channel> channels;
	
	/**
	 * {@link DaoService}.
	 */

	
	private LdapSchema ldapSchema;
	
	private String accountDescrCodeKey;	
	
	private String accountDescrPossibleChannelsKey;
			
	private ValidationCode validationCode;
	
	
	private BruteForceBlock bruteForceBlock;
	
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

		
	private CleaningValidationCode cleaningValidationCode;
	
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
	
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws AuthentificationException, LdapProblemException{
		HashMap<String,String> accountDescr=new HashMap<String,String>();
		List<LdapUser> ldapUserList=null;
		
		/**
		 * Construction d'un filtre ldap à partir des données à valider.
		 * Si l'application du filtre retourne une entrée, c'est que les données sont valides
		 */
		
		String filter="(&";
		Set<String> keys=hashInfToValidate.keySet();
		for(String key:keys)
		{
			String value=hashInfToValidate.get(key);
			
			//Suppression des caractères spéciaux susceptibles de permettre une injection ldap
			value=value.replace("&","");
			value=value.replace(")","");
			value=value.replace("(","");
			value=value.replace("|","");
			value=value.replace("*","");
			value=value.replace("=","");
			
			filter+="("+key+"="+value+")";
		}
		filter+=")";
		if (logger.isDebugEnabled()) 
			logger.debug("Le filtre construit pour la validation est : "+filter);
		
		ldapUserList = this.ldapUserService.getLdapUsersFromFilter(filter);									
				
		if (ldapUserList.size() == 0) throw new AuthentificationException("Identification �chouée : "+filter);
	
		LdapUser ldapUser = ldapUserList.get(0);
				
		//Construction du hasMap de retour
		accountDescr.put(ldapSchema.getLogin(), convertListToString(ldapUser.getAttributes(ldapSchema.getLogin())));
		accountDescr.put(ldapSchema.getMail(), convertListToString(ldapUser.getAttributes(ldapSchema.getMail())));
			
		for (int j=0;j<attrPersoInfo.size();j++){
			accountDescr.put(attrPersoInfo.get(j), convertListToString(ldapUser.getAttributes(attrPersoInfo.get(j))));
		}
		logger.debug("OK pour la methode3");	
		//envoi d'un code si le compte n'est pas activ�
		if (ldapUser.getAttribute(ldapSchema.getShadowLastChange())==null){
			
			String code=validationCode.generateCode(ldapUser.getAttribute(ldapSchema.getLogin()));
			List<String>list=new ArrayList<String>();
			list.add(code);
			accountDescr.put(accountDescrCodeKey,convertListToString(list));
			logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(ldapSchema.getLogin())+" dans la table effectu�e");
		}
		
		String possibleChannels="";
		for(Channel c:channels){
			if(c.isPossible(ldapUser)){
				if (possibleChannels.isEmpty()) possibleChannels+=c.getName();
				else possibleChannels+=","+c.getName();
			}
		}
		List<String>list=new ArrayList<String>();
		list.add(possibleChannels);
		
		accountDescr.put(accountDescrPossibleChannelsKey, convertListToString(list));
		
		logger.debug(accountDescr.toString());
		
		return accountDescr;
	}

	
	public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException{
		
		try{
			if (validationCode.verify(id,code)){/*security reasons*/
				
				//Lecture LDAP
				List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+ldapSchema.getLogin()+"="+ id + ")");
				LdapUser ldapUser = ldapUserList.get(0); 
				
				this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(),ldapSchema.getPasswordAdmin());
				
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
	}
	
	
	public boolean getCode(String id,String canal)throws LdapProblemException{
		//validationCode.generateCode(id);
		//return true;
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
	
	public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException{
		
		LdapUser ldapUser=null;
		LdapUser ldapUserRead=null;
		try {
			
			
			if (validationCode.verify(id,code)){/*security reasons*/
				
				//Lecture LDAP
				List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+ldapSchema.getLogin()+"="+ id + ")");
				

				ldapUserRead = ldapUserList.get(0); 
				
				String ldapUserRedirectKerb = ldapUserRead.getAttribute(ldapSchema.getPassword());
				String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
				
				this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(), ldapSchema.getPasswordAdmin());
				//ldapUser = this.ldapUserService.getLdapUser(id);
				
				if (true){//if (!ldapUserRedirectKerb.equals(redirectKer)) {
					logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");

					/* Writing of Kerberos redirection in LDAP */
					List<String> listPasswordAttr = new ArrayList<String>();
					listPasswordAttr.add(redirectKer);
					ldapUserRead.getAttributes().put(ldapSchema.getPassword(),listPasswordAttr);
					if (logger.isDebugEnabled()) {
						logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
					}
				}
				
				if (ldapUserRead.getAttribute(ldapSchema.getShadowLastChange())==null){
					/* Writing of shadowLastChange in LDAP */
					List<String> listShadowLastChangeAttr = new ArrayList<String>();
					Calendar cal = Calendar.getInstance();
					String shadowLastChange = Integer.toString((int) Math.floor(cal.getTimeInMillis()/ (1000 * 3600 * 24)));
					listShadowLastChangeAttr.add(shadowLastChange);
					ldapUserRead.getAttributes().put(ldapSchema.getShadowLastChange(),listShadowLastChangeAttr);
					if (logger.isDebugEnabled()) {
						logger.debug("Writing shadowLastChange in LDAP : " + shadowLastChange);
					}
				}	
				
				//Ajout ou modification du mot de passe dans kerberos
				kerberosAdmin.add(id, currentPassword);
				logger.info("Ajout de mot de passe dans kerberos effectu�e");

				this.finalizeLdapWriting(ldapUserRead);
							
			
			}else{
				throw new UserPermissionException("L'utilisateur n'a pas le droit de continuer l'activation");
			}
		
		} catch (KRBPrincipalAlreadyExistsException e){
			
			try{
				logger.info("Le compte kerberos existe d�ja, Modification du password");
				kerberosAdmin.changePasswd(id, currentPassword);
				this.finalizeLdapWriting(ldapUserRead);
			
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

		
	}
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException{
		
		HashMap<String, String> accountDescr=new HashMap<String,String>();
		
		try{
			if (bruteForceBlock.isBlocked(id)){
				throw new UserPermissionException("Nombre de tentative d'authentification atteint pour l'utilisateur "+id);
			}
			
			this.writeableLdapUserService.defineAuthenticatedContextForUser(id, password);
			
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+ldapSchema.getLogin()+"="+id+ ")");
			
			if (ldapUserList.size() == 0) {
				return null;
			}
	
			LdapUser ldapUser = ldapUserList.get(0); 
				
			if (logger.isDebugEnabled()) {
					logger.debug("Validating account for : " + ldapUser);
			}
			
			//Construction du hasMap de retour
			accountDescr.put(ldapSchema.getLogin(), convertListToString(ldapUser.getAttributes(ldapSchema.getLogin())));
			accountDescr.put(ldapSchema.getMail(), this.convertListToString(ldapUser.getAttributes(ldapSchema.getMail())));
			
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), this.convertListToString(ldapUser.getAttributes(attrPersoInfo.get(j))));
			}

			//envoi d'un code si le compte n'est pas activ�
			if (ldapUser.getAttribute(ldapSchema.getShadowLastChange())!=null){
				String code=validationCode.generateCode(ldapUser.getAttribute(ldapSchema.getLogin()));
				List<String>list=new ArrayList<String>();
				list.add(code);
				accountDescr.put(accountDescrCodeKey,this.convertListToString(list));
				logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(ldapSchema.getLogin())+" dans la table effectu�e");
			}
			logger.info("Accoutdescr renvoy� : "+accountDescr.toString());
			//si authentification pas bonne 
			bruteForceBlock.setFail(id);
			
		}catch(LdapException e){
			logger.debug("Exception thrown by authentificateUser() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}
		return accountDescr;
	}
	
	
	    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException{
    	LdapUser ldapUser=null;
    	try{
	    	if (validationCode.verify(id,code)){/*security reasons*/

	    		List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+ldapSchema.getLogin()+"="+newLogin+ ")");
				
				if (ldapUserList.size() == 0) {
					throw new LdapLoginAlreadyExistsException("");
				}
	    		
	    		this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(), ldapSchema.getPasswordAdmin());
				logger.info("Authentification LDAP r�ussie");
				
				ldapUser = this.ldapUserService.getLdapUser(id);
				ldapUser.getAttributes().clear();
				
				List<String> list=new ArrayList<String>();
				
				list.add(newLogin);
				ldapUser.getAttributes().put(ldapSchema.getLogin(),list);
				
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

    	
    	
	}
    
    public boolean validateCode(String id,String code)throws UserPermissionException {

    	if (validationCode.verify(id,code)){
			return true;
		}
		return false;
	}
    
    private void finalizeLdapWriting(LdapUser ldapUser){
		logger.debug("L'ecriture dans le LDAP commence");
		this.writeableLdapUserService.updateLdapUser(ldapUser);
		ldapUser.getAttributes().clear();
		this.writeableLdapUserService.defineAnonymousContext();
		logger.debug("Ecriture dans le LDAP r�ussie");
	}

	
	public CleaningValidationCode getCleaningValidationCode() {
		return cleaningValidationCode;
	}

	public void setCleaningValidationCode(
			CleaningValidationCode cleaningValidationCode) {
		this.cleaningValidationCode = cleaningValidationCode;
	}

	public ValidationCode getValidationCode() {
		return validationCode;
	}

	public void setValidationCode(ValidationCode validationCode) {
		this.validationCode = validationCode;
	}
		
	public String getAccountDescrCodeKey() {
		return accountDescrCodeKey;
	}

	public void setAccountDescrCodeKey(String accountDescrCodeKey) {
		this.accountDescrCodeKey = accountDescrCodeKey;
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
	

	public String getAccountDescrPossibleChannelsKey() {
		return accountDescrPossibleChannelsKey;
	}

	public void setAccountDescrPossibleChannelsKey(
			String accountDescrPossibleChannelsKey) {
		this.accountDescrPossibleChannelsKey = accountDescrPossibleChannelsKey;
	}

	/**
	 * @param bruteForceBlock the bruteForceBlock to set
	 */
	public void setBruteForceBlock(BruteForceBlock bruteForceBlock) {
		this.bruteForceBlock = bruteForceBlock;
	}
	
	public String convertListToString(List<String>listString){
		String result="";
		for (int i=0;i<listString.size();i++){
			
			if (result.isEmpty()) result+=listString.get(i);
			else result+=","+listString.get(i);
		}
		
		return result;
		
	}


	
	
	
}
