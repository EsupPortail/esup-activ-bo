/**
 * 
 */
package org.esupportail.activbo.domain.beans.channels;

import java.util.Map;
import java.util.List;
import java.util.Set;

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
	private Map<String,List<String>> access;
	private Map<String,List<String>> deny;
	/* (non-Javadoc)
	 * @see org.esupportail.activbo.domain.beans.channels.AbstractChannel#send(java.lang.String)
	 */
	@Override
	public void send(String id) throws ChannelException {
		
			this.validationCode.generateChannelCode(id, codeDelay, getName());
			
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
			String code = validationCode.getCode(id);
			mailBody=mailBody.replace("{0}", id);
			mailBody=mailBody.replace("{1}", code);
			mailBody=mailBody.replace("{2}", validationCode.getDate(id));
			mailBody=mailBody.replace("{3}", displayName);
			
			smtpService.send(mail,newSubject,mailBody,"");
			logger.info(id + "@" + code + ": Envoi du code à l'adresse mail gestionnaire "+mailGest);
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
		if(access==null && deny==null) return true; //si pas de définition de droit d'accès, le canal est disponible pour tout profil
		
		if(deny!=null && profileMatches(deny,ldapUser))				
			return false;																					
					
		if(access!=null && profileMatches(access,ldapUser))
			return true;
		
		if(deny!=null) return true; 
		else return false;
	}
	
	private boolean profileMatches(Map<String,List<String>> profile, LdapUser ldapUser){
		Set<String> keySet = profile.keySet();
		for(String attribute : keySet) {
			List<String> values=profile.get(attribute);
			List<String> userValues=ldapUser.getAttributes(attribute);				
			for(String userValue:userValues)
				if(values.contains(userValue))		
					return true;																					
		}
		return false;
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

	/**
	 * @return the access
	 */
	public Map<String, List<String>> getAccess() {
		return access;
	}

	/**
	 * @param access the access to set
	 */
	public void setAccess(Map<String, List<String>> access) {
		this.access = access;
	}

	/**
	 * @return the deny
	 */
	public Map<String, List<String>> getDeny() {
		return deny;
	}

	/**
	 * @param deny the deny to set
	 */
	public void setDeny(Map<String, List<String>> deny) {
		this.deny = deny;
	}
	
	

}
