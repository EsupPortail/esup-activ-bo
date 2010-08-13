package org.esupportail.activbo.services.kerberos;



/**
 * @author aanli
 *
 */
public interface KRBAdmin {
		
	/**
	 * @param principal
	 * @param passwd
	 */
	public void add(String principal,String passwd) throws KRBException, KRBPrincipalAlreadyExistsException;
	
	
	/**
	 * @param principal
	 * @throws KRBException 
	 */
	public void del(final String principal) throws KRBException;
	
	/**
	 * @param principal
	 * @param passwd
	 * @throws KRBException 
	 */
	public void changePasswd(String principal,String passwd) throws KRBException,KRBIllegalArgumentException;
	
	
	/**
	 * uses principal privilege
	 * @param principal
	 * @param oldPasswd
	 * @param newPasswd
	 * @throws KRBException 
	 */
	public void changePasswd(String principal, String oldPasswd, String newPasswd) throws KRBException;
			
	/**
	 * @param principal
	 * @return true if principal exists
	 * @throws KRBException 
	 */
	public boolean exists(String principal) throws KRBException ;	
	
	/**
	 * @param oldPrincipal
	 * @param newPrincipal
	 * @throws KRBException, KRBPrincipalAlreadyExistsException
	 */
	public void rename(String oldPrincipal,String newPrincipal)throws KRBException,KRBPrincipalAlreadyExistsException;

}
