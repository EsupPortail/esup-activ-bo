/**
 * 
 */
package org.esupportail.activbo.domain.beans.channels;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.smtp.AsynchronousSmtpServiceImpl;

/**
 * @author aanli
 *
 */
public class Mail2GestChannel extends AbstractChannel{

	private String mailGest;
	private AsynchronousSmtpServiceImpl smtpService;
	private String mailCodeSubject;
	private String mailCodeBody;
	private String attributeDisplayName;
	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.AbstractChannel#send(java.lang.String)
	 */
	@Override
	public void send(String id) throws ChannelException {
		
			this.validationCode.generateCode(id, codeDelay);
			logger.debug("Insertion code pour l'utilisateur "+id+" dans la table effectuée");
			
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+ldapSchema.getLogin()+"="+ id + ")");
			if (ldapUserList.size() == 0) throw new ChannelException("Utilisateur "+id+" inconnu");
			
			LdapUser ldapUserRead = ldapUserList.get(0); 
			String displayName = ldapUserRead.getAttribute(attributeDisplayName);
			String newSubject = this.mailCodeSubject.replace("{0}", displayName);
									
			InternetAddress mail=null;			
			try {
				mail = new InternetAddress(mailGest);
			} catch (AddressException e) {
				throw new ChannelException("Problem de création de InternetAddress "+mailGest);
			}
			
			String mailBody=this.mailCodeBody;
			mailBody=mailBody.replace("{0}", id);
			mailBody=mailBody.replace("{1}", validationCode.getCode(id));
			mailBody=mailBody.replace("{2}", validationCode.getDate(id));
			mailBody=mailBody.replace("{3}", displayName);
			
			smtpService.send(mail,newSubject,mailBody,"");
			logger.debug("Envoi du code à l'adresse mail "+mailGest);										
	}

	/**
	 * @param smtpService the smtpService to set
	 */
	public void setSmtpService(AsynchronousSmtpServiceImpl smtpService) {
		this.smtpService = smtpService;
	}
	/**
	 * @param mailCodeSubject the mailCodeSubject to set
	 */
	public void setMailCodeSubject(String mailCodeSubject) {
		this.mailCodeSubject = mailCodeSubject;
	}
	/**
	 * @param mailCodeBody the mailCodeBody to set
	 */
	public void setMailCodeBody(String mailCodeBody) {
		this.mailCodeBody = mailCodeBody;
	}

	/**
	 * @param mailGest the mailGest to set
	 */
	public void setMailGest(String mailGest) {
		this.mailGest = mailGest;
	}
	
	public boolean isPossible(LdapUser ldapUser){
		return true;
	}

	/**
	 * @return the attributeDisplayName
	 */
	public String getAttributeDisplayName() {
		return attributeDisplayName;
	}

	/**
	 * @param attributeDisplayName the attributeDisplayName to set
	 */
	public void setAttributeDisplayName(String attributeDisplayName) {
		this.attributeDisplayName = attributeDisplayName;
	}
	
	

}
