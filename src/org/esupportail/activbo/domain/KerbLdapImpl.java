package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.List;

import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBPrincipalAlreadyExistsException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class KerbLdapImpl extends DomainServiceImpl {

	/**Cette classe permet d'utiliser l'impl�mentation LDAP et Kerberos
	 * 
	 */
	private static final long serialVersionUID = 8874960057301525796L;
	private final Logger logger = new LoggerImpl(getClass());

	public KerbLdapImpl() {}
	

	//
	public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		LdapUser ldapUser=this.getLdapUserPassword(id, code, currentPassword);
		ldapUser.getAttributes().clear();
		
		try {
			this.gestRedirectionKerberos(ldapUser,id);
			//Ajout ou modification du mot de passe dans kerberos
			kerberosAdmin.add(id, currentPassword);
			logger.info("Ajout de mot de passe dans kerberos effectu�e");
			this.finalizeLdapWriting(ldapUser);
		} 
		catch (KRBPrincipalAlreadyExistsException e){
			
			try{
				logger.info("Le compte kerberos existe d�ja, Modification du password");
				kerberosAdmin.changePasswd(id, currentPassword);
				this.finalizeLdapWriting(ldapUser);
			
			} catch(Exception  e2){exceptions (e2);}
			
		}catch(Exception  e3){exceptions (e3);}
	}
	
	//
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		
		LdapUser ldapUser=this.getLdapUserPassword(id, code, newLogin, currentPassword);
		ldapUser.getAttributes().clear();
		 try {
			   ldapUser = this.getLdapUser("("+getLdapSchema().getLogin()+"="+ id + ")");
				this.gestRedirectionKerberos(ldapUser,newLogin);
				kerberosAdmin.add(id, currentPassword);
				logger.info("Ajout de mot de passe dans kerberos effectu�e");
				
				List<String> list=new ArrayList<String>();
				list.add(newLogin);
				ldapUser.getAttributes().put(getLdapSchema().getLogin(),list);

				this.finalizeLdapWriting(ldapUser);
		 } catch (KRBPrincipalAlreadyExistsException e){
			try{
				logger.info("Le compte kerberos existe d�ja, Modification du password");
				kerberosAdmin.changePasswd(id, currentPassword);
				this.finalizeLdapWriting(ldapUser);
			
			} catch(Exception  e2){exceptions (e2);}
			
		}catch(Exception  e3){exceptions (e3);}
	}
	
	//
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException, PrincipalNotExistsException{
	 
	   LdapUser ldapUser=this.getLdapUserLogin(id, code, newLogin);
	   ldapUser.getAttributes().clear();
	   try {
			this.gestRedirectionKerberos(ldapUser,newLogin);
			if (!kerberosAdmin.exists(id))	throw new PrincipalNotExistsException("");//lever exception puis lancer setpassword au niveau du FO
			// le compte kerb existe
			List<String> list=new ArrayList<String>();
		    list.add(newLogin);
		    ldapUser.getAttributes().put(getLdapSchema().getLogin(),list);
		    kerberosAdmin.rename(id, newLogin);
			finalizeLdapWriting(ldapUser);
		
	   } catch(Exception  e){exceptions (e);}
	   
	}
	

	
}




