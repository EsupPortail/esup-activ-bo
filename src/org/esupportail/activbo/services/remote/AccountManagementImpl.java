package org.esupportail.activbo.services.remote;

import java.util.HashMap;
import java.util.List;

import org.esupportail.activbo.domain.DomainService;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.UserPermissionException;

import org.springframework.beans.factory.InitializingBean;


public class AccountManagementImpl implements AccountManagement,InitializingBean{
	
	private DomainService domainService;
	
	public AccountManagementImpl() {
		super();
	}
	
	
	public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws LdapProblemException,AuthentificationException, LoginException{
		return domainService.validateAccount(hashInfToValidate,attrPersoInfo);
	}
	
	
	public void setPassword(String id,String code,final String currentPassword)throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		domainService.setPassword(id,code,currentPassword);
		
	}
	
	/*public HashMap<String,String> setPassword(String id,String oldPassword,final String currentPassword,List<String>attrPersoInfo)throws LdapProblemException,UserPermissionException,KerberosException,OldPasswordException{
		return domainService.setPassword(id,oldPassword,currentPassword,attrPersoInfo);
	}*/
	
	public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException, LoginException{
		domainService.updatePersonalInformations(id,code,hashBeanPersoInfo);
	}
	
	public boolean getCode(String id,String canal)throws LdapProblemException{
		return domainService.getCode(id, canal);
	}
	
	
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}
	
	public boolean validateCode(String id,String code)throws UserPermissionException{
		return domainService.validateCode(id, code);
	}
	
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException{
		domainService.changeLogin(id, code, newLogin);
	}
	
	public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException{
		return domainService.authentificateUser(id, password,attrPersoInfo);
	}

}
