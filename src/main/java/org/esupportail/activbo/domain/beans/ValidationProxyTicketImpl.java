package org.esupportail.activbo.domain.beans;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.its.tp.cas.client.ProxyTicketValidator;

public class ValidationProxyTicketImpl implements ValidationProxyTicket{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Inject private ProxyTicketValidator proxyTicketValidator;
    private String casValidateUrl;
    private String allowedProxies;

    public void setCasValidateUrl(String casValidateUrl) { this.casValidateUrl = casValidateUrl; }
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
                    logger.debug("Authentification reussie");                   
                    return true;
                }                                   
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Authentification ratee");
        logger.debug("isAuthenticationSuccesful: "+proxyTicketValidator.isAuthenticationSuccesful());
        return false;       
    }
    
    private boolean isProxyAllowed(List<?> proxies) {       
        var allowedProxyList = Arrays.asList(allowedProxies.split(","));
        for (var p : proxies) 
            if (allowedProxyList.contains(p))
                return true;        
        logger.warn("Les proxies ci-apres ne sont pas authorises a acceder au BO : "+proxies.toString());
        logger.warn("Vous pouvez les ajouter sur properties/config.properties, cas.allowedProxies");
        logger.warn("Les proxies actuellement autorises : "+allowedProxyList.toString());
        return false;
    }

}