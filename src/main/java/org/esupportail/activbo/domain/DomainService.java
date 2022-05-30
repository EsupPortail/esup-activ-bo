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
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.commons.exceptions.UserNotFoundException;

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
        
    public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws AuthentificationException,LdapProblemException,LoginException;
    
    public void setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException,LoginException;
    
    public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException;
    
    public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo) throws LdapProblemException,UserPermissionException,LoginException;
    
    public void removeCode(String user_id, String code);

    public void sendCode(String id,String canal)throws ChannelException;
    
    public boolean validateCode(String id,String code)throws UserPermissionException;
    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException,LoginException,PrincipalNotExistsException;
    
    public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;
    
    public HashMap<String,String> authentificateUserWithCas(String id,String proxyticket,String targetUrl,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;

    public HashMap<String,String> authentificateUserWithCodeKey(String id,String accountCodeKey,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;
    public String validatePassword(String supannAliasLogin, String password) throws  KRBException,LdapProblemException, LoginException;

}
