package org.esupportail.activbo.services.remote;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.commons.services.ldap.LdapException;



public interface AccountManagement {
		
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws LdapProblemException;

	public boolean setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException;
		
	public boolean setPassword(String id,String code,String oldPassword,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException;
	
	public boolean updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException;
	
	public String getCode(String id,String canal);
	
	public String getCode(String id);
	
	public boolean validateCode(String id,String code);
	
	//public void updateDisplayName(String displayName,String id,String code);


	//public void setMailPerso(String id,String mailPerso);
	
	/*public int validateCode(String id,String code);*/
	
	/*verification du code mentionné par l'utilisateur*/
	/*public boolean verifyCode(String id,String code);
	
	public void updateMailPerso(String mailPerso);
	
	public void updatePhoneNumber(String phoneNumber);
	
	public putPassword(String password,String id,String newPassword);
	
	*
	*/
}
