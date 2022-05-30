package org.esupportail.activbo.domain.beans.channels;

import java.util.Collections;
import java.util.Set;

import org.esupportail.activbo.services.ldap.LdapUser;

/**
 * @author aanli
 * 
 * Ce canal peut etre utilise par les utilisateurs disposant deja d'un code d'activation
 */
public class CodeChannel implements Channel {

    // default name
    private String name="code";
    public void setName(String name) { this.name=name; }

    public String getName() {       
        return name;
    }

    public Set<String> neededAttrs() {
        return Collections.emptySet();
    }

    public boolean isPossible(LdapUser ldapUser) {  
        return true;
    }

    public void send(String id) throws ChannelException {
        // nothing to do :-)
    }


}
