package org.esupportail.activbo.domain;

import java.util.Collections;
import java.util.Random;

import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.ldap.LdapAttributesModificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LdapImpl extends DomainServiceImpl {

    /**Cette classe permet d'utiliser que l'impl�mentation LDAP
     * 
     */
    private static final long serialVersionUID = -920391586782473692L;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public void setPassword(String id,String code,final String currentPassword) throws LdapProblemException,UserPermissionException,LoginException{     
        try {
            verifyCode(id, code);
            var ldapUser = getLdapUserOut(id);
            // changement de mot de passe
            ldapUser.attributes().put(ldapSchema.password, Collections.singletonList(encryptPassword(currentPassword)));
            setShadowLastChange(ldapUser);
            finalizeLdapWriting(ldapUser);
        } catch (LdapAttributesModificationException e) {
            logger.error("", e);
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
    }
    
    public void setPassword(String id,String code,String newLogin, final String currentPassword) throws LoginException, LdapProblemException, UserPermissionException {     
            changeLogin(id, code, newLogin);         
            setPassword(id, code, currentPassword);
    }
    
    public void changeLogin(String id, String code,String newLogin) throws LoginException, LdapProblemException, UserPermissionException {
        try {
            var ldapUserNewLogin= getLdapUser("("+ldapSchema.login+"="+newLogin+ ")", new String[] {});             
            if (ldapUserNewLogin!=null) {throw new LdapLoginAlreadyExistsException("newLogin = "+newLogin); }
                
            verifyCode(id, code);
            var ldapUser=getLdapUserOut(id);
            ldapUser.attributes().put(ldapSchema.login, Collections.singletonList(newLogin));          
            finalizeLdapWriting(ldapUser);             
        } catch (LdapAttributesModificationException e) {
            logger.error("", e);
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
    }
    
    public String validatePassword(String supannAliasLogin, String password) {
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
