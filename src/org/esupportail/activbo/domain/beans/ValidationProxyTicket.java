package org.esupportail.activbo.domain.beans;

public interface ValidationProxyTicket{
	
    public boolean validation(String id, String proxyticket, String targetUrl);
    
    public boolean isConfirmedTargetUrl(String targetUrl);
	
}
