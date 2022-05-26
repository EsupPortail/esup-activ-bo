package org.esupportail.activbo.domain;

import java.util.Collections;
import java.util.Random;

import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.PrincipalNotExistsException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;


public class LdapImpl extends DomainServiceImpl {

    /**Cette classe permet d'utiliser que l'impl�mentation LDAP
     * 
     */
    private static final long serialVersionUID = -920391586782473692L;
    private final Logger logger = new LoggerImpl(getClass());


    public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{      
        try {
            var ldapUser = getLdapUser(id, code);
            // changement de mot de passe
            ldapUser.getAttributes().put(ldapSchema.password, Collections.singletonList(encryptPassword(currentPassword)));
            setShadowLastChange(ldapUser);
            finalizeLdapWriting(ldapUser);
          } catch(Exception e) { exceptions (e); }
    }
    
    public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LdapProblemException,UserPermissionException,KerberosException, LoginException{     
        try {
            changeLogin(id, code, newLogin);         
            setPassword(id, code, currentPassword);
        } catch(Exception e) { exceptions (e); }
    }
    
    public void changeLogin(String id, String code,String newLogin)throws LdapProblemException,UserPermissionException,KerberosException, LoginException, PrincipalNotExistsException{
        try {
            var ldapUserNewLogin= getLdapUser("("+ldapSchema.login+"="+newLogin+ ")");              
            if (ldapUserNewLogin!=null) {throw new LdapLoginAlreadyExistsException("newLogin = "+newLogin); }
                
            var ldapUser=getLdapUser(id, code);
            ldapUser.getAttributes().put(ldapSchema.login, Collections.singletonList(newLogin));           
            finalizeLdapWriting(ldapUser);             
        } catch (Exception e) { exceptions (e); }
    }
    
    public String validatePassword(String supannAliasLogin, String password)throws KRBException, LdapProblemException, LoginException {
        return null; // no (extra) validation, relying on application not calling esup-activ-bo to validate it!
    }

    private String encryptPassword(String password) {
        /*
         * If we look at phpldapadmin SSHA encryption algorithm in :
         * /usr/share/phpldapadmin/lib/functions.php function password_hash(
         * $password_clear, $enc_type ) salt length for SSHA is 4
         */
        final int SALT_LENGTH = 4;
        
        /* Salt generation */
        var salt = new byte[SALT_LENGTH];
        new Random().nextBytes(salt);
        /* SSHA encoding */
        return new LdapShaPasswordEncoder().encodePassword(password, salt);
    }

}
