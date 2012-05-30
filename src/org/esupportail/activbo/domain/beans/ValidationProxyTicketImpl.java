package org.esupportail.activbo.domain.beans;

import java.util.Arrays;
import java.util.List;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

import edu.yale.its.tp.cas.client.ProxyTicketValidator;

public class ValidationProxyTicketImpl implements ValidationProxyTicket{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	private String casValidateUrl;
	private String allowedProxies;
	
	private ProxyTicketValidator proxyTicketValidator;
	
	public boolean validation(String id,String proxyticket,String targetUrl) {
			
		proxyTicketValidator.setCasValidateUrl(casValidateUrl);
		proxyTicketValidator.setServiceTicket(proxyticket);
		proxyTicketValidator.setService(targetUrl);
		
		try {
			proxyTicketValidator.validate();
			logger.debug("getresponse :"+proxyTicketValidator.getResponse());
			logger.debug("getuser :"+proxyTicketValidator.getUser());
			logger.debug("service renew :"+proxyTicketValidator.isRenew());
			logger.debug("Proxyticket: "+proxyticket); 
			
			if (proxyTicketValidator.isAuthenticationSuccesful() &&
				proxyTicketValidator.getUser().equals(id) &&
				isProxyAllowed(proxyTicketValidator.getProxyList()))
				{
					logger.debug("Authentification réussie");					
					return true;
				} 									
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("Authentification ratée");
		logger.debug("isAuthenticationSuccesful: "+proxyTicketValidator.isAuthenticationSuccesful());
		return false;		
	}
	
	private boolean isProxyAllowed(List<String> proxies) {		
		List<String> allowedProxyList = Arrays.asList(allowedProxies.split(","));
		for(String p:proxies) 
			if (allowedProxyList.contains(p))
				return true;
		logger.warn("Pas de proxy autorisé à accéder au service");
		logger.warn("Proxies: "+proxies.toString());
		return false;
	}

	/**
	 * @return the casValidateUrl
	 */
	public String getCasValidateUrl() {
		return casValidateUrl;
	}


	/**
	 * @param casValidateUrl the casValidateUrl to set
	 */
	public void setCasValidateUrl(String casValidateUrl) {
		this.casValidateUrl = casValidateUrl;
	}


	/**
	 * @return the proxyTicketValidator
	 */
	public ProxyTicketValidator getProxyTicketValidator() {
		return proxyTicketValidator;
	}

	/**
	 * @param proxyTicketValidator the proxyTicketValidator to set
	 */
	public void setProxyTicketValidator(ProxyTicketValidator proxyTicketValidator) {
		this.proxyTicketValidator = proxyTicketValidator;
	}

	/**
	 * @return the allowedProxies
	 */
	public String getAllowedProxies() {
		return allowedProxies;
	}

	/**
	 * @param allowedProxies the allowedProxies to set
	 */
	public void setAllowedProxies(String allowedProxies) {
		this.allowedProxies = allowedProxies;
	}

	
}