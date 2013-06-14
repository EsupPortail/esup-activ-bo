/**
 * 
 */
package org.esupportail.activbo.domain.beans.channels;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.esupportail.commons.services.ldap.LdapUser;

/**
 * @author csar
 *
 */
public class SMSUChannel extends AbstractChannel{

	private String attributePager;
	private String urlWS;
	private String usernameCredentials;
	private String passwordCredentials;
	private String messageBody;
	
	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.AbstractChannel#send(java.lang.String)
	 */
	@Override
	public void send(String id) throws ChannelException {
		
			this.validationCode.generateCode(id, codeDelay);
			logger.debug("Insertion code pour l'utilisateur "+id+" dans la table effectuée");
			
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+ldapSchema.getLogin()+"="+ id + ")");
						if (ldapUserList.size() == 0) throw new ChannelException("Utilisateur "+id+" inconnu");
	
			LdapUser ldapUserRead = ldapUserList.get(0); 
			
			String pager = ldapUserRead.getAttribute(attributePager);
			
			if(pager==null) throw new ChannelException("Utilisateur "+id+" n'a pas numéro de portable");
									
			
			String message=this.messageBody;
			message=message.replace("{0}", validationCode.getCode(id));
			Map<String, String> map = new HashMap<String,String>();
			HttpClient client = new HttpClient();
		    client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
		    								 new UsernamePasswordCredentials(this.usernameCredentials, this.passwordCredentials));

	        map.put("action", "SendSms");
	        map.put("phoneNumber", pager);
	        map.put("message", message);
		    String cooked_url = cook_url(this.urlWS, map);
		    try {
				requestGET(client, cooked_url);
			} catch (IOException e) {logger.error(e.getMessage(),e);
			}
			
			
			
			logger.debug("Envoi du code par sms au numéro portable "+pager);										
	}
	

	public static String urlencode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("urlencode failed on '" + s + "'");
        }
    }

    private static String cook_url(String url, Map<String, String> params) {
        String s = null;
        for (Entry<String, String> e : params.entrySet()) {
            s = (s == null ? "?" : s + "&") + e.getKey() + "=" + urlencode(e.getValue());
        }
        return url + s;
    } 
    
    // Appel au service     
	private String requestGET(HttpClient client, String request) throws IOException {
        logger.debug("requesting url " + request);

        GetMethod method = new GetMethod(request);

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("GET failed with status " + method.getStatusLine());
            }

            // Read the response body.
            String resp = method.getResponseBodyAsString();

            logger.debug(resp);
            return resp;
       
        } catch (HttpException e) {
            throw new IOException("Fatal protocol violation: " + e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    } 
	
	
	public String getUrlWS() {
		return urlWS;
	}


	public void setUrlWS(String urlWS) {
		this.urlWS = urlWS;
	}

	public String getUsernameCredentials() {
		return usernameCredentials;
	}


	public void setUsernameCredentials(String usernameCredentials) {
		this.usernameCredentials = usernameCredentials;
	}


	public String getPasswordCredentials() {
		return passwordCredentials;
	}


	public void setPasswordCredentials(String passwordCredentials) {
		this.passwordCredentials = passwordCredentials;
	}


	/**
	 * @param messageBody the messageBody to set
	 */
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	/**
	 * @param attributePager the attributePager to set
	 */
	public void setAttributePager(String attributePager) {
		this.attributePager = attributePager;
	}
	
	public boolean isPossible(LdapUser ldapUser){
		
		String pager = ldapUser.getAttribute(attributePager);
		
		if(pager==null) return false;
		
		return true;
		
	}

}
