package org.esupportail.activbo.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.esupportail.activbo.domain.beans.channels.ChannelException;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBException;


public interface DomainService extends Serializable {

    public Map<String,List<String>> validateAccount(Map<String,String> hashInfToValidate, Set<String>attrPersoInfo) throws AuthentificationException,LdapProblemException,LoginException;
    
    public void setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException,LoginException;
    
    public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException;
    
    public void updatePersonalInformations(String id,String code,Map<String,List<? extends Object>> hashBeanPersoInfo) throws LdapProblemException,UserPermissionException,LoginException;
    
    public void removeCode(String user_id, String code);

    public void sendCode(String id,String channel)throws ChannelException;
    
    public void verifyCode(String id,String code)throws UserPermissionException;
    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginException,PrincipalNotExistsException;
    
    public Map<String,List<String>> authentificateUser(String id,String password,Set<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;
    
    public Map<String,List<String>> authentificateUserWithCas(String id,String proxyticket,String targetUrl,Set<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;

    public Map<String,List<String>> authentificateUserWithCodeKey(String id,String accountCodeKey,Set<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException,LoginException;

    public String validatePassword(String supannAliasLogin, String password) throws KRBException;

}
