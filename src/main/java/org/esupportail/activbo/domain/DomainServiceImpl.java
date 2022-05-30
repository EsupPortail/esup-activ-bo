/**
 * ESUP-Portail esup-activ-bo - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-activ-bo
 */
package org.esupportail.activbo.domain;

import static org.esupportail.activbo.Utils.toArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.esupportail.activbo.Utils;
import org.esupportail.activbo.domain.beans.ValidationCodeImpl;
import org.esupportail.activbo.domain.beans.ValidationProxyTicket;
import org.esupportail.activbo.domain.beans.channels.Channel;
import org.esupportail.activbo.domain.beans.channels.ChannelException;
import org.esupportail.activbo.domain.tools.BruteForceBlock;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.activbo.exceptions.LdapProblemException;
import org.esupportail.activbo.exceptions.LoginException;
import org.esupportail.activbo.exceptions.UserPermissionException;
import org.esupportail.activbo.services.ldap.LdapAttributesModificationException;
import org.esupportail.activbo.services.ldap.LdapSchema;
import org.esupportail.activbo.services.ldap.LdapUser;
import org.esupportail.activbo.services.ldap.LdapUserImpl;
import org.esupportail.activbo.services.ldap.LdapUserOut;
import org.esupportail.activbo.services.ldap.WriteableLdapUserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private List<Channel> channels;
    protected LdapSchema ldapSchema;
    private String accountDescrCodeKey; 
    private String accountDescrPossibleChannelsKey;
    private ValidationCodeImpl validationCode;
    private ValidationProxyTicket validationProxyTicket;
    private BruteForceBlock bruteForceBlock;
    private WriteableLdapUserServiceImpl ldapUserService;
    private String displayNameLdapAttribute;
    private String casID;
    
    public void setChannels(List<Channel> channels) { this.channels = channels; }
    public void setLdapSchema(LdapSchema ldapSchema) { this.ldapSchema = ldapSchema; }
    public void setAccountDescrCodeKey(String accountDescrCodeKey) { this.accountDescrCodeKey = accountDescrCodeKey; }
    public void setAccountDescrPossibleChannelsKey( String accountDescrPossibleChannelsKey) { this.accountDescrPossibleChannelsKey = accountDescrPossibleChannelsKey; }
    public void setValidationCode(ValidationCodeImpl validationCode) { this.validationCode = validationCode; }
    public void setValidationProxyTicket(ValidationProxyTicket validationProxyTicket) { this.validationProxyTicket = validationProxyTicket; }
    public void setBruteForceBlock(BruteForceBlock bruteForceBlock) { this.bruteForceBlock = bruteForceBlock; }
    public void setLdapUserService(final WriteableLdapUserServiceImpl ldapUserService) { this.ldapUserService = ldapUserService; }
    public void setDisplayNameLdapAttribute(final String displayNameLdapAttribute) { this.displayNameLdapAttribute = displayNameLdapAttribute; }
    public void setCasID(String casID) { this.casID = casID; }
    
    public void afterPropertiesSet() throws Exception {
        if (ldapUserService == null) 
                throw new Exception("property ldapUserService of class " + getClass().getName() + " can not be null");
        if (channels == null) 
                throw new Exception("property channels of class " + getClass().getName() + " can not be null");
        if (StringUtils.isBlank(displayNameLdapAttribute)) 
                throw new Exception("property displayNameLdapAttribute of class " + getClass().getName() 
                + " can not be null");
    }
    

    private LdapUser searchUser(Map<String, String> hashInfToValidate, String[] wanted_attrs) throws LoginException, AuthentificationException {
        /**
         * Construction d'un filtre ldap a partir des donnees a valider.
         * Si l'application du filtre retourne une entree, c'est que les donnees sont valides
         */
        
        String filter = hasInf_to_ldap_filter(hashInfToValidate);
        logger.debug("Le filtre construit pour la validation est : "+filter);
        var ldapUser = getLdapUser(filter, wanted_attrs);

        if (ldapUser==null) {
            logger.warn("Identification failed: " + filter);
            throw new AuthentificationException("Identification échouée");
        }
        return ldapUser;
    }
    private String hasInf_to_ldap_filter(Map<String, String> hashInfToValidate) {
        String filter="(&";
        for (var entry: hashInfToValidate.entrySet()) {
            filter+="("+ entry.getKey() + "=" + escape_ldap_filter_value(entry.getValue()) + ")";
        }
        filter+=")";
        return filter;
    }
    private String escape_ldap_filter_value(String value) {
        //Suppression des caracteres speciaux susceptibles de permettre une injection ldap
        value=value.replace("&","");
        value=value.replace(")","");
        value=value.replace("(","");
        value=value.replace("|","");
        value=value.replace("*","");
        value=value.replace("=","");
        return value;
    }

    public Map<String,List<String>> validateAccount(Map<String,String> hashInfToValidate,Set<String>attrPersoInfo) throws AuthentificationException, LdapProblemException, LoginException{         
        var wanted_attrs = channelsNeededAttrs();
        logger.debug("attrPersoInfo: asked=" + attrPersoInfo + " needed=" + wanted_attrs);
        wanted_attrs.addAll(attrPersoInfo);                
        var ldapUser = searchUser(hashInfToValidate, toArray(wanted_attrs));
        
        //envoi d'un code si le compte n'est pas active
        boolean with_code = ldapUser.getAttribute(ldapSchema.shadowLastChange)==null;

        var infos = ldapInfos_and_maybe_code(ldapUser, attrPersoInfo, with_code);

        infos.put(accountDescrPossibleChannelsKey, possibleChannels(ldapUser));

        return infos;
    }
    
    private HashMap<String, List<String>> ldapInfos_and_maybe_code(LdapUser ldapUser, Set<String> wanted_attrs, boolean with_code) {
        var infos=new HashMap<String,List<String>>();
        infos.put(ldapSchema.login, ldapUser.getAttributeValues(ldapSchema.login));
        
        var rawAttrs = ((LdapUserImpl) ldapUser).attributes();
        for (String attr: wanted_attrs) {
            var vals = rawAttrs.get(attr);
            if (vals == null) vals = Collections.emptyList();
            var base64s = mayEncodeBase64s(vals);
            if (base64s != null) {
                infos.put(attr + ";base64", base64s);
            } else {
                infos.put(attr, (List<String>) vals); }
        }
        
        if (with_code) {
            String code = validationCode.generateCode(ldapUser.getAttribute(ldapSchema.login));
            infos.put(accountDescrCodeKey, Collections.singletonList(code));
            logger.debug("Insertion code pour l'utilisateur "+ldapUser.getAttribute(ldapSchema.login)+" dans la table effectuee");
        }
        return infos;
    }

    private List<String> mayEncodeBase64s(List<? extends Object> vals) {
        for (var val: vals) {
            if (!(val instanceof String)) return Utils.encodeBase64s(vals);
        }
        return null;
    }
    private HashSet<String> channelsNeededAttrs() {
        var r = new HashSet<String>();
        for (var c : channels) r.addAll(c.neededAttrs());
        return r;        
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
    
    public LdapUser getLdapUser(String filter, String[] wantedAttrs) throws LoginException{
        var ldapUser = ldapUserService.getLdapUserFromFilter(filter, 
            (String[]) ArrayUtils.add(ArrayUtils.add(wantedAttrs, ldapSchema.login), ldapSchema.shadowLastChange));
        
        if (ldapUser != null && ldapUser.getAttribute(ldapSchema.login)== null) {
                throw new LoginException("Le login pour l'utilisateur est null");
        }
        
        return ldapUser;
    }
    
    protected LdapUser getLdapUserId(String id, String[] wantedAttrs) throws LoginException{
        return getLdapUser("("+ldapSchema.login+"="+ id + ")", wantedAttrs);
    }

    public void verifyCode(String id, String code) throws UserPermissionException {
        if (!validationCode.verify(id,code)) throw new UserPermissionException("Code invalide L'utilisateur id="+id+" n'a pas le droit de continuer la procedure");
    }

    public LdapUserOut getLdapUserOut(String id) throws LdapProblemException, LoginException {
        var ldapUser = getLdapUserId(id, new String[] {});
        if (ldapUser==null) throw new LdapProblemException("Probleme au niveau de LDAP");
        return new LdapUserImpl(ldapUser.getDN());
    }
    
    public void updatePersonalInformations(String id,String code,Map<String,List<? extends Object>> hashBeanPersoInfo)throws LdapProblemException,UserPermissionException, LoginException{
        try {
            verifyCode(id, code);
            var ldapUser = getLdapUserOut(id);
                                            
            logger.debug("Parcours des informations personnelles mises a jour au niveau du FO pour insertion LDAP");
            
            for (var e : hashBeanPersoInfo.entrySet()) {
                logger.debug("Key="+e.getKey()+" Value="+e.getValue());
                logger.info(id + "@" + code + ": modification "+e.getKey()+": "+e.getValue());
                ldapUser.attributes().put(e.getKey(), e.getValue());
            }
           
            finalizeLdapWriting(ldapUser);          
        
        } catch (LdapAttributesModificationException e) {
            logger.debug("Exception thrown by updatePersonalInfo() : "+ e.getMessage());
            throw new LdapProblemException("Probleme au niveau du LDAP");
        }
    }

    public void removeCode(String user_id, String code) {
        validationCode.removeCode(user_id);
    }
    
    public void sendCode(String id,String channel)throws ChannelException{  
        for (var c : channels) {
            if (c.getName().equalsIgnoreCase(channel)) {
                c.send(id);
                break;
            }                   
        }
    }
    
    private HashMap<String,List<String>> getLdapInfos(String id,String password,Set<String>attrPersoInfo) throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
        try {
            if (bruteForceBlock.isBlocked(id)) {
                throw new UserPermissionException("Nombre de tentative d'authentification atteint pour l'utilisateur "+id);
            }
            var ldapUser = getLdapUserId(id, toArray(attrPersoInfo));
            if (ldapUser==null) throw new AuthentificationException("Login invalide");

            if (password!=null) {
                ldapUserService.bindLdap(ldapUser, password);
            }
    
            //envoi d'un code si le compte est active
            boolean with_code = ldapUser.getAttribute(ldapSchema.shadowLastChange)!=null;

            //Construction du hasMap de retour
            return ldapInfos_and_maybe_code(ldapUser, attrPersoInfo, with_code);
        } catch(AuthentificationException e) {
            //si authentification pas bonne 
            bruteForceBlock.setFail(id);
            throw e;
        }
    }
    
    public Map<String,List<String>> authentificateUser(String id,String password,Set<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException{
        if (password==null) throw new AuthentificationException("Password must not be null !");
        return getLdapInfos(id,password,attrPersoInfo);
    }
    
    public Map<String,List<String>> authentificateUserWithCas(String id,String proxyticket,String targetUrl,Set<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
        logger.debug("Id, proxyticket et targetUrl : "+id +","+proxyticket+ ", "+targetUrl);
        
        if (!validationProxyTicket.validation(id, proxyticket,targetUrl))
            throw new AuthentificationException();
        
        var ldapUser = getLdapUser("("+casID+"="+ id + ")", new String[] { ldapSchema.login });
        var login = ldapUser!=null ? ldapUser.getAttribute(ldapSchema.login) : id; 
        
        return getLdapInfos(login,null,attrPersoInfo);
    }
    
    public Map<String,List<String>> authentificateUserWithCodeKey(String id,String accountCodeKey,Set<String>attrPersoInfo)throws AuthentificationException,LdapProblemException,UserPermissionException, LoginException {
        
        logger.debug("Id et accountCodeKey : "+id +","+accountCodeKey);
        
        if (!validationCode.verify(id, accountCodeKey))
            throw new AuthentificationException();
        
        return getLdapInfos(id,null,attrPersoInfo);
    }

    protected void finalizeLdapWriting(LdapUserOut ldapUser) throws LdapAttributesModificationException {
        logger.debug("L'ecriture dans le LDAP commence");
        ldapUserService.updateLdapUser(ldapUser);
        logger.debug("Ecriture dans le LDAP reussie");
    }
    
    private int nowEpochDays() {
        var cal = Calendar.getInstance();
        return (int) Math.floor(cal.getTimeInMillis() / (1000 * 3600 * 24));
    }
    
    protected void setShadowLastChange(LdapUserOut ldapUser) {
        // Preparer l'attribut shadowLastChange a ecrire dans LDAP
        var shadowLastChange = Integer.toString(nowEpochDays());
        ldapUser.attributes().put(ldapSchema.shadowLastChange, Collections.singletonList(shadowLastChange));
        if (logger.isDebugEnabled()) {logger.debug("Setting shadowLastChange in LDAP : "+ shadowLastChange );}              
    }   

}
