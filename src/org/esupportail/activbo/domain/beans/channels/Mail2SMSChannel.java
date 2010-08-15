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
public class Mail2SMSChannel extends AbstractChannel{

	private String attributePager;
	private String mailSMS;
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
			
			List<LdapUser> ldapUserList = this.ldapUserService.getLdapUsersFromFilter("("+accountDescrIdKey+"="+ id + ")");
			
			if (ldapUserList.size() == 0) throw new ChannelException("Utilisateur "+id+" inconnu");
	
			LdapUser ldapUserRead = ldapUserList.get(0); 
			
			String pager = ldapUserRead.getAttribute(attributePager);
			
			if(pager==null) throw new ChannelException("Utilisateur "+id+" n'a pas numéro de portable");
									
			InternetAddress mail=null;			
			try {
				mail = new InternetAddress(mailSMS);
			} catch (AddressException e) {
				throw new ChannelException("Problem de création de InternetAddress "+mailSMS);
			}
			
			String mailBody=this.mailCodeBody;
			mailBody=mailBody.replace("{0}", pager);
			mailBody=mailBody.replace("{1}", validationCode.getCode(id));
			mailBody=mailBody.replace("{2}", validationCode.getDate(id));
			
			smtpService.send(mail,this.mailCodeSubject,mailBody,"");
			
			logger.debug("Envoi du code par sms via mail2sms au numéro portable "+pager);										
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
	 * @param attributePager the attributePager to set
	 */
	public void setAttributePager(String attributePager) {
		this.attributePager = attributePager;
	}

	/**
	 * @param mailSMS the mailSMS to set
	 */
	public void setMailSMS(String mailSMS) {
		this.mailSMS = mailSMS;
	}

}
