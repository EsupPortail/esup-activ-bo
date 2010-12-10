package org.esupportail.activbo.services.remote;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import org.esupportail.activbo.domain.beans.channels.ChannelException;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;




public interface AccountManagement {
	
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws LdapProblemException,AuthentificationException, LoginException;

	public void setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException, LoginException;
	
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException;
		
	public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException, LoginException;
	
	public void sendCode(String id,String canal)throws ChannelException;
		
	public boolean validateCode(String id,String code)throws UserPermissionException;
	
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException,PrincipalNotExistsException;
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException;
}
