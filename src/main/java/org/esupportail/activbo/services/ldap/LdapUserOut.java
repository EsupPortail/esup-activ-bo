package org.esupportail.activbo.services.ldap;

import java.util.HashMap;
import java.util.List;

public interface LdapUserOut {
    public String getDN();    
    public HashMap<String, List<? extends Object>> attributes();
}