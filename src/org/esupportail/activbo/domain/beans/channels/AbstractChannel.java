/**
 * 
 */
package org.esupportail.activbo.domain.beans.channels;

import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.commons.services.ldap.LdapUserService;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

/**
 * @author aanli
 *
 */
public abstract class AbstractChannel implements Channel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4565683340244617289L;
	
	/**
	 * Nom du canal
	 */
	private String name;
	
	protected int codeDelay;
	protected ValidationCode validationCode;
	protected String accountDescrIdKey;
	/**
	 * {@link LdapUserService}.
	 */
	protected LdapUserService ldapUserService;
	
	protected final Logger logger = new LoggerImpl(getClass());

	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#send(java.lang.String, java.lang.String)
	 */
	public abstract void send(String id) throws ChannelException;
	
	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.Channel#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name=name;
		
	}

	/**
	 * @param ldapUserService the ldapUserService to set
	 */
	public void setLdapUserService(LdapUserService ldapUserService) {
		this.ldapUserService = ldapUserService;
	}


	/**
	 * @param codeDelay the codeDelay to set
	 */
	public void setCodeDelay(int codeDelay) {
		this.codeDelay = codeDelay;
	}

	/**
	 * @param validationCode the validationCode to set
	 */
	public void setValidationCode(ValidationCode validationCode) {
		this.validationCode = validationCode;
	}

	/**
	 * @return the accountDescrIdKey
	 */
	public String getAccountDescrIdKey() {
		return accountDescrIdKey;
	}

	/**
	 * @param accountDescrIdKey the accountDescrIdKey to set
	 */
	public void setAccountDescrIdKey(String accountDescrIdKey) {
		this.accountDescrIdKey = accountDescrIdKey;
	}
	
 


}
