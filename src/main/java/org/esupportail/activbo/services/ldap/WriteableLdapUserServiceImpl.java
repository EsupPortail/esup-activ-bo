/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.activbo.services.ldap;

import javax.naming.Name;

import org.apache.commons.codec.binary.Base64;
import org.esupportail.activbo.exceptions.AuthentificationException;
import org.esupportail.commons.services.ldap.LdapException;
import org.esupportail.commons.services.ldap.LdapUser;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;

import net.sf.ehcache.CacheManager;

/**
 * An implementation of WriteableLdapService based on LdapTemplate.
 * See /properties/ldap/ldap-write-example.xml.
 */
public class WriteableLdapUserServiceImpl extends org.esupportail.commons.services.ldap.WriteableLdapUserServiceImpl implements WriteableLdapUserService {
    /**
     * The serialization id.
     */
    private static final long serialVersionUID = -2833750508738328830L;
    private final Logger logger = new LoggerImpl(getClass());

    private CacheManager cacheManager;

    public void setCacheManager(final CacheManager cacheManager) { this.cacheManager = cacheManager; }
    

    /** Modify an LDAP user using Spring LdapContextSource.
     * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#updateLdapUser(
     * org.esupportail.commons.services.ldap.LdapUser)
     */
    public void updateLdapUser(final LdapUser ldapUser) throws LdapAttributesModificationException {        
        super.updateLdapUser(ldapUser);
        invalidateLdapCache();      
    }

    /**
     * @param ldapUser
     * @param context
     */
    protected void mapToContext(final LdapUser ldapUser, final DirContextAdapter context) {
        var attributesNames = ldapUser.getAttributeNames();
        for (String ldapAttributeName : attributesNames) {
            var values = ldapUser.getAttributes(ldapAttributeName);
            
            // The attribute exists
            if (!values.contains("null") && values.size() != 0 ) {
                for (String val : values) 
                // Si insertion de l'attribut jpegphoto dans LDAP
                // Décoder la photo qui a été encodée lors de la saisie dans le formulaire accountDataChange
                 if (val.contains("encodeBase64")) {
                     var obj = Base64.decodeBase64(val.substring(12).getBytes());
                     context.setAttributeValues(ldapAttributeName, new Object[] { obj });

                     // l'attribut jpegPhoto n'est pas censé etre multi-value.
                     // spring-ldap ne sait pas faire de ADD_ATTRIBUTE + REMOVE_ATTRIBUTE de plusieurs jpegPhoto,on contourne ce bug
                     // en lui envoyant qu'un seul attribut origine pour qu'il puisse faire du REPLACE_ATTRIBUTE au lieu de ADD_ATTRIBUTE + REMOVE_ATTRIBUTE
                     var origAttr = context.getAttributes().get(ldapAttributeName);
                     if (origAttr!=null) {
                         // garder un seul attribut :
                         for (int i = 1; i < origAttr.size(); i++) origAttr.remove(i);
                     }
                 } else {
                     // insertion autres attributs que jpegphoto
                     context.setAttributeValues(ldapAttributeName, values.toArray());
                 }
            } else {
                context.setAttributeValues(ldapAttributeName, null); 
            }
        }
    }
    

    public void bindLdap(final LdapUser ldapUser)throws AuthentificationException{
        try {
            Name dn = buildLdapUserDn(ldapUser.getId());
            this.getLdapTemplate().lookup(dn);
        
        } catch (UncategorizedLdapException e) {
            logger.debug("Une authentification a échouée : "+e);
            if (e.getCause() instanceof javax.naming.AuthenticationException) {
                logger.warn("Authentification invalide pour l'utilisateur " + ldapUser.getId());
                throw new AuthentificationException("Authentification invalide pour l'utilisateur " + ldapUser.getId());
            }
        } 
    }
    
    public void defineAuthenticatedContext(String username, String password) throws LdapException {
        this.getContextSource().setUserName(username);
        this.getContextSource().setPassword(password);
    }
    
    public void defineAuthenticatedContextForUser(String userId, String password) throws LdapException{
        DistinguishedName ldapBindUserDn = new DistinguishedName(getDnAuth());
        ldapBindUserDn.add(getIdAuth(), userId);
        logger.debug("Binding to LDAP with DN [" + ldapBindUserDn + "] (password ******)");
        
        getContextSource().setUserName(ldapBindUserDn.encode());
        getContextSource().setPassword(password);
    }

    /**
     * @see org.esupportail.commons.services.ldap.WriteableLdapUserService#defineAnonymousContext()
     */
    public void defineAnonymousContext() throws LdapException {
        getContextSource().setUserName("");
        getContextSource().setPassword("");
    }

    public void invalidateLdapCache() {
        net.sf.ehcache.Cache cache = cacheManager.getCache(org.esupportail.commons.services.ldap.CachingLdapEntityServiceImpl.class.getName());
        cache.removeAll();      
    }
    
}
