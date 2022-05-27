package org.esupportail.activbo.services.ldap;

import java.util.List;

public interface LdapUser {
    public String getDN();    

    /**
     * @param name 
     * @return the values for an attribute.
     */
    public List<String> getAttributeValues(String name);

    // alias of getAttributeValue
    public String getAttribute(final String name);

    public String getAttributeValue(final String name);
}