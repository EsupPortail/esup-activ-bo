package org.esupportail.activbo.services.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LdapUserImpl implements LdapUser, LdapUserOut {
    private String dn;
    protected HashMap<String, List<? extends Object>> attributes = new HashMap<>();

    public LdapUserImpl(String dn) {
        this.dn = dn;
    }

    public String getDN() {
        return dn;
    }
     
    /**
     * @param name 
     * @return the values for an attribute.
     */
    public List<String> getAttributeValues(String name) {
        var result = attributes.get(name);
        return result != null ? (List<String>) result : new ArrayList<String>();
    }

    // alias of getAttributeValue
    public String getAttribute(final String name) {
        return getAttributeValue(name);
    }

    public String getAttributeValue(final String name) {
        var values = getAttributeValues(name);
        return values.size() > 0 ? (String) values.get(0) : null;
    }
     
    /**
     * @return the map of all the attributes.
     */
    public HashMap<String, List<? extends Object>> attributes() {
        return attributes;
    }

}