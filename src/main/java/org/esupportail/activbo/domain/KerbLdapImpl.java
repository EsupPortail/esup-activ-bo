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
import org.esupportail.activbo.services.ldap.LdapAttributesModificationException;
import org.esupportail.activbo.services.ldap.LdapUser;
import org.esupportail.activbo.services.ldap.LdapUserImpl;
import org.esupportail.activbo.services.ldap.LdapUserOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KerbLdapImpl extends DomainServiceImpl {

    /**Cette classe permet d'utiliser l'implementation LDAP et Kerberos
     * 
     */
    private static final long serialVersionUID = 8874960057301525796L;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
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
            verifyCode(id, code);
            var ldapUser = getLdapUserForKrb(id);
            LdapUserOut ldapUserOut = new LdapUserImpl(ldapUser.getDN());
            setRedirectionKerberos(ldapUser, ldapUserOut, newLogin != null ? newLogin : id);
            var created = true;
            try {
                kerberosAdmin.add(id, currentPassword);
            } catch (KRBPrincipalAlreadyExistsException e) {
                created = false;
            }
            if (created) {
                logger.info(id + "@" + code + ": Ajout de mot de passe dans kerberos effectuee");
                if (newLogin != null) {
                    ldapUserOut.attributes().put(ldapSchema.login, Collections.singletonList(newLogin));
                }
            } else {
                logger.info(id + "@" + code + ": Le compte kerberos de l'utilisateur existe deja, Modification du password");
                kerberosAdmin.changePasswd(id, currentPassword);    
            }
            finalizeLdapWriting(ldapUserOut);
        } catch (KRBException e) {
            logger.error("", e);
            throw new KerberosException("Probleme au niveau de Kerberos", e);
        } catch (LdapAttributesModificationException e) {
            logger.error("", e);
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
    }
    private LdapUser getLdapUserForKrb(String id) throws LdapProblemException, LoginException {
        var ldapUser = getLdapUserId(id, new String[] { ldapSchema.krbPrincipal, ldapSchema.password });
        if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
        return ldapUser;
    }
    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException, LoginException, PrincipalNotExistsException{
       try {
            if (getLdapUserId(newLogin, new String[] {}) != null) {
                throw new LdapLoginAlreadyExistsException("newLogin = "+newLogin);
            }
            verifyCode(id, code);
            var ldapUser = getLdapUserForKrb(id);
            var ldapUserOut = new LdapUserImpl(ldapUser.getDN());
            setRedirectionKerberos(ldapUser, ldapUserOut, newLogin);
            if (!kerberosAdmin.exists(id))  throw new PrincipalNotExistsException();//lever exception puis lancer setpassword au niveau du FO
            // le compte kerb existe
            ldapUserOut.attributes().put(ldapSchema.login, Collections.singletonList(newLogin));
            kerberosAdmin.rename(id, newLogin);
            finalizeLdapWriting(ldapUserOut);
        } catch (KRBException e) {
            logger.error("", e);
            throw new KerberosException("Probleme au niveau de Kerberos", e);
        } catch (LdapAttributesModificationException e) {
            logger.error("", e);
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
       
    }
    
    private void setRedirectionKerberos(LdapUser in, LdapUserOut out, String id) {
        String currentPrincipal = in.getAttribute(ldapSchema.krbPrincipal);
        String currentPasswd = in.getAttribute(ldapSchema.password);        
        
        logger.debug("ancien redirection : "+currentPasswd );
        String newPrincipal = to_principal(id);
        String redirectKer = "{"+krbLdapMethod+"}" + newPrincipal;
        
        if (!redirectKer.equals(currentPasswd) || !newPrincipal.equals(currentPrincipal)) {
            logger.debug("Le compte Kerberos ne gere pas encore l'authentification");

            setShadowLastChange(out);
            
            //Writing of principal in LDAP 
            if (!StringUtils.isEmpty(ldapSchema.krbPrincipal)) {
                out.attributes().put(ldapSchema.krbPrincipal, Collections.singletonList(newPrincipal));
                logger.debug("Writing principal in LDAP : " + newPrincipal);
            }
            
            //Writing of Kerberos redirection in LDAP 
            out.attributes().put(ldapSchema.password, Collections.singletonList(redirectKer));
            logger.debug("Writing Kerberos redirection in LDAP : " + redirectKer);
        }
    }
        
    public String validatePassword(String supannAliasLogin, String password) throws KRBException {
        return kerberosAdmin.validatePassword(to_principal(supannAliasLogin), password);
    }

    private String to_principal(String id) {
        return id + "@" + krbRealm;
    }
}
