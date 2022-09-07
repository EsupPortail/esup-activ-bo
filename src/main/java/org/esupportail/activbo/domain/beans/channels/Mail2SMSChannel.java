package org.esupportail.activbo.domain.beans.channels;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.esupportail.activbo.services.SmtpService;
import org.esupportail.activbo.services.ldap.LdapUser;

public class Mail2SMSChannel extends AbstractChannel {
    @Inject private SmtpService smtpService;
    private String attributePager;
    private String mailSMS;
    private String mailCodeSubject;
    private String mailCodeBody;

    public void setAttributePager(String attributePager) { this.attributePager = attributePager; }
    public void setMailSMS(String mailSMS) { this.mailSMS = mailSMS; }  
    public void setMailCodeSubject(String mailCodeSubject) { this.mailCodeSubject = mailCodeSubject; }
    public void setMailCodeBody(String mailCodeBody) { this.mailCodeBody = mailCodeBody; }

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
                                
        var mail = to_InternetAddress(mailSMS);
        
        var code = validationCode.generateChannelCode(id, codeDelay, getName());
        String mailBody=mailCodeBody
            .replace("{0}", pager)
            .replace("{1}", code.code)
            .replace("{2}", code.date);
        
        smtpService.sendEmail(mail, mailCodeSubject, mailBody, false);
        
        logger.info(id + "@" + code.code + ": Envoi du code par sms via mail2sms au numero portable "+pager);
    }
    
}
