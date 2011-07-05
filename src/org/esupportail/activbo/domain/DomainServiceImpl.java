/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.esupportail.activbo.dao.DaoService;
import org.esupportail.activbo.domain.tools.BruteForceBlock;
import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.activbo.domain.beans.ValidationProxyTicket;
import org.esupportail.activbo.domain.beans.User;
import org.esupportail.activbo.domain.beans.VersionManager;
import org.esupportail.activbo.domain.beans.channels.Channel;
import org.esupportail.activbo.domain.beans.channels.ChannelException;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBAdmin;
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.activbo.services.kerberos.KRBIllegalArgumentException;
import org.esupportail.activbo.services.kerberos.KRBPrincipalAlreadyExistsException;
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
	
	private ValidationProxyTicket validationProxyTicket;
	
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
	
	private String separator;

	
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

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
	
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws AuthentificationException, LdapProblemException, LoginException{
		
		HashMap<String,String> accountDescr=new HashMap<String,String>();
		
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
		logger.debug("filter : " + filter);
		
		if (logger.isDebugEnabled()) 
			logger.debug("Le filtre construit pour la validation est : "+filter);
		
		LdapUser ldapUser=this.getLdapUser(filter);
				
		if (ldapUser==null) throw new AuthentificationException("Identification �chouée : "+filter);
							
		//Construction du hasMap de retour
		
		accountDescr.put(ldapSchema.getLogin(), convertListToString(ldapUser.getAttributes(ldapSchema.getLogin())));
		accountDescr.put(ldapSchema.getMail(), convertListToString(ldapUser.getAttributes(ldapSchema.getMail())));
        
		for (int j=0;j<attrPersoInfo.size();j++){
			accountDescr.put(attrPersoInfo.get(j), convertListToString(ldapUser.getAttributes(attrPersoInfo.get(j))));
		}
		
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
				if ("".equals(possibleChannels)) possibleChannels+=c.getName();
				else possibleChannels+=getSeparator()+c.getName();
			}
		}
		List<String>list=new ArrayList<String>();
		list.add(possibleChannels);
		//accountDescr.put(accountDescrPossibleChannelsKey, convertListToStringPossibleKey(list));
		accountDescr.put(accountDescrPossibleChannelsKey, convertListToString(list));
		return accountDescr;
	}
	
	public LdapUser getLdapUser(String filter) throws LoginException{
		LdapUser ldapUser=null;
		List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter(filter);
		
		if(ldapUserList.size() !=0){
			ldapUser = ldapUserList.get(0);
			if(ldapUser.getAttribute(ldapSchema.getLogin())== null) 
				throw new LoginException("Le login pour l'utilisateur est null");
		}
		
		return ldapUser;
	}

	
	public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException, LoginException{
		
		try{
			if (validationCode.verify(id,code)){/*security reasons*/
				
				//Lecture LDAP
				
				LdapUser ldapUser=this.getLdapUser("("+ldapSchema.getLogin()+"="+ id + ")");
				 				
				if (ldapUser==null) throw new LdapProblemException("Probleme au niveau du LDAP");
				
				logger.debug("Parcours des informations personnelles mises � jour au niveau du FO pour insertion LDAP");
				
				Iterator<Map.Entry<String,String>> it=hashBeanPersoInfo.entrySet().iterator();
				int i=0;
				while(it.hasNext()){
					
					List<String> list=new ArrayList<String>();
					Map.Entry<String,String> e=it.next();
					
					logger.debug("Key="+e.getKey()+" Value="+e.getValue());
					
					if("".equals(e.getValue())||e.getValue()==null) ldapUser.getAttributes().put(e.getKey(),list);
					else
						if (e.getValue().contains(getSeparator())) {
							List<String> ldapUserMultiValue=Arrays.asList(e.getValue().split(getSeparator()));
							ldapUser.getAttributes().put(e.getKey(),ldapUserMultiValue);
						} else {
							list.add(e.getValue());
							ldapUser.getAttributes().put(e.getKey(),list);
						}					
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
	
	
	public void sendCode(String id,String canal)throws ChannelException{	
		for(Channel c:channels)
			if(c.getName().equalsIgnoreCase(canal)){
				c.send(id);
				break;
			}					
	}
	
	public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		
		LdapUser ldapUser=null;
		try {

			if (validationCode.verify(id,code)){ //security reasons
				
				//Lecture LDAP
				ldapUser=this.getLdapUser("("+ldapSchema.getLogin()+"="+ id + ")");								
				
				if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
				
				this.gestRedirectionKerberos(ldapUser,id);
																
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

		
	}
	
	
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		
		LdapUser ldapUser=null;
		try {
			
			
			if (validationCode.verify(id,code)){/*security reasons*/
				
				//Lecture LDAP
				ldapUser=this.getLdapUser("("+ldapSchema.getLogin()+"="+ id + ")");								
				
				if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
				
				this.gestRedirectionKerberos(ldapUser,newLogin);
				
				//Ajout ou modification du mot de passe dans kerberos
				kerberosAdmin.add(newLogin, currentPassword);
				logger.info("Ajout du login et du mot de passe dans kerberos effectu�e");

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

		
	}
	
	private HashMap<String,String> getLdapInfos(String id,String password,List<String>attrPersoInfo) throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
		
		HashMap<String, String> accountDescr=new HashMap<String,String>();
		
		try{
			if (bruteForceBlock.isBlocked(id)){
				throw new UserPermissionException("Nombre de tentative d'authentification atteint pour l'utilisateur "+id);
			}
			
			LdapUser ldapUser =this.getLdapUser("("+ldapSchema.getLogin()+"="+ id + ")");
			
			if (ldapUser==null) throw new AuthentificationException("Login invalide");
			if (password!=null) {
				this.writeableLdapUserService.defineAuthenticatedContextForUser(ldapUser.getId(), password);
				this.writeableLdapUserService.bindLdap(ldapUser);
			}
	
			//Construction du hasMap de retour
			
			accountDescr.put(ldapSchema.getLogin(), convertListToString(ldapUser.getAttributes(ldapSchema.getLogin())));
			accountDescr.put(ldapSchema.getMail(), convertListToString(ldapUser.getAttributes(ldapSchema.getMail())));
			
			for (int j=0;j<attrPersoInfo.size();j++){
				accountDescr.put(attrPersoInfo.get(j), convertListToString(ldapUser.getAttributes(attrPersoInfo.get(j))));
			}

			//envoi d'un code si le compte n'est pas activ�
			if (ldapUser.getAttribute(ldapSchema.getShadowLastChange())!=null){
				String code=validationCode.generateCode(ldapUser.getAttribute(ldapSchema.getLogin()));
				ArrayList<String>list=new ArrayList<String>();
				list.add(code);
				accountDescr.put(accountDescrCodeKey,convertListToString(list));
				logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(ldapSchema.getLogin())+" dans la table effectu�e");
			}					
			
		}catch(LdapException e){
			logger.debug("Exception thrown by authentificateUser() : "+ e.getMessage());
			throw new LdapProblemException("Probleme au niveau du LDAP");
		}
		catch(AuthentificationException e){
			//si authentification pas bonne 
			bruteForceBlock.setFail(id);
			throw e;
		}
		return accountDescr;
	}
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException{
		
		logger.debug("id :"+id);
		if(password==null) throw new AuthentificationException("Password must not be null !");
		return getLdapInfos(id,password,attrPersoInfo);
	}
	
	public HashMap<String,String> authentificateUserWithCas(String id,String proxyticket,String targetUrl,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
		
		logger.debug("Id, proxyticket et targetUrl : "+id +","+proxyticket+ ", "+targetUrl);
		
		if(!validationProxyTicket.validation(id, proxyticket,targetUrl))
			throw new AuthentificationException("Authentification failed ! ");
		
		return getLdapInfos(id,null,attrPersoInfo);
	}
	
	public HashMap<String,String> authentificateUserWithCodeKey(String id,String accountCodeKey,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
		
		logger.debug("Id et accountCodeKey : "+id +","+accountCodeKey);
		
		if (accountCodeKey !=null)
			if(!validationCode.verify(id, accountCodeKey))
				throw new AuthentificationException("Authentification failed ! ");
		
		return getLdapInfos(id,null,attrPersoInfo);
	}
	    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException, PrincipalNotExistsException{
    	LdapUser ldapUser=null;
    	try{
	    	if (validationCode.verify(id,code)){//security reasons

	    		//Vérifier que le login n'est pas déjà utilisé
	    		ldapUser= this.getLdapUser("("+ldapSchema.getLogin()+"="+newLogin+ ")");				
				if (ldapUser!=null) {
					throw new LdapLoginAlreadyExistsException("");
				}
	    						
				ldapUser = this.getLdapUser("("+ldapSchema.getLogin()+"="+ id + ")");
				
				if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
								
				this.gestRedirectionKerberos(ldapUser,newLogin);
				
				List<String> list=new ArrayList<String>();
				list.add(newLogin);
				ldapUser.getAttributes().put(ldapSchema.getLogin(),list);
				
				if (!kerberosAdmin.exists(id)){
					throw new PrincipalNotExistsException("");//lever exception puis lancer setpassword au niveau du FO
				}
				
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
    
    private void finalizeLdapWriting(LdapUser ldapUser)throws LdapException{
		logger.debug("L'ecriture dans le LDAP commence");
		this.writeableLdapUserService.defineAuthenticatedContext(ldapSchema.getUsernameAdmin(), ldapSchema.getPasswordAdmin());
		this.writeableLdapUserService.updateLdapUser(ldapUser);
		this.writeableLdapUserService.defineAnonymousContext();
		logger.debug("Ecriture dans le LDAP r�ussie");
	}
	
	public ValidationCode getValidationCode() {
		return validationCode;
	}

	public void setValidationCode(ValidationCode validationCode) {
		this.validationCode = validationCode;
	}
	
	public ValidationProxyTicket getValidationProxyTicket() {
		return validationProxyTicket;
	}

	public void setValidationProxyTicket(ValidationProxyTicket validationProxyTicket) {
		this.validationProxyTicket = validationProxyTicket;
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
	
	public String convertListToString(List<String>listString) {
		String result="";
		for (int i=0;i<listString.size();i++){
			if ("".equals(result)) result+=listString.get(i);
			else result+=getSeparator()+listString.get(i);
		}
		return result;
	}
	
	
	public void gestRedirectionKerberos(LdapUser ldapUser,String id)throws LdapException{
		List<String> passwds= ldapUser.getAttributes(ldapSchema.getPassword());
		
		List<String> principals= ldapUser.getAttributes(ldapSchema.getKrbPrincipal());
		
		String krbPrincipal=null;
		String ldapUserRedirectKerb=null;
		if(passwds.size()>0)
			ldapUserRedirectKerb =passwds.get(0);
		
		if(principals.size()>0)
			krbPrincipal=principals.get(0);
		
		logger.debug("ancien redirection : "+ldapUserRedirectKerb);
		
		//String ldapUserRedirectKerb = ldapUser.getAttribute(ldapSchema.getPassword());
		String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
		String newPrincipal=id+"@"+krbHost;
	
		
		if (!redirectKer.equals(ldapUserRedirectKerb) || !newPrincipal.equals(krbPrincipal)) {
			logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");

			// Writing of shadowLastChange in LDAP 
			List<String> listShadowLastChangeAttr = new ArrayList<String>();
			Calendar cal = Calendar.getInstance();
			String shadowLastChange = Integer.toString((int) Math.floor(cal.getTimeInMillis()/ (1000 * 3600 * 24)));
			listShadowLastChangeAttr.add(shadowLastChange);
			ldapUser.getAttributes().put(ldapSchema.getShadowLastChange(),listShadowLastChangeAttr);
			if (logger.isDebugEnabled()) {
				logger.debug("Writing shadowLastChange in LDAP : " + shadowLastChange);
			}
			
			//Writing of krbPrincipal in LDAP 
			if( !"".equals(ldapSchema.getKrbPrincipal()) && ldapSchema.getKrbPrincipal()!=null ) {
				List<String> listKrbPrincipalAttr = new ArrayList<String>();
				listKrbPrincipalAttr.add(newPrincipal);
				ldapUser.getAttributes().put(ldapSchema.getKrbPrincipal(),listKrbPrincipalAttr);
				if (logger.isDebugEnabled()) {
					logger.debug("Writing principal in LDAP : " + newPrincipal);
				}
			}
			
			//Writing of Kerberos redirection in LDAP 
			List<String> listPasswordAttr = new ArrayList<String>();
			listPasswordAttr.add(redirectKer);
			ldapUser.getAttributes().put(ldapSchema.getPassword(),listPasswordAttr);
			if (logger.isDebugEnabled()) {
				logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
			}
		}
	}
	
	

}
