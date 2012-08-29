package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class LdapImpl extends DomainServiceImpl {

	/**Cette classe permet d'utiliser que l'impl�mentation LDAP
	 * 
	 */
	private static final long serialVersionUID = -920391586782473692L;
	private final Logger logger = new LoggerImpl(getClass());

	//Constructeur
	public LdapImpl() {	}
	
	//
	public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{		
		LdapUser ldapUser=null;
		try {
			ldapUser=this.getLdapUser(id, code);
			// changement de mot de passe
			List<String> list=new ArrayList<String>();			
			list.add(encryptPassword(currentPassword));
			ldapUser.getAttributes().put(getLdapSchema().getPassword(), list);
			listShadowLastChangeAttr(ldapUser);
			finalizeLdapWriting(ldapUser);
		  } catch(Exception  e){exceptions (e);}
	}
	
	//
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		LdapUser ldapUser=null;
		try {
			ldapUser=this.getLdapUser(id, code);
			// changement de mot de passe
			List<String> list=new ArrayList<String>();
			
			list.add(newLogin);
			ldapUser.getAttributes().put(getLdapSchema().getLogin(), list);
			
			list=new ArrayList<String>();
			list.add(ldapUser.getAttribute(getLdapSchema().getPassword()));
			ldapUser.getAttributes().put(getLdapSchema().getPassword(),list);
			listShadowLastChangeAttr(ldapUser);
			finalizeLdapWriting(ldapUser);
		   } catch(Exception  e){exceptions (e);}
	}
	
	//
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException, PrincipalNotExistsException{
		LdapUser ldapUser=null;
		try {
			 LdapUser ldapUserNewLogin= this.getLdapUser("("+getLdapSchema().getLogin()+"="+newLogin+ ")");				
			 if (ldapUserNewLogin!=null) {throw new LdapLoginAlreadyExistsException("newLogin = "+newLogin);	}
				
			ldapUser=this.getLdapUser(id, code);
		    List<String> list=new ArrayList<String>();
		   list.add(newLogin);
		   ldapUser.getAttributes().put(getLdapSchema().getLogin(),list);		   
		   finalizeLdapWriting(ldapUser);
			   
		   }catch(Exception  e){exceptions (e);}
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






