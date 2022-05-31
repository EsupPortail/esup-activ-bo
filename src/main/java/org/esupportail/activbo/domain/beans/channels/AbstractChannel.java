package org.esupportail.activbo.domain.beans.channels;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import javax.inject.Inject;

import org.esupportail.activbo.domain.beans.ValidationCodeImpl;
import org.esupportail.activbo.services.ldap.LdapSchema;
import org.esupportail.activbo.services.ldap.WriteableLdapUserServiceImpl;
import org.esupportail.activbo.services.ldap.LdapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChannel implements Channel {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Inject protected ValidationCodeImpl validationCode;
    @Inject protected LdapSchema ldapSchema;
    @Inject protected WriteableLdapUserServiceImpl ldapUserService;
    /**
     * Nom du canal
     */
    private String name;
    protected int codeDelay;

    public void setName(String name) { this.name=name; }
    public void setCodeDelay(int codeDelay) { this.codeDelay = codeDelay; }

    
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
            throw new ChannelException("Probleme de creation de InternetAddress "+mailPerso);
        }
    }

}
