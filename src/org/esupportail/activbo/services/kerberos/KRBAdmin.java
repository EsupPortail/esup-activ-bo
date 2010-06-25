package org.esupportail.activbo.services.kerberos;


/**
 * @author aanli
 *
 */
public interface KRBAdmin {
	
	public final static int ADDED=0,
							ALREADY_EXIST=1,
							NOT_CHANGED=2,
							CHANGED=3,
							ILLEGAL_ARGUMENT=4,
							DELETED=5,
							ERROR=6;
	
	/**
	 * @param principal
	 * @param passwd
	 * @return ALREADY_EXIST, ADDED, ERROR
	 */
	public int add(String principal,String passwd);
	
	
	/**
	 * @param principal
	 * @return DELETED, ERROR
	 */
	public int del(final String principal);
	
	/**
	 * @param principal
	 * @param passwd
	 * @return CHANGED, ILLEGAL_ARGUMENT, ERROR
	 */
	public int changePasswd(String principal,String passwd);
	
	
	/**
	 * uses principal privilege
	 * @param principal
	 * @param oldPasswd
	 * @param newPasswd
	 * @return NOT_CHANGED, CHANGED, ERROR
	 */
	public int changePasswd(String principal, String oldPasswd, String newPasswd);
			
	/**
	 * @param principal
	 * @return true if principal exists
	 */
	public boolean exists(String principal) ;	

}
