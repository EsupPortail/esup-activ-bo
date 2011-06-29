/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;



import org.esupportail.activbo.domain.beans.User;
import org.esupportail.activbo.domain.beans.channels.ChannelException;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.commons.exceptions.ConfigException;
import org.esupportail.commons.exceptions.UserNotFoundException;
import org.esupportail.commons.services.application.Version;
import org.esupportail.commons.web.beans.Paginator;

/**
 * The domain service interface.
 */
public interface DomainService extends Serializable {

	public final static int TIMEOUT=0,
	BADCODE=1,
	GOOD=2;
	
	
	//////////////////////////////////////////////////////////////
	// User
	//////////////////////////////////////////////////////////////

	/**
	 * @param id
	 * @return the User instance that corresponds to an id.
	 * @throws UserNotFoundException
	 */
	User getUser(String id) throws UserNotFoundException;

	/**
	 * @return the list of all the users.
	 */
	List<User> getUsers();

	/**
	 * Update a user.
	 * @param user
	 */
	void updateUser(User user);

	/**
	 * Update a user's information (retrieved from the LDAP directory for instance).
	 * @param user
	 */
	void updateUserInfo(User user);
	
	/**
	 * Add an administrator.
	 * @param user
	 */
	void addAdmin(User user);

	/**
	 * Delete an administrator.
	 * @param user
	 */
	void deleteAdmin(User user);

	/**
	 * @return a paginator for administrators.
	 */
	Paginator<User> getAdminPaginator();

	//////////////////////////////////////////////////////////////
	// VersionManager
	//////////////////////////////////////////////////////////////
	
	/**
	 * @return the database version.
	 * @throws ConfigException when the database is not initialized
	 */
	Version getDatabaseVersion() throws ConfigException;
	
	/**
	 * Set the database version.
	 * @param version 
	 */
	void setDatabaseVersion(Version version);
	
	/**
	 * Set the database version.
	 * @param version 
	 */
	void setDatabaseVersion(String version);
	
	//////////////////////////////////////////////////////////////
	// Authorizations
	//////////////////////////////////////////////////////////////

	/**
	 * @param currentUser
	 * @return 'true' if the user can view administrators.
	 */
	boolean userCanViewAdmins(User currentUser);
	
	/**
	 * @param user 
	 * @return 'true' if the user can grant the privileges of administrator.
	 */
	boolean userCanAddAdmin(User user);

	/**
	 * @param user 
	 * @param admin
	 * @return 'true' if the user can revoke the privileges of an administrator.
	 */
	boolean userCanDeleteAdmin(User user, User admin);
	
		
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws AuthentificationException,LdapProblemException,LoginException;
	
	public void setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException,LoginException;
	
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException;
	
	public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo) throws LdapProblemException,UserPermissionException,LoginException;
	
	public void sendCode(String id,String canal)throws ChannelException;
	
	public boolean validateCode(String id,String code)throws UserPermissionException;
	
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException,LoginException,PrincipalNotExistsException;
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;
	
	public HashMap<String,String> authentificateUserWithCas(String id,String proxyticket,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;

	public HashMap<String,String> authentificateUserWithCodeKey(String id,String accountCodeKey,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;

}
