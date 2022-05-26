package org.esupportail.activbo.domain;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBAdmin;
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.activbo.services.kerberos.KRBPrincipalAlreadyExistsException;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;


public class KerbLdapImpl extends DomainServiceImpl {

    /**Cette classe permet d'utiliser l'implementation LDAP et Kerberos
     * 
     */
    private static final long serialVersionUID = 8874960057301525796L;
    private final Logger logger = new LoggerImpl(getClass());
    
    private String krbLdapMethod;
    private String krbRealm;    
    private KRBAdmin kerberosAdmin;

    public final void setKrbLdapMethod(String krbLdapMethod) { this.krbLdapMethod = krbLdapMethod; }
    public final void setKrbRealm(String krbRealm) { this.krbRealm = krbRealm; }
    public void setKerberosAdmin(KRBAdmin kerberosAdmin) { this.kerberosAdmin = kerberosAdmin; }


    public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{
        setPassword(id, code, null, currentPassword);
    }
    
    public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{             
        try {
            var ldapUser= getLdapUser(id, code);
            setRedirectionKerberos(ldapUser, newLogin != null ? newLogin : id);
            var created = true;
            try {
                kerberosAdmin.add(id, currentPassword);
            } catch (KRBPrincipalAlreadyExistsException e) {
                created = false;
            }
            if (created) {
                logger.info(id + "@" + code + ": Ajout de mot de passe dans kerberos effectu�e");
                if (newLogin != null) {
                    ldapUser.getAttributes().put(ldapSchema.login, Collections.singletonList(newLogin));
                }
            } else {
                logger.info(id + "@" + code + ": Le compte kerberos de l'utilisateur existe d�ja, Modification du password");
                kerberosAdmin.changePasswd(id, currentPassword);    
            }
            finalizeLdapWriting(ldapUser);
        } catch (Exception e) {
            exceptions(e);
        }
    }
    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException, LoginException, PrincipalNotExistsException{
       try {
            if (getLdapUserId(newLogin) != null) {throw new LdapLoginAlreadyExistsException("newLogin = "+newLogin);    }
            
            var ldapUser=getLdapUser(id, code);   
            setRedirectionKerberos(ldapUser,newLogin);
            if (!kerberosAdmin.exists(id))  throw new PrincipalNotExistsException();//lever exception puis lancer setpassword au niveau du FO
            // le compte kerb existe
            ldapUser.getAttributes().put(ldapSchema.login, Collections.singletonList(newLogin));
            kerberosAdmin.rename(id, newLogin);
            finalizeLdapWriting(ldapUser);
        
       } catch(Exception e) { exceptions (e); }
       
    }
    
    private void setRedirectionKerberos(LdapUser ldapUser,String id)throws LdapException{
        String currentPrincipal = ldapUser.getAttribute(ldapSchema.krbPrincipal);
        String currentPasswd = ldapUser.getAttribute(ldapSchema.password);      
        
        logger.debug("ancien redirection : "+currentPasswd );
        String newPrincipal = to_principal(id);
        String redirectKer = "{"+krbLdapMethod+"}" + newPrincipal;
        
        if (!redirectKer.equals(currentPasswd) || !newPrincipal.equals(currentPrincipal)) {
            logger.debug("Le compte Kerberos ne g�re pas encore l'authentification");

            setShadowLastChange(ldapUser);
            
            //Writing of principal in LDAP 
            if (!StringUtils.isEmpty(ldapSchema.krbPrincipal)) {
                ldapUser.getAttributes().put(ldapSchema.krbPrincipal, Collections.singletonList(newPrincipal));
                logger.debug("Writing principal in LDAP : " + newPrincipal);
            }
            
            //Writing of Kerberos redirection in LDAP 
            ldapUser.getAttributes().put(ldapSchema.password, Collections.singletonList(redirectKer));
            logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
        }
    }
        
    public String validatePassword(String supannAliasLogin, String password)throws KRBException, LdapProblemException, LoginException {
        return kerberosAdmin.validatePassword(to_principal(supannAliasLogin), password);
    }

    private String to_principal(String id) {
        return id + "@" + krbRealm;
    }
}
