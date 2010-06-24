package org.esupportail.activbo.services.remote;

import java.util.Date;
import java.util.HashMap;

import org.esupportail.commons.services.ldap.LdapException;



public interface AccountManagement {
		
	public HashMap<String,String> validAccount(String number,String birthName,Date birthDate)throws LdapException;

	public boolean updateLdap(final String currentPassword,String id,String code)throws LdapException;
		
	public void updateDisplayName(String displayName);
}
