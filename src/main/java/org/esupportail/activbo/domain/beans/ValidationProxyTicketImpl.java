package org.esupportail.activbo.domain.beans;

import java.util.Arrays;
import java.util.List;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

import edu.yale.its.tp.cas.client.ProxyTicketValidator;

public class ValidationProxyTicketImpl implements ValidationProxyTicket{
    private final Logger logger = new LoggerImpl(getClass());
    
    private String casValidateUrl;
    private String allowedProxies;
    private ProxyTicketValidator proxyTicketValidator;

    public void setCasValidateUrl(String casValidateUrl) { this.casValidateUrl = casValidateUrl; }
    public void setProxyTicketValidator(ProxyTicketValidator proxyTicketValidator) { this.proxyTicketValidator = proxyTicketValidator; }
    public void setAllowedProxies(String allowedProxies) { this.allowedProxies = allowedProxies; }


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
                isProxyAllowed(proxyTicketValidator.getProxyList())) {
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
    
    private boolean isProxyAllowed(List<?> proxies) {       
        var allowedProxyList = Arrays.asList(allowedProxies.split(","));
        for (var p : proxies) 
            if (allowedProxyList.contains(p))
                return true;        
        logger.warn("Les proxies ci-après ne sont pas authorisés à accéder au BO : "+proxies.toString());
        logger.warn("Vous pouvez les ajouter sur properties/config.properties, cas.allowedProxies");
        logger.warn("Les proxies actuellement autorisés : "+allowedProxyList.toString());
        return false;
    }

}