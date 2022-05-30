package org.esupportail.activbo.domain.beans.channels;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Set;

import org.esupportail.activbo.services.ldap.LdapUser;
import org.esupportail.smsuapi.exceptions.InsufficientQuotaException;
import org.esupportail.smsuapi.services.client.HttpRequestSmsuapiWS;
import org.esupportail.smsuapi.utils.HttpException;

/**
 * @author csar
 *
 */
public class SMSUChannel extends AbstractChannel{

    private String attributePager;
    private String messageBody;
    private HttpRequestSmsuapiWS httpRequestSmsuapiWS;

    public void setAttributePager(String attributePager) { this.attributePager = attributePager; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public void setHttpRequestSmsuapiWS(HttpRequestSmsuapiWS httpRequestSmsuapiWS) { this.httpRequestSmsuapiWS = httpRequestSmsuapiWS; }

    public Set<String> neededAttrs() {
        return Collections.singleton(attributePager);
    }

    public boolean isPossible(LdapUser ldapUser) {
        return ldapUser.getAttribute(attributePager) != null;
    }

    @Override
    public void send(String id) throws ChannelException {
        String pager = getUserAttr(id, attributePager);
        if (pager==null) throw new ChannelException("Utilisateur "+id+" n'a pas numero de portable");

        var code = validationCode.generateChannelCode(id, codeDelay, getName());
        
        String message = this.messageBody.replace("{0}", code.code);

        try {
            httpRequestSmsuapiWS.sendSms(null, pager, message);
        } catch (HttpException | InsufficientQuotaException e) {
            logger.error(e.getMessage(), e);
            throw new ChannelException("error sending SMS");
        }
        
        logger.info(id + "@" + code + ": Envoi du code par sms au numero portable "+pager);
    }
    

    public static String urlencode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("urlencode failed on '" + s + "'");
        }
    }

}
