package org.esupportail.activbo.domain.beans.channels;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.esupportail.activbo.services.ldap.LdapUser;

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

    public void setAttributePager(String attributePager) { this.attributePager = attributePager; }
    public void setUrlWS(String urlWS) { this.urlWS = urlWS; }
    public void setUsernameCredentials(String usernameCredentials) { this.usernameCredentials = usernameCredentials; }
    public void setPasswordCredentials(String passwordCredentials) { this.passwordCredentials = passwordCredentials; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }


    public boolean isPossible(LdapUser ldapUser) {
        return ldapUser.getAttribute(attributePager) != null;
    }

    @Override
    public void send(String id) throws ChannelException {
        String pager = getUserAttr(id, attributePager);
        if (pager==null) throw new ChannelException("Utilisateur "+id+" n'a pas numéro de portable");

        var code = validationCode.generateChannelCode(id, codeDelay, getName());
        
        String message = this.messageBody.replace("{0}", code.code);

        var map = new HashMap<String,String>();
        map.put("action", "SendSms");
        map.put("phoneNumber", pager);
        map.put("message", message);
        String cooked_url = cook_url(this.urlWS, map);
        
        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
            new UsernamePasswordCredentials(this.usernameCredentials, this.passwordCredentials));

        try {
            requestGET(client, cooked_url);
        } catch (IOException e) { logger.error(e.getMessage(), e); }
        
        logger.info(id + "@" + code + ": Envoi du code par sms au numéro portable "+pager);
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
        for (var e : params.entrySet()) {
            s = (s == null ? "?" : s + "&") + e.getKey() + "=" + urlencode(e.getValue());
        }
        return url + s;
    } 
    
    // Appel au service     
    private String requestGET(HttpClient client, String request) throws IOException {
        logger.debug("requesting url " + request);

        var method = new GetMethod(request);

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
        
}
