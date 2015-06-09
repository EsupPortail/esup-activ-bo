package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.List;

import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBAdmin;
import org.esupportail.activbo.services.kerberos.KRBPrincipalAlreadyExistsException;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

public class KerbLdapImpl extends DomainServiceImpl {

	/**Cette classe permet d'utiliser l'impl�mentation LDAP et Kerberos
	 * 
	 */
	private static final long serialVersionUID = 8874960057301525796L;
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * kerberos.ldap.method
	 * kerberos.host
	 */
	private String krbLdapMethod,krbHost;
	
	/**
	 * {@link KerberosAdmin}
	 */
	private KRBAdmin kerberosAdmin;

	public KerbLdapImpl() {}
	

	//
	public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
		LdapUser ldapUser=null;		
		try {
			ldapUser=this.getLdapUser(id, code);
			this.gestRedirectionKerberos(ldapUser,id);
			//Ajout ou modification du mot de passe dans kerberos
			kerberosAdmin.add(id, currentPassword);
			logger.info("Ajout de mot de passe dans kerberos effectu�e");
			this.finalizeLdapWriting(ldapUser);
		} 
		catch (KRBPrincipalAlreadyExistsException e){
			
			try{
				logger.info("Le compte kerberos de l'utilisateur "+id+" existe d�ja, Modification du password");
				kerberosAdmin.changePasswd(id, currentPassword);
				this.finalizeLdapWriting(ldapUser);
			
			} catch(Exception  e2){exceptions (e2);}
			
		}catch(Exception  e3){exceptions (e3);}
	}
	
	//
	public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{				
		LdapUser ldapUser=null; 
		try {
			   ldapUser=this.getLdapUser(id, code);
				this.gestRedirectionKerberos(ldapUser,newLogin);
				kerberosAdmin.add(id, currentPassword);
				logger.info("Ajout de mot de passe dans kerberos effectu�e");
				
				List<String> list=new ArrayList<String>();
				list.add(newLogin);
				ldapUser.getAttributes().put(getLdapSchema().getLogin(),list);

				this.finalizeLdapWriting(ldapUser);
		 } catch (KRBPrincipalAlreadyExistsException e){
			try{
				logger.info("Le compte kerberos de l'utilisateur "+id+" existe d�ja, Modification du password");
				kerberosAdmin.changePasswd(id, currentPassword);
				this.finalizeLdapWriting(ldapUser);
			
			} catch(Exception  e2){exceptions (e2);}
			
		}catch(Exception  e3){exceptions (e3);}
	}
	
	//
	public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException,LoginAlreadyExistsException, LoginException, PrincipalNotExistsException{
	   LdapUser ldapUser=null;
	   try {
		    LdapUser ldapUserNewLogin= this.getLdapUser("("+getLdapSchema().getLogin()+"="+newLogin+ ")");				
			if (ldapUserNewLogin!=null) {throw new LdapLoginAlreadyExistsException("newLogin = "+newLogin);	}
			
		    ldapUser=this.getLdapUser(id, code);   
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
	
	private void gestRedirectionKerberos(LdapUser ldapUser,String id)throws LdapException{
		List<String> passwds	= ldapUser.getAttributes(getLdapSchema().getPassword());
		List<String> principals	= ldapUser.getAttributes(getLdapSchema().getKrbPrincipal());
		
		String krbPrincipal=null;
		String ldapUserRedirectKerb=null;
		if(passwds.size()>0) ldapUserRedirectKerb =passwds.get(0);
		
		if(principals.size()>0)	krbPrincipal=principals.get(0);
		
		logger.debug("ancien redirection : "+ldapUserRedirectKerb);
		String redirectKer="{"+krbLdapMethod+"}"+id+"@"+krbHost;
		String newPrincipal=id+"@"+krbHost;
		
		if (!redirectKer.equals(ldapUserRedirectKerb) || !newPrincipal.equals(krbPrincipal)) {
			logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");

			// Writing of shadowLastChange in LDAP 
			listShadowLastChangeAttr(ldapUser);
			
			//Writing of krbPrincipal in LDAP 
			if( !"".equals(getLdapSchema().getKrbPrincipal()) && getLdapSchema().getKrbPrincipal()!=null ) {
				List<String> listKrbPrincipalAttr = new ArrayList<String>();
				listKrbPrincipalAttr.add(newPrincipal);
				ldapUser.getAttributes().put(getLdapSchema().getKrbPrincipal(),listKrbPrincipalAttr);
				if (logger.isDebugEnabled()) {
					logger.debug("Writing principal in LDAP : " + newPrincipal);
				}
			}
			
			//Writing of Kerberos redirection in LDAP 
			List<String> listPasswordAttr = new ArrayList<String>();
			listPasswordAttr.add(redirectKer);
			ldapUser.getAttributes().put(getLdapSchema().getPassword(),listPasswordAttr);
			if (logger.isDebugEnabled()) {
				logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
			}
		}
	}
	
	/**
	 * @param krbLdapMethod
	 */
	public final void setKrbLdapMethod(String krbLdapMethod) {
		this.krbLdapMethod = krbLdapMethod;
	}

	/**
	 * @param krbHost
	 */
	public final void setKrbHost(String krbHost) {
		this.krbHost = krbHost;
	}

	public void setKerberosAdmin(KRBAdmin kerberosAdmin) {
		this.kerberosAdmin = kerberosAdmin;
	}
	
}




