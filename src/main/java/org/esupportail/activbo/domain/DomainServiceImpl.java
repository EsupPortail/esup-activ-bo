/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.esupportail.activbo.domain.beans.ValidationCode;
import org.esupportail.activbo.domain.beans.ValidationProxyTicket;
import org.esupportail.activbo.domain.beans.channels.Channel;
import org.esupportail.activbo.domain.beans.channels.ChannelException;
import org.esupportail.activbo.domain.tools.BruteForceBlock;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.KerberosException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.kerberos.KRBException;
import org.esupportail.activbo.services.kerberos.KRBIllegalArgumentException;
import org.esupportail.activbo.services.ldap.LdapSchema;
import org.esupportail.activbo.services.ldap.WriteableLdapUserService;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.ldap.LdapUserService;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.springframework.beans.factory.InitializingBean;




/**
 * The basic implementation of DomainService.
 * 
 * See /properties/domain/domain-example.xml
 */
public abstract class DomainServiceImpl implements DomainService, InitializingBean {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = -8200845058340254019L;

    private final Logger logger = new LoggerImpl(getClass());
    
    private List<Channel> channels;
    protected LdapSchema ldapSchema;
    private String accountDescrCodeKey; 
    private String accountDescrPossibleChannelsKey;
    private ValidationCode validationCode;
    private ValidationProxyTicket validationProxyTicket;
    private BruteForceBlock bruteForceBlock;
    private LdapUserService ldapUserService;
    private String displayNameLdapAttribute;
    private WriteableLdapUserService writeableLdapUserService;
    private String separator;
    private String casID;
    
    public void setChannels(List<Channel> channels) { this.channels = channels; }
    public void setLdapSchema(LdapSchema ldapSchema) { this.ldapSchema = ldapSchema; }
    public void setAccountDescrCodeKey(String accountDescrCodeKey) { this.accountDescrCodeKey = accountDescrCodeKey; }
    public void setAccountDescrPossibleChannelsKey( String accountDescrPossibleChannelsKey) { this.accountDescrPossibleChannelsKey = accountDescrPossibleChannelsKey; }
    public void setValidationCode(ValidationCode validationCode) { this.validationCode = validationCode; }
    public void setValidationProxyTicket(ValidationProxyTicket validationProxyTicket) { this.validationProxyTicket = validationProxyTicket; }
    public void setBruteForceBlock(BruteForceBlock bruteForceBlock) { this.bruteForceBlock = bruteForceBlock; }
    public void setLdapUserService(final LdapUserService ldapUserService) { this.ldapUserService = ldapUserService; }
    public void setDisplayNameLdapAttribute(final String displayNameLdapAttribute) { this.displayNameLdapAttribute = displayNameLdapAttribute; }
    public void setWriteableLdapUserService(WriteableLdapUserService writeableLdapUserService) { this.writeableLdapUserService = writeableLdapUserService; }
    public void setSeparator(String separator) { this.separator = separator; }
    public void setCasID(String casID) { this.casID = casID; }
    
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(ldapUserService, 
                "property ldapUserService of class " + getClass().getName() + " can not be null");
        Assert.notNull(channels, 
                "property channels of class " + getClass().getName() + " can not be null");
        Assert.hasText(displayNameLdapAttribute, 
                "property displayNameLdapAttribute of class " + getClass().getName() 
                + " can not be null");
    }
    

    private LdapUser searchUser(HashMap<String, String> hashInfToValidate) throws LoginException, AuthentificationException {
        /**
         * Construction d'un filtre ldap à partir des données à valider.
         * Si l'application du filtre retourne une entrée, c'est que les données sont valides
         */
        
        String filter = hasInf_to_ldap_filter(hashInfToValidate);
        logger.debug("Le filtre construit pour la validation est : "+filter);
        var ldapUser = getLdapUser(filter);

        if (ldapUser==null) {
            logger.warn("Identification failed: " + filter);
            throw new AuthentificationException("Identification �chouée : "+filter);
        }
        return ldapUser;
    }
    private String hasInf_to_ldap_filter(HashMap<String, String> hashInfToValidate) {
        String filter="(&";
        for (var entry: hashInfToValidate.entrySet()) {
            filter+="("+ entry.getKey() + "=" + escape_ldap_filter_value(entry.getValue()) + ")";
        }
        filter+=")";
        return filter;
    }
    private String escape_ldap_filter_value(String value) {
        //Suppression des caractères spéciaux susceptibles de permettre une injection ldap
        value=value.replace("&","");
        value=value.replace(")","");
        value=value.replace("(","");
        value=value.replace("|","");
        value=value.replace("*","");
        value=value.replace("=","");
        return value;
    }

    public HashMap<String,String> validateAccount(HashMap<String,String> hashInfToValidate,List<String>attrPersoInfo) throws AuthentificationException, LdapProblemException, LoginException{          
        var ldapUser = searchUser(hashInfToValidate);
        
        //envoi d'un code si le compte n'est pas activ�
        boolean with_code = ldapUser.getAttribute(ldapSchema.shadowLastChange)==null;

        var infos = ldapInfos_and_maybe_code(ldapUser, attrPersoInfo, with_code);

        infos.put(accountDescrPossibleChannelsKey, convertListToString(possibleChannels(ldapUser)));

        return infos;
    }
    
    private HashMap<String, String> ldapInfos_and_maybe_code(LdapUser ldapUser, List<String> wanted_attrs, boolean with_code) {
        var infos=new HashMap<String,String>();
        infos.put(ldapSchema.login, convertListToString(ldapUser.getAttributes(ldapSchema.login)));
        infos.put(ldapSchema.mail, convertListToString(ldapUser.getAttributes(ldapSchema.mail)));
        
        for (String attr: wanted_attrs) {
            infos.put(attr, convertListToString(ldapUser.getAttributes(attr)));
        }
        
        if (with_code) {
            String code = validationCode.generateCode(ldapUser.getAttribute(ldapSchema.login));
            infos.put(accountDescrCodeKey, code);
            logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(ldapSchema.login)+" dans la table effectu�e");
        }
        return infos;
    }

    private List<String> possibleChannels(LdapUser ldapUser) {
        var possibleChannels= new ArrayList<String>();
        for (var c : channels) {
            if (c.isPossible(ldapUser)) {
                possibleChannels.add(c.getName());
            }
        }
        return possibleChannels;
    }
    
    public LdapUser getLdapUser(String filter) throws LoginException{
        LdapUser ldapUser=null;
        List<LdapUser> ldapUserList = ldapUserService.getLdapUsersFromFilter(filter);
        
        if (ldapUserList.size() !=0) {
            ldapUser = ldapUserList.get(0);
            if (ldapUser.getAttribute(ldapSchema.login)== null) 
                throw new LoginException("Le login pour l'utilisateur est null");
        }
        
        return ldapUser;
    }
    
    protected LdapUser getLdapUserId(String id) throws LdapProblemException,LoginException{
        return getLdapUser("("+ldapSchema.login+"="+ id + ")");
    }

    public LdapUser getLdapUser(String id,String code) throws UserPermissionException,LdapProblemException,LoginException{
        if (!validationCode.verify(id,code)) throw new UserPermissionException("Code invalide L'utilisateur id="+id+" n'a pas le droit de continuer la procédure");
        
        var ldapUser = getLdapUserId(id);
        if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
        ldapUser.getAttributes().clear(); 
        return ldapUser;
    }
    
    public void updatePersonalInformations(String id,String code,HashMap<String,String> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException, LoginException{
        try {
            writeableLdapUserService.invalidateLdapCache();
            var ldapUser=getLdapUser(id,code);
                                            
            logger.debug("Parcours des informations personnelles mises � jour au niveau du FO pour insertion LDAP");
            
            for (var e : hashBeanPersoInfo.entrySet()) {
                logger.debug("Key="+e.getKey()+" Value="+e.getValue());
                logger.info(id + "@" + code + ": modification "+e.getKey()+": "+e.getValue());
                List<String> list = 
                    StringUtils.isEmpty(e.getValue()) ? Collections.<String>emptyList() :
                    e.getValue().contains(separator) ? Arrays.asList(e.getValue().split(separator)) :
                    Collections.singletonList(e.getValue());
                ldapUser.getAttributes().put(e.getKey(), list);
            }
           
            finalizeLdapWriting(ldapUser);          
        
        } catch (LdapException e) {
            logger.debug("Exception thrown by updatePersonalInfo() : "+ e.getMessage());
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
    }

    public void removeCode(String user_id, String code) {
        validationCode.removeCode(user_id);
    }
    
    public void sendCode(String id,String canal)throws ChannelException{    
        for (var c : channels) {
            if (c.getName().equalsIgnoreCase(canal)) {
                c.send(id);
                break;
            }                   
        }
    }
    
    private HashMap<String,String> getLdapInfos(String id,String password,List<String>attrPersoInfo) throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
        try {
            if (bruteForceBlock.isBlocked(id)) {
                throw new UserPermissionException("Nombre de tentative d'authentification atteint pour l'utilisateur "+id);
            }
            
            var ldapUser = getLdapUserId(id);       
            if (ldapUser==null) throw new AuthentificationException("Login invalide");

            if (password!=null) {
                writeableLdapUserService.defineAuthenticatedContextForUser(ldapUser.getId(), password);
                writeableLdapUserService.bindLdap(ldapUser);
            }
    
            //envoi d'un code si le compte est activé
            boolean with_code = ldapUser.getAttribute(ldapSchema.shadowLastChange)!=null;

            //Construction du hasMap de retour
            return ldapInfos_and_maybe_code(ldapUser, attrPersoInfo, with_code);
        } catch(LdapException e) {
            logger.debug("Exception thrown by authentificateUser() : "+ e.getMessage());
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
        catch(AuthentificationException e) {
            //si authentification pas bonne 
            bruteForceBlock.setFail(id);
            throw e;
        }
    }
    
    public HashMap<String,String> authentificateUser(String id,String password,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException{
        if (password==null) throw new AuthentificationException("Password must not be null !");
        return getLdapInfos(id,password,attrPersoInfo);
    }
    
    public HashMap<String,String> authentificateUserWithCas(String id,String proxyticket,String targetUrl,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
        logger.debug("Id, proxyticket et targetUrl : "+id +","+proxyticket+ ", "+targetUrl);
        
        if (!validationProxyTicket.validation(id, proxyticket,targetUrl))
            throw new AuthentificationException("Authentification failed ! ");
        
        var ldapUser =getLdapUser("("+casID+"="+ id + ")");
        var login = ldapUser!=null ? ldapUser.getAttribute(ldapSchema.login) : id; 
        
        return getLdapInfos(login,null,attrPersoInfo);
    }
    
    public HashMap<String,String> authentificateUserWithCodeKey(String id,String accountCodeKey,List<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
        
        logger.debug("Id et accountCodeKey : "+id +","+accountCodeKey);
        
        if (!validationCode.verify(id, accountCodeKey))
            throw new AuthentificationException("Authentification failed ! ");
        
        return getLdapInfos(id,null,attrPersoInfo);
    }

    public boolean validateCode(String id,String code)throws UserPermissionException {
        return validationCode.verify(id,code);
    }
    
    protected void finalizeLdapWriting(LdapUser ldapUser)throws LdapException{
        logger.debug("L'ecriture dans le LDAP commence");
        writeableLdapUserService.defineAuthenticatedContext(ldapSchema.usernameAdmin, ldapSchema.passwordAdmin);
        writeableLdapUserService.updateLdapUser(ldapUser);
        writeableLdapUserService.defineAnonymousContext();
        logger.debug("Ecriture dans le LDAP r�ussie");
    }
    
    private String convertListToString(List<String>listString) {
        return String.join(separator, listString);
    }   
    
    private int nowEpochDays() {
        var cal = Calendar.getInstance();
        return (int) Math.floor(cal.getTimeInMillis() / (1000 * 3600 * 24));
    }
    
    protected void setShadowLastChange(LdapUser ldapUser) {
        // Préparer l'attribut shadowLastChange à écrire dans LDAP
        var shadowLastChange = Integer.toString(nowEpochDays());
        ldapUser.getAttributes().put(ldapSchema.shadowLastChange, Collections.singletonList(shadowLastChange));
        if (logger.isDebugEnabled()) {logger.debug("Setting shadowLastChange in LDAP : "+ shadowLastChange );}              
    }
    
    /**
     * But : Gestion des exceptions
     */
    protected void exceptions(Exception exception) throws LdapProblemException,KerberosException, LoginException, UserPermissionException{
        logger.debug("Dans m�thode exceptions",exception);
        if      (exception instanceof LdapException)throw new LdapProblemException("Probleme au niveau du LDAP");
        else if (exception instanceof KRBException) throw new KerberosException("Probleme au niveau de Kerberos", exception);
        else if (exception instanceof KRBIllegalArgumentException) throw new KerberosException("Probleme au niveau de Kerberos", exception);
        else if (exception instanceof UserPermissionException) throw (UserPermissionException)exception;
        else if (exception instanceof RuntimeException) throw (RuntimeException)(exception);
        else logger.error("Erreur inattendue");
        
    }
    

}
