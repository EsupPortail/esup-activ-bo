/**
 * ESUP-Portail Commons - Copyright (c) 2006-2009 ESUP-Portail consortium.
 */
package org.esupportail.activbo.services.ldap;

import static org.esupportail.activbo.Utils.asMap;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.esupportail.activbo.exceptions.AuthentificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.esupportail.activbo.Utils.toArray;

public class WriteableLdapUserServiceImpl {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private String bindUrl;
    private String adminBindDN;
    private String adminBindPassword;
    private String peopleDN;

    public void setBindUrl(String bindUrl) { this.bindUrl = bindUrl; }
    public void setAdminBindDN(String adminBindDN) { this.adminBindDN = adminBindDN; }
    public void setAdminBindPassword(String adminBindPassword) { this.adminBindPassword = adminBindPassword; }
    public void setPeopleDN(String peopleDN) { this.peopleDN = peopleDN; }


    /** Modify an LDAP user using Spring LdapContextSource.
     */
    public void updateLdapUser(final LdapUserOut ldapUser) throws LdapAttributesModificationException {     
        DirContext dir_context = ldap_admin_connect();
        String[] wantedAttrs = toArray(ldapUser.attributes().keySet());
        var current = getLdapUserFromDN(dir_context, ldapUser.getDN(), wantedAttrs);
        var mods = toModificationItems(current, ldapUser);
        if (mods.isEmpty()) {
            logger.debug("aucune modification a envoyer a LDAP");
            return;
        }
        try {
            logger.debug("L'ecriture dans le LDAP commence");
            dir_context.modifyAttributes(ldapUser.getDN(), mods.toArray(new ModificationItem[0]));
            logger.debug("Ecriture dans le LDAP reussie");
        } catch (NamingException e) {
            throw new LdapAttributesModificationException(e);
        }
    }

    /**
     * @param ldapUser
     * @param context
     */
    protected List<ModificationItem> toModificationItems(LdapUser current, LdapUserOut wanted) {
        var r = new LinkedList<ModificationItem>();
        for (var attrVals : wanted.attributes().entrySet()) {
            String attr = attrVals.getKey();
            var wantedVals = attrVals.getValue();
            var currentVals = current.getRawAttributeValues(attr);

            if (hasNonString(wantedVals) || hasNonString(currentVals)) {
                logger.debug("will replace binary attribute "  + attr + " values " + wantedVals);
                r.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, toJndiAttribute(attr, wantedVals)));
                continue;
            }

            var toAdd = difference(wantedVals, currentVals);
            var toRemove = difference(currentVals, wantedVals);

            if (!toRemove.isEmpty()) {
                logger.debug("will remove attribute "  + attr + " values " + toRemove);
                r.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, toJndiAttribute(attr, toRemove)));
            }
            if (!toAdd.isEmpty()) {
                logger.debug("will add attribute "  + attr + " values " + toAdd);
                r.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, toJndiAttribute(attr, toAdd)));
            }
        }
        return r;
    }

    private List<? extends Object> difference(List<? extends Object> wanted, List<? extends Object> non_wanted) {
        var r = new LinkedList<Object>(wanted);
        r.removeAll(non_wanted);
        return r;
    }

    private BasicAttribute toJndiAttribute(String attrName, List<? extends Object> currentVals) {
        var attr = new BasicAttribute(attrName);
        for (var val : currentVals) attr.add(val);
        return attr;
    }

    public void bindLdap(final LdapUser ldapUser, String password) throws AuthentificationException {
        try {
            ldap_connect(ldapUser.getDN(), password);
        } catch (javax.naming.AuthenticationException e) {
            throw new AuthentificationException("Identification échouée");
        } catch (NamingException e) {
            logger.error("error connecting to ldap server", e);
            throw new RuntimeException("error connecting to ldap server");
        }
    }

    private boolean hasNonString(List<? extends Object> vals) {
        for (var val : vals) {
            if (!(val instanceof String)) return true;
        }
        return false;

    }

    private LdapUser getLdapUserFromDN(DirContext dir_context, String dn, String[] wantedAttrs) {
        var ldapUser = new LdapUserImpl(dn);
        try {
            addAttrValues(ldapUser, dir_context.getAttributes(dn, wantedAttrs));
            return ldapUser;
        } catch (NamingException e) {
            logger.error("", e);
            return null;
        }
    }

    public LdapUser getLdapUserFromFilter(String filter, String[] wantedAttrs) {
        return getLdapUserFromFilter(ldap_admin_connect(), filter, wantedAttrs);
    }

    private LdapUser getLdapUserFromFilter(DirContext dir_context, String filter, String[] wantedAttrs) {
        try {
            var list = Collections.list(dir_context.search(peopleDN, filter, searchControls(wantedAttrs)));
            if (list.size() == 0) {
                return null;
            } else if (list.size() > 1) {
                throw new RuntimeException("ambigous filter " + filter);
            } else {
                return toLdapUser(list.get(0));
            }
        } catch (NamingException e) { 
            logger.error("", e);
            return null;
        }
    }
    private SearchControls searchControls(String[] wantedAttrs) {
        var ctrls = new javax.naming.directory.SearchControls();
        ctrls.setCountLimit(2);
        ctrls.setReturningAttributes(wantedAttrs);
        return ctrls;
    }

    private LdapUser toLdapUser(SearchResult entry) throws NamingException {
        var ldapUser = new LdapUserImpl(entry.getNameInNamespace());
        addAttrValues(ldapUser, entry.getAttributes());        
        return ldapUser;
    }
    @SuppressWarnings("unchecked")
    private void addAttrValues(LdapUserOut ldapUser, Attributes attrs) throws NamingException {
        var attrsIt = attrs.getAll();
        while (attrsIt.hasMore()) {
            var attr = attrsIt.next();
            ldapUser.attributes().put(attr.getID(), (List<Object>) Collections.list(attr.getAll()));
        }
    }

    private DirContext ldap_admin_connect() {
        try {
            return ldap_connect(adminBindDN, adminBindPassword);
        } catch (NamingException e) {
            logger.error("error connecting to ldap server", e);
            throw new RuntimeException("error connecting to ldap server");
        }
    }

    private DirContext ldap_connect(String bindDN, String password) throws NamingException {
        Map<String,String> env =
            asMap(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
             .add(Context.PROVIDER_URL, bindUrl)
             .add(Context.SECURITY_AUTHENTICATION, "simple")
             .add(Context.SECURITY_PRINCIPAL, bindDN)
             .add(Context.SECURITY_CREDENTIALS, password);

        return new InitialDirContext(new Hashtable<>(env));
    }
    
}
