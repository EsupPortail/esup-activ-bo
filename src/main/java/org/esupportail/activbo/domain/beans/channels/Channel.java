package org.esupportail.activbo.domain.beans.channels;

import java.util.Set;

import org.esupportail.activbo.services.ldap.LdapUser;


/**
 * @author aanli
 * Canal permettant l'envoi du code d'activation pour un utilisateur donne
 */
public interface Channel {
    /**
     * @param id login de l'utilisateur concerne par le code
     * @throws ChannelException 
     */
    public void send(String id) throws ChannelException;
    
    /**
     * @param name nom du canal
     */
    public void setName(String name);
    
    public String getName();
    
    public Set<String> neededAttrs();
    public boolean isPossible(LdapUser ldapUser);

}
