package org.esupportail.activbo.domain;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;
import org.apache.commons.lang.StringUtils;
import org.esupportail.activbo.exceptions.LdapLoginAlreadyExistsException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.ldap.LdapAttributesModificationException;
import org.esupportail.activbo.services.ldap.LdapUserOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LdapImpl extends DomainServiceImpl {

    /**Cette classe permet d'utiliser que l'implementation LDAP
     * 
     */
    private static final long serialVersionUID = -920391586782473692L;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public void setPassword(String id,String code,final String password) throws LdapProblemException,UserPermissionException,LoginException{
        try {
            verifyCode(id, code);
            var ldapUser = getLdapUserOut(id);
            // changement de mot de passe
            ldapUser.attributes().put(ldapSchema.password, Collections.singletonList(encryptPassword(password)));
            setShadowLastChange(ldapUser);
            if (!StringUtils.isEmpty(ldapSchema.sambaNTPassword)) addSmbPasswordAttr(ldapUser, password); 
            if (!StringUtils.isEmpty(ldapSchema.sambaPwdLastSet)) addSmbPwdLastSet(ldapUser); 
            finalizeLdapWriting(ldapUser);
        } catch (LdapAttributesModificationException | NoSuchAlgorithmException e) {
            logger.error("", e);
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
    }
    
    public void setPassword(String id,String code,String newLogin, final String password) throws LoginException, LdapProblemException, UserPermissionException {
            changeLogin(id, code, newLogin);         
            setPassword(id, code, password);
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

    private String bytes_to_string(byte[] bytes) {
        var hashedPwd = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xff;
            if (v < 16) {
                hashedPwd.append('0');
            }
            hashedPwd.append(Integer.toHexString(v));
        }
        return hashedPwd.toString().toUpperCase();
    }

    private String encryptPasswordSmb(String clearPassword) {
        var salt = new byte[4];
        new SecureRandom().nextBytes(salt);
        var md4 = new jcifs.util.MD4();
        md4.reset();
        md4.update(clearPassword.getBytes(Charset.forName("UTF-16LE")));
        return bytes_to_string(md4.digest());
    }

    private void addSmbPasswordAttr(LdapUserOut ldapUser, final String clearPassword) throws NoSuchAlgorithmException {
        // Ecrire l'attribut sambaNTPassword dans LDAP
        var passwd = encryptPasswordSmb(clearPassword);
        ldapUser.attributes().put("sambaNTPassword", Collections.singletonList(passwd));
    }

    private void addSmbPwdLastSet(LdapUserOut ldapUser) throws NoSuchAlgorithmException {
        // Ecrire l'attribut sambaPwdLastSet dans LDAP
        var cal = Calendar.getInstance();
        var sambaPwdLastSet = Integer.toString((int) Math.floor(cal.getTimeInMillis() / 1000 ));
        ldapUser.attributes().put("sambaPwdLastSet", Collections.singletonList(sambaPwdLastSet));
    }
}
