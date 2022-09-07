package org.esupportail.activbo.domain;

import static org.esupportail.activbo.Utils.encryptSmbNTPassword;
import static org.esupportail.activbo.Utils.ldapShaPasswordEncoder;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Collections;

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
            ldapUser.attributes().put(ldapSchema.password, Collections.singletonList(ldapShaPasswordEncoder(password)));
            setShadowLastChange(ldapUser);
            if (!StringUtils.isEmpty(ldapSchema.sambaNTPassword)) addSmbNTPasswordAttr(ldapUser, password); 
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

    private void addSmbNTPasswordAttr(LdapUserOut ldapUser, final String clearPassword) throws NoSuchAlgorithmException {
        // Ecrire l'attribut sambaNTPassword dans LDAP
        var passwd = encryptSmbNTPassword(clearPassword);
        ldapUser.attributes().put("sambaNTPassword", Collections.singletonList(passwd));
    }

    private void addSmbPwdLastSet(LdapUserOut ldapUser) throws NoSuchAlgorithmException {
        // Ecrire l'attribut sambaPwdLastSet dans LDAP
        var cal = Calendar.getInstance();
        var sambaPwdLastSet = Integer.toString((int) Math.floor(cal.getTimeInMillis() / 1000 ));
        ldapUser.attributes().put("sambaPwdLastSet", Collections.singletonList(sambaPwdLastSet));
    }
}
