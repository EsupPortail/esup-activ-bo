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
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.activbo.services.kerberos.KRBIllegalArgumentException;
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
public abstract class DomainServiceImpl implements DomainService, InitializingBean {

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

	private String casID;
	
	/**
	 * @return the casID
	 */
	public String getCasID() {
		return casID;
	}

	/**
	 * @param casID the casID to set
	 */
	public void setCasID(String casID) {
		this.casID = casID;
	}

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
	
	public LdapUser getLdapUser(String id,String code) throws UserPermissionException,LdapProblemException,LoginException{
		if (!validationCode.verify(id,code)) throw new UserPermissionException("Code invalide L'utilisateur id="+id+" n'a pas le droit de continuer la procédure");
		
		LdapUser ldapUser = this.getLdapUser("("+ldapSchema.getLogin()+"="+ id + ")");	
		if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
		ldapUser.getAttributes().clear(); 
		return ldapUser;
	}

	
	public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException, LoginException{
		
		try{
				this.writeableLdapUserService.invalidateLdapCache();
				//Lecture LDAP				
				LdapUser ldapUser=this.getLdapUser(id,code);
												
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

			//envoi d'un code si le compte est activé
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
		
		
		
		LdapUser ldapUser =this.getLdapUser("("+casID+"="+ id + ")");
        String login=id;
        if(ldapUser!=null)
            login=ldapUser.getAttribute(ldapSchema.getLogin()); 
        
        return getLdapInfos(login,null,attrPersoInfo);
	}
	
	public HashMap<String,String> authentificateUserWithCodeKey(String id,String accountCodeKey,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
		
		logger.debug("Id et accountCodeKey : "+id +","+accountCodeKey);
		
		if(!validationCode.verify(id, accountCodeKey))
			throw new AuthentificationException("Authentification failed ! ");
		
		return getLdapInfos(id,null,attrPersoInfo);
	}

	public boolean validateCode(String id,String code)throws UserPermissionException {

    	if (validationCode.verify(id,code)){
			return true;
		}
		return false;
	}
    
    protected void finalizeLdapWriting(LdapUser ldapUser)throws LdapException{
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
	
	protected void listShadowLastChangeAttr(LdapUser ldapUser){
		// Ecrire l'attribut shadowLastChange dans LDAP
		List<String> listShadowLastChangeAttr = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		String shadowLastChange = Integer.toString((int) Math.floor(cal.getTimeInMillis() / (1000 * 3600 * 24)));
		listShadowLastChangeAttr.add(shadowLastChange);
		ldapUser.getAttributes().put(getLdapSchema().getShadowLastChange(),listShadowLastChangeAttr);
		if (logger.isDebugEnabled()) {logger.debug("Writing shadowLastChange in LDAP : "+ shadowLastChange );}				
	}
	
	/**
	 * But : Gestion des exceptions
	*        
	 */
	
	public void exceptions(Exception exception) throws LdapProblemException,KerberosException, LoginException, UserPermissionException{
		logger.debug("Dans m�thode exceptions",exception);
		if 		(exception instanceof LdapException)throw new LdapProblemException("Probleme au niveau du LDAP");
		else if (exception instanceof KRBException) throw new KerberosException("Probleme au niveau de Kerberos");
		else if (exception instanceof KRBIllegalArgumentException) throw new KerberosException("Probleme au niveau de Kerberos");
		else if (exception instanceof UserPermissionException) throw (UserPermissionException)exception;
		else if (exception instanceof RuntimeException) throw (RuntimeException)(exception);
		else logger.error("Erreur inattendue");
		
	}
	
	

}
