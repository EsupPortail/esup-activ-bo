package org.esupportail.activbo.services.remote;
import java.util.Date;

import org.esupportail.activbo.domain.DomainService;
import org.esupportail.commons.services.ldap.LdapException;
import org.springframework.beans.factory.InitializingBean;


public class InformationImpl implements Information,InitializingBean{
	
	private DomainService domainService;
	
	public InformationImpl() {
		super();
	}

	public boolean validAccount(String number,String birthName,Date birthDate)throws LdapException{
		return domainService.validateAccount(number,birthName,birthDate);
	}
	
	
	public boolean updateLdap(final String currentPassword)throws LdapException{
		return domainService.updateLdapAttributes(currentPassword);
		
	}
	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}



}
