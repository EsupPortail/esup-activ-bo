package org.esupportail.activbo.services.remote;

import java.util.Date;

import org.esupportail.commons.services.ldap.LdapException;



public interface Information {
		
	public boolean validAccount(String number,String birthName,Date birthDate)throws LdapException;

	public boolean updateLdap(final String currentPassword)throws LdapException;
		
	
}
