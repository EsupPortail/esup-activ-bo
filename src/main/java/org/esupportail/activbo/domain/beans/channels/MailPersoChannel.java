package org.esupportail.activbo.domain.beans.channels;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.esupportail.activbo.services.SmtpService;
import org.esupportail.activbo.services.ldap.LdapUser;

public class MailPersoChannel extends AbstractChannel{
    @Inject private SmtpService smtpService;
    private String attributeMailPerso;
    private String attributeDisplayName;
    private String mailCodeSubject;
    private String mailCodeBody;

    public void setAttributeMailPerso(String attributeMailPerso) { this.attributeMailPerso = attributeMailPerso; }
    public void setAttributeDisplayName(String attributeDisplayName) { this.attributeDisplayName = attributeDisplayName; }
    public void setMailCodeSubject(String mailCodeSubject) { this.mailCodeSubject = mailCodeSubject; }
    public void setMailCodeBody(String mailCodeBody) { this.mailCodeBody = mailCodeBody; }

    public Set<String> neededAttrs() {
        return Collections.singleton(attributeMailPerso);
    }

    @Override
    public boolean isPossible(LdapUser ldapUser) {
        return ldapUser.getAttribute(attributeMailPerso) != null;
    }

    @Override
    public void send(String id) throws ChannelException {
        var ldapUser = getUser(id, new String[] { attributeDisplayName, attributeMailPerso });
        String displayName = ldapUser.getAttribute(attributeDisplayName);
        String mailPerso = ldapUser.getAttribute(attributeMailPerso);
        if (mailPerso==null) throw new ChannelException("Utilisateur "+id+" n'a pas de mail perso");                                    
        var mail = to_InternetAddress(mailPerso);
        
        var code = this.validationCode.generateChannelCode(id, codeDelay, getName());

        String subject = this.mailCodeSubject.replace("{0}", displayName);
        String mailBody=this.mailCodeBody.replace("{0}", displayName)
            .replace("{1}", code.code)
            .replace("{2}", code.date);
        smtpService.sendEmail(mail, subject, mailBody, true);
        
        logger.info(id + "@" + code + ": Envoi du code a l'adresse mail perso "+mailPerso);
    }

}
