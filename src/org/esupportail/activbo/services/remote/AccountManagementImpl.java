package org.esupportail.activbo.services.remote;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.esupportail.activbo.domain.DomainService;
import org.esupportail.commons.services.ldap.LdapException;
import org.springframework.beans.factory.InitializingBean;


public class AccountManagementImpl implements AccountManagement,InitializingBean{
	
	private DomainService domainService;
	
	public AccountManagementImpl() {
		super();
	}

	public HashMap<String,String> validateAccount(String number,String birthName,Date birthDate,List<String>attrPersoInfo) throws LdapException{
		return domainService.validateAccount(number,birthName,birthDate,attrPersoInfo);
	}
	
	
	public boolean setPassword(String id,String code,final String currentPassword)throws LdapException{
		return domainService.setPassword(id,code,currentPassword);
		
	}
	
	public boolean updateInfoPerso(HashMap<String,String> infoPerso){
		return domainService.updateInfoPerso(infoPerso);
	}
	
	/*public void updateDisplayName(String displayName,String id, String code){
		domainService.updateDisplayName(displayName,id,code);
	}*/
	
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}
	
	/*public void setMailPerso(String id,String mailPerso){
		domainService.setMailPerso(id,mailPerso);
	}*/
	
	/*public int validateCode(String id,String code){
		return domainService.validateCode(id, code);
	}*/
	
	/*
	
	public boolean verifyCode(String id,String code){
		return true;
	}
	
	public void setMailPerso(String mailPerso){
		System.out.println("Modification du mail perso");
	}
	
	public void setPhoneNumber(String phoneNumber){
		System.out.println("Modification du numero de telephone");
	}
	
	public putPassword(String password,String id,String newPassword){
	
	}
	
	
	*
	*
	*
	*
	*
	*/



}
