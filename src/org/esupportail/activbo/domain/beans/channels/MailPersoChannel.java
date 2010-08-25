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
public class MailPersoChannel extends AbstractChannel{

	private String attributeMailPerso;
	private AsynchronousSmtpServiceImpl smtpService;
	private String mailCodeSubject;
	private String mailCodeBody;
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
			
			String mailPerso = ldapUserRead.getAttribute(attributeMailPerso);
			
			if(mailPerso==null) throw new ChannelException("Utilisateur "+id+" n'a pas de mail perso");
									
			InternetAddress mail=null;			
			try {
				mail = new InternetAddress(mailPerso);
			} catch (AddressException e) {
				throw new ChannelException("Probleme de création de InternetAddress "+mailPerso);
			}
			
			String mailBody=this.mailCodeBody;
			mailBody=mailBody.replace("{0}", validationCode.getCode(id));
			mailBody=mailBody.replace("{1}", validationCode.getDate(id));
			
			smtpService.send(mail,this.mailCodeSubject,mailBody,"");
			
			logger.debug("Envoi du code à l'adresse mail "+mailPerso);										
	}
	/**
	 * @param attributeMailPerso the attributeMailPerso to set
	 */
	public void setAttributeMailPerso(String attributeMailPerso) {
		this.attributeMailPerso = attributeMailPerso;
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
	
	@Override
	public boolean isPossible(LdapUser ldapUser){
				
		String mailPerso = ldapUser.getAttribute(attributeMailPerso);
		
		if(mailPerso==null) return false;
		
		return true;
		
	}

}
