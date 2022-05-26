package org.esupportail.activbo.domain.beans.channels;

import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.smtp.AsynchronousSmtpServiceImpl;

public class MailPersoChannel extends AbstractChannel{
    private String attributeMailPerso;
    private String attributeDisplayName;
    private String mailCodeSubject;
    private String mailCodeBody;
    private AsynchronousSmtpServiceImpl smtpService;

    public void setAttributeMailPerso(String attributeMailPerso) { this.attributeMailPerso = attributeMailPerso; }
    public void setAttributeDisplayName(String attributeDisplayName) { this.attributeDisplayName = attributeDisplayName; }
    public void setMailCodeSubject(String mailCodeSubject) { this.mailCodeSubject = mailCodeSubject; }
    public void setMailCodeBody(String mailCodeBody) { this.mailCodeBody = mailCodeBody; }
    public void setSmtpService(AsynchronousSmtpServiceImpl smtpService) { this.smtpService = smtpService; }

    @Override
    public boolean isPossible(LdapUser ldapUser) {
        return ldapUser.getAttribute(attributeMailPerso) != null;
    }

    @Override
    public void send(String id) throws ChannelException {
        LdapUser ldapUserRead = getUser(id);
        String displayName = ldapUserRead.getAttribute(attributeDisplayName);
        String mailPerso = ldapUserRead.getAttribute(attributeMailPerso);
        if (mailPerso==null) throw new ChannelException("Utilisateur "+id+" n'a pas de mail perso");                                    
        var mail = to_InternetAddress(mailPerso);
        
        var code = this.validationCode.generateChannelCode(id, codeDelay, getName());

        String subject = this.mailCodeSubject.replace("{0}", displayName);
        String mailBody=this.mailCodeBody.replace("{0}", displayName)
            .replace("{1}", code.code)
            .replace("{2}", code.date);
        smtpService.send(mail,subject,mailBody,"");
        
        logger.info(id + "@" + code + ": Envoi du code à l'adresse mail perso "+mailPerso);
    }

}
