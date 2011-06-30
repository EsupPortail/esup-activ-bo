package org.esupportail.activbo.domain.beans;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

import edu.yale.its.tp.cas.client.ServiceTicketValidator;

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
	
	private String casTargetUrl;
	
	private ServiceTicketValidator serviceTicketValidator;
	
	public boolean validation(String id,String proxyticket,String targetUrl) {
		
		boolean returnvalue=false;
		
		serviceTicketValidator.setCasValidateUrl(casValidateUrl);
		serviceTicketValidator.setServiceTicket(proxyticket);
		serviceTicketValidator.setService(targetUrl);
		
			try {
				serviceTicketValidator.validate();
				logger.debug("getresponse :"+serviceTicketValidator.getResponse());
				logger.debug("getuser :"+serviceTicketValidator.getUser());
				logger.debug("service renew :"+serviceTicketValidator.isRenew());
				
				if (!serviceTicketValidator.isAuthenticationSuccesful()) {
					logger.debug("Proxyticket "+proxyticket + " Authentification ratée");
					returnvalue=false;
				} else {
					logger.debug("Proxyticket "+proxyticket+" Authentification réussie");
					logger.debug("Authentification réussie");
					if (serviceTicketValidator.getUser().equals(id)) returnvalue=true;
					else returnvalue=false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return returnvalue;
	}

	/**
	 * @return the serviceTicketValidator
	 */
	public ServiceTicketValidator getServiceTicketValidator() {
		return serviceTicketValidator;
	}


	/**
	 * @param serviceTicketValidator the serviceTicketValidator to set
	 */
	public void setServiceTicketValidator(
			ServiceTicketValidator serviceTicketValidator) {
		this.serviceTicketValidator = serviceTicketValidator;
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
	 * @return the casTargetUrl
	 */
	public String getCasTargetUrl() {
		return casTargetUrl;
	}


	/**
	 * @param casTargetUrl the casTargetUrl to set
	 */
	public void setCasTargetUrl(String casTargetUrl) {
		this.casTargetUrl = casTargetUrl;
	}
	
}