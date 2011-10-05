package org.esupportail.activbo.domain.beans.channels;

import org.esupportail.commons.services.ldap.LdapUser;

/**
 * @author aanli
 * 
 * Ce canal peut être utilisé par les utilisateurs disposant déjà d'un code d'activation
 */
public class CodeChannel implements Channel {

	private String name="code";
	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#getName()
	 */
	public String getName() {		
		return name;
	}

	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#isPossible(org.esupportail.commons.services.ldap.LdapUser)
	 */
	public boolean isPossible(LdapUser ldapUser) {	
		return true;
	}

	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#send(java.lang.String)
	 */
	public void send(String id) throws ChannelException {
	
	}

	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name=name;

	}

}
