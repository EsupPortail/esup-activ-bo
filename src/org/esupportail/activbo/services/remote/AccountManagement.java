package org.esupportail.activbo.services.remote;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.commons.services.ldap.LdapException;



public interface AccountManagement {
		
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws LdapProblemException;

	public boolean setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException;
		
	public boolean updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException;
	
	public boolean getCode(String id,String canal)throws LdapProblemException;
		
	public boolean validateCode(String id,String code);
	
	public boolean changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException;
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException;
}
