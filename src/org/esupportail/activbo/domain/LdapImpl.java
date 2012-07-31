package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class LdapImpl extends DomainServiceImpl {

	/**Cette classe permet d'utiliser que l'implémentation LDAP
	 * 
	 */
	private static final long serialVersionUID = -920391586782473692L;
	private final Logger logger = new LoggerImpl(getClass());

	//Constructeur
	public LdapImpl() {	}
	
	//
	public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		
		LdapUser ldapUser=this.getLdapUserPassword(id, code, currentPassword);		
			// changement de mot de passe
			List<String> list=new ArrayList<String>();			
			list.add(encryptPassword(currentPassword));
			ldapUser.getAttributes().put(getLdapSchema().getPassword(), list);
			listShadowLastChangeAttr(ldapUser);
	}
	
	//
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		
		LdapUser ldapUser=this.getLdapUserPassword(id, code, newLogin, currentPassword);
			// changement de mot de passe
			List<String> list=new ArrayList<String>();
			
			list.add(newLogin);
			ldapUser.getAttributes().put(getLdapSchema().getLogin(), list);
		
			list.add(ldapUser.getAttribute(getLdapSchema().getPassword()));
			ldapUser.getAttributes().put(getLdapSchema().getPassword(),list);
			listShadowLastChangeAttr(ldapUser);
	}
	
	//
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException, PrincipalNotExistsException{
	    LdapUser ldapUser=getLdapUserLogin(id, code, newLogin);
	   try {
		    List<String> list=new ArrayList<String>();
		   list.add(newLogin);
		   ldapUser.getAttributes().put(getLdapSchema().getLogin(),list);
		   
		   list.add(ldapUser.getAttribute(getLdapSchema().getPassword()));
		   ldapUser.getAttributes().put(getLdapSchema().getPassword(),list);
		   
		   listShadowLastChangeAttr(ldapUser);
			   
		   }catch(Exception  e){exceptions (e);}
	}
	
	
	private void listShadowLastChangeAttr(LdapUser ldapUser){
		// Ecrire l'attribut shadowLastChange dans LDAP
		List<String> listShadowLastChangeAttr = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		String shadowLastChange = Integer.toString((int) Math.floor(cal.getTimeInMillis() / (1000 * 3600 * 24)));
		listShadowLastChangeAttr.add(shadowLastChange);
		ldapUser.getAttributes().put(getLdapSchema().getShadowLastChange(),listShadowLastChangeAttr);
		if (logger.isDebugEnabled()) {logger.debug("Writing shadowLastChange in LDAP : "+ shadowLastChange );}
		
		 this.finalizeLdapWriting(ldapUser);
		
	}
	private String encryptPassword(String passWord) {
		/*
		 * If we look at phpldapadmin SSHA encryption algorithm in :
		 * /usr/share/phpldapadmin/lib/functions.php function password_hash(
		 * $password_clear, $enc_type ) salt length for SSHA is 4
		 */
		final int SALT_LENGTH = 4;
		
		LdapShaPasswordEncoder ldapShaPasswordEncoder = new LdapShaPasswordEncoder();
		/* Salt generation */
		byte[] salt = new byte[SALT_LENGTH];
		Random generator = new Random();
		generator.nextBytes(salt);
		/* SSHA encoding */
		String encryptedPassword = ldapShaPasswordEncoder.encodePassword(passWord, salt);
		

		return encryptedPassword;
	}
	


}






