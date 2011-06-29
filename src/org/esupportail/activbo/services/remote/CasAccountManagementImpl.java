package org.esupportail.activbo.services.remote;

import java.util.HashMap;
import java.util.List;

import org.esupportail.activbo.domain.DomainService;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.UserPermissionException;

import org.springframework.beans.factory.InitializingBean;


public class CasAccountManagementImpl implements CasAccountManagement,InitializingBean{
	
	private DomainService domainService;
	
	public CasAccountManagementImpl() {
		super();
	}
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService; 
	}
	
	public HashMap<String,String> authentificateUserWithCas(String id,String proxyticket,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException{
		return domainService.authentificateUserWithCas(id, proxyticket,attrPersoInfo);
	}
	
	public HashMap<String,String> authentificateUserWithCodeKey(String id,String accountCodeKey,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException{
		return domainService.authentificateUserWithCodeKey(id, accountCodeKey,attrPersoInfo);
	}
	
}
