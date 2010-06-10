package org.esupportail.activbo.services.remote;
import org.esupportail.activbo.domain.DomainService;
import org.springframework.beans.factory.InitializingBean;


public class InformationImpl implements Information,InitializingBean{
	
	private DomainService domainService;
	
	public InformationImpl() {
		super();
	}
	
	public String obtenirChaine(){
		System.out.println("===============");
		return "vbdlfjdjfsl";
	}

	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}



}
