package org.esupportail.activbo.domain.beans.channels;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.esupportail.activbo.services.ldap.LdapUser;
import org.esupportail.activbo.services.SmtpService;

public class Mail2GestChannel extends AbstractChannel{
    @Inject private SmtpService smtpService;
    private String mailGest;
    private String mailCodeSubject;
    private String mailCodeBody;
    private String attributeDisplayName;
    private Map<String,List<String>> access;
    private Map<String,List<String>> deny;

    public void setMailCodeSubject(String mailCodeSubject) { this.mailCodeSubject = mailCodeSubject; }
    public void setMailCodeBody(String mailCodeBody) { this.mailCodeBody = mailCodeBody; }
    public void setMailGest(String mailGest) { this.mailGest = mailGest; }      
    public void setAttributeDisplayName(String attributeDisplayName) { this.attributeDisplayName = attributeDisplayName; }
    public void setAccess(Map<String, List<String>> access) { this.access = access; }
    public void setDeny(Map<String, List<String>> deny) { this.deny = deny; }

    @Override
    public void send(String id) throws ChannelException {
        String displayName = getUserAttr(id, attributeDisplayName);
        var mail = to_InternetAddress(mailGest);
        var code = validationCode.generateChannelCode(id, codeDelay, getName());

        String newSubject = mailCodeSubject.replace("{0}", displayName);
        String mailBody=mailCodeBody
            .replace("{0}", id)
            .replace("{1}", code.code)
            .replace("{2}", validationCode.dateToString(code.date))
            .replace("{3}", displayName);
        
        smtpService.sendEmail(mail, newSubject, mailBody, true);
        logger.info(id + "@" + code.code + ": Envoi du code a l'adresse mail gestionnaire "+mailGest);
    }

    public boolean isPossible(LdapUser ldapUser) {
        if (access==null && deny==null) return true; //si pas de definition de droit d'acces, le canal est disponible pour tout profil
        
        if (deny!=null && profileMatches(deny,ldapUser))                
            return false;                                                                                   
                    
        if (access!=null && profileMatches(access,ldapUser))
            return true;
        
        if (deny!=null) return true; 
        else return false;
    }
    
    private boolean profileMatches(Map<String,List<String>> profile, LdapUser ldapUser) {
        for (String attribute : profile.keySet()) {
            var values=profile.get(attribute);
            for (String userValue : ldapUser.getAttributeValues(attribute))
                if (values.contains(userValue))     
                    return true;                                                                                    
        }
        return false;
    }
    @Override
    public Set<String> neededAttrs() {
        var r = new HashSet<String>();
        if (access != null) r.addAll(access.keySet());
        if (deny != null) r.addAll(deny.keySet());
        return r;
    }

}
