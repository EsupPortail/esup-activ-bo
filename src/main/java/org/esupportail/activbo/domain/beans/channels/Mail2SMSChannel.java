package org.esupportail.activbo.domain.beans.channels;

import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.smtp.AsynchronousSmtpServiceImpl;

public class Mail2SMSChannel extends AbstractChannel {
    private String attributePager;
    private String mailSMS;
    private String mailCodeSubject;
    private String mailCodeBody;
    private AsynchronousSmtpServiceImpl smtpService;

    public void setAttributePager(String attributePager) { this.attributePager = attributePager; }
    public void setMailSMS(String mailSMS) { this.mailSMS = mailSMS; }  
    public void setMailCodeSubject(String mailCodeSubject) { this.mailCodeSubject = mailCodeSubject; }
    public void setMailCodeBody(String mailCodeBody) { this.mailCodeBody = mailCodeBody; }
    public void setSmtpService(AsynchronousSmtpServiceImpl smtpService) { this.smtpService = smtpService; }

    public boolean isPossible(LdapUser ldapUser) {
        return ldapUser.getAttribute(attributePager) != null;
    }

    @Override
    public void send(String id) throws ChannelException {
        String pager = getUser(id).getAttribute(attributePager);            
        if (pager==null) throw new ChannelException("Utilisateur "+id+" n'a pas numéro de portable");
                                
        var mail = to_InternetAddress(mailSMS);
        
        var code = validationCode.generateChannelCode(id, codeDelay, getName());
        String mailBody=mailCodeBody
            .replace("{0}", pager)
            .replace("{1}", code.code)
            .replace("{2}", code.date);
        
        smtpService.send(mail,mailCodeSubject,"",mailBody);
        
        logger.info(id + "@" + code + ": Envoi du code par sms via mail2sms au numéro portable "+pager);
    }
    
}
