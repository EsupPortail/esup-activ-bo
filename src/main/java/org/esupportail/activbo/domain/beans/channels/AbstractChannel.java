package org.esupportail.activbo.domain.beans.channels;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.esupportail.activbo.domain.beans.ValidationCodeImpl;
import org.esupportail.activbo.services.ldap.LdapSchema;
import org.esupportail.activbo.services.ldap.WriteableLdapUserServiceImpl;
import org.esupportail.activbo.services.ldap.LdapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChannel implements Channel {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Nom du canal
     */
    private String name;
    protected int codeDelay;
    protected ValidationCodeImpl validationCode;
    protected LdapSchema ldapSchema;
    protected WriteableLdapUserServiceImpl ldapUserService;

    public void setName(String name) { this.name=name; }
    public void setCodeDelay(int codeDelay) { this.codeDelay = codeDelay; }
    public void setValidationCode(ValidationCodeImpl validationCode) { this.validationCode = validationCode; }
    public void setLdapSchema(LdapSchema ldapSchema) { this.ldapSchema = ldapSchema; }
    public void setLdapUserService(WriteableLdapUserServiceImpl ldapUserService) { this.ldapUserService = ldapUserService; }

    
    public abstract boolean isPossible(LdapUser ldapUser);
    public abstract void send(String id) throws ChannelException;

    public String getName() {
        return name;
    }

    protected LdapUser getUser(String id, String[] wantedAttrs) throws ChannelException {
        var ldapUser = ldapUserService.getLdapUserFromFilter("("+ldapSchema.login+"="+ id + ")", wantedAttrs);
        if (ldapUser == null) throw new ChannelException("Utilisateur "+id+" inconnu");
        return ldapUser; 
    }

    protected String getUserAttr(String id, String attrName) throws ChannelException {
        var ldapUser = getUser(id, new String[] { attrName });
        return ldapUser.getAttribute(attrName);
    }

    protected static InternetAddress to_InternetAddress(String mailPerso) throws ChannelException {
        try {
            return new InternetAddress(mailPerso);
        } catch (AddressException e) {
            throw new ChannelException("Probleme de création de InternetAddress "+mailPerso);
        }
    }

}
