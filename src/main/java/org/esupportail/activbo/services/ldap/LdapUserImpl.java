package org.esupportail.activbo.services.ldap;

import java.io.UnsupportedEncodingException;
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
    
    public List<? extends Object> getRawAttributeValues(String name) {
        var result = attributes.get(name);
        return result != null ? result : new ArrayList<String>();
    }
    
    /**
     * @param name 
     * @return the values for an attribute.
     * 
     * this method excepts the values to be valid UTF-8 strings
     */
    public List<String> getAttributeValues(String name) {
        var r = new ArrayList<String>();
        var values = attributes.get(name);
        if (values != null) {
            for (var value : values) {
                r.add(ldapValueToString(value));
            }
        }
        return r;
    }

    // alias of getAttributeValue
    public String getAttribute(final String name) {
        return getAttributeValue(name);
    }

    // this method excepts the value to be a valid UTF-8 string
    public String getAttributeValue(final String name) {
        var values = attributes.get(name);
        return values != null && !values.isEmpty() ? ldapValueToString(values.get(0)) : null;
    }

    // LDAP values can be byte[] if attribute syntax is "OctetString",
    private String ldapValueToString(Object val) {
        if (val instanceof String) {
            return (String) val;
        } else if (val instanceof byte[]) {
            try {
                 return new String((byte[]) val, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
     
    /**
     * @return the map of all the attributes.
     */
    public HashMap<String, List<? extends Object>> attributes() {
        return attributes;
    }

}