package org.esupportail.activbo.domain.beans;


import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.xml.sax.SAXException;

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
	
	
	public boolean validation(String id,String proxyticket) {
		
		boolean returnvalue=false;
		
		ServiceTicketValidator sv = new ServiceTicketValidator();
		
		sv.setCasValidateUrl("https://cas-test.univ-paris1.fr/cas/proxyValidate");
		sv.setServiceTicket(proxyticket);
		sv.setService("https://busan-desktop.univ-paris1.fr:7080");
		
		
			try {
				sv.validate();
				logger.debug("getresponse :"+sv.getResponse());
				logger.debug("getuser :"+sv.getUser());
				logger.debug("service renew :"+sv.isRenew());
				
				if (!sv.isAuthenticationSuccesful()) {
					logger.debug("Proxyticket is "+proxyticket);
					logger.debug("Authentification ratée");
					returnvalue=false;
				} else {
					logger.debug("Proxyticket is "+proxyticket);
					logger.debug("Authentification réussie");
					if (sv.getUser().equals(id)) returnvalue=true;
					else returnvalue=false;
					logger.debug("returnvalue1 is "+returnvalue+"svgetuser,id "+sv.getUser()+","+id);
				}
			} catch (Exception e) {
				returnvalue=false;
				logger.debug("returnvalue2 is "+returnvalue);
			}
			logger.debug("returnvalue3 is "+returnvalue);
			return returnvalue;
			
		

	}
	
}