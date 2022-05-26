package org.esupportail.activbo.domain.beans.channels;

import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.activbo.services.ldap.LdapSchema;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.ldap.LdapUserService;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public abstract class AbstractChannel implements Channel {
    protected final Logger logger = new LoggerImpl(getClass());
    
    /**
     * Nom du canal
     */
    private String name;
    protected int codeDelay;
    protected ValidationCode validationCode;
    protected LdapSchema ldapSchema;
    protected LdapUserService ldapUserService;

    public void setName(String name) { this.name=name; }
    public void setCodeDelay(int codeDelay) { this.codeDelay = codeDelay; }
    public void setValidationCode(ValidationCode validationCode) { this.validationCode = validationCode; }
    public void setLdapSchema(LdapSchema ldapSchema) { this.ldapSchema = ldapSchema; }
    public void setLdapUserService(LdapUserService ldapUserService) { this.ldapUserService = ldapUserService; }

    
    public abstract boolean isPossible(LdapUser ldapUser);
    public abstract void send(String id) throws ChannelException;

    public String getName() {
        return name;
    }

    protected LdapUser getUser(String id) throws ChannelException {
        List<LdapUser> ldapUserList = ldapUserService.getLdapUsersFromFilter("("+ldapSchema.login+"="+ id + ")");
        if (ldapUserList.size() == 0) throw new ChannelException("Utilisateur "+id+" inconnu");
        return ldapUserList.get(0); 
    }

    protected static InternetAddress to_InternetAddress(String mailPerso) throws ChannelException {
        try {
            return new InternetAddress(mailPerso);
        } catch (AddressException e) {
            throw new ChannelException("Probleme de création de InternetAddress "+mailPerso);
        }
    }

}
